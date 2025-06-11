package com.multikube_rest_service.services.provider;

import com.multikube_rest_service.common.SecurityContextHelper; //
import com.multikube_rest_service.common.encryption.KubeconfigEncryptor;
import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.dtos.requests.provider.ClusterAllocationRequest;
import com.multikube_rest_service.dtos.requests.provider.ClusterRegistrationRequest;
import com.multikube_rest_service.dtos.responses.provider.ClusterDto;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.User;
import com.multikube_rest_service.entities.provider.ClusterAllocation;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.provider.KubernetesClusterMapper;
import com.multikube_rest_service.repositories.TenantRepository;
import com.multikube_rest_service.repositories.UserRepository; //
import com.multikube_rest_service.repositories.provider.ClusterAllocationRepository;
import com.multikube_rest_service.repositories.provider.KubernetesClusterRepository;
import com.multikube_rest_service.repositories.provider.TenantNamespaceRepository;
import com.multikube_rest_service.rest.RestMessageResponse;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.StringReader;
import java.util.Map;

/**
 * Service layer for managing Kubernetes cluster registration and related operations
 * from a provider's perspective.
 */
@Service
public class ProviderClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderClusterService.class);

    private final KubernetesClusterRepository clusterRepository;
    private final UserRepository userRepository;
    private final KubeconfigEncryptor kubeconfigEncryptor;
    private final KubernetesClusterMapper clusterMapper;
    private final ClusterAllocationRepository clusterAllocationRepository;
    private final TenantRepository tenantRepository;
    private final TenantNamespaceRepository tenantNamespaceRepository;

    /**
     * Constructs a new ProviderClusterService.
     *
     * @param clusterRepository   The repository for Kubernetes cluster data.
     * @param userRepository      The repository for user data.
     * @param kubeconfigEncryptor The utility for encrypting/decrypting kubeconfigs.
     * @param clusterMapper       The mapper for converting between cluster entities and DTOs.
     */
    public ProviderClusterService(KubernetesClusterRepository clusterRepository,
                                  UserRepository userRepository,
                                  KubeconfigEncryptor kubeconfigEncryptor,
                                  KubernetesClusterMapper clusterMapper,
                                  ClusterAllocationRepository clusterAllocationRepository,
                                  TenantRepository tenantRepository, TenantNamespaceRepository tenantNamespaceRepository
    ) {
        this.clusterRepository = clusterRepository;
        this.userRepository = userRepository;
        this.kubeconfigEncryptor = kubeconfigEncryptor;
        this.clusterMapper = clusterMapper;
        this.clusterAllocationRepository = clusterAllocationRepository;
        this.tenantRepository = tenantRepository;
        this.tenantNamespaceRepository = tenantNamespaceRepository;
    }

    /**
     * Registers a new Kubernetes cluster.
     * This involves validating the request, encrypting the kubeconfig,
     * saving the cluster information, and attempting an initial connectivity verification.
     *
     * @param request The cluster registration request DTO containing name, description, and kubeconfig.
     * @return A DTO representing the newly registered cluster, including its initial status.
     * @throws ResourceNotFoundException if the authenticated provider user is not found.
     * @throws IllegalArgumentException  if the cluster name or kubeconfig is invalid or if a cluster with the same name already exists.
     */
    @Transactional
    public ClusterDto registerCluster(ClusterRegistrationRequest request) {
        Long providerUserId = SecurityContextHelper.getAuthenticatedUserId();
        User providerUser = userRepository.findById(providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider user not found with ID: " + providerUserId));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be empty.");
        }
        if (request.getKubeconfig() == null || request.getKubeconfig().trim().isEmpty()) {
            throw new IllegalArgumentException("Kubeconfig cannot be empty.");
        }

        String trimmedName = request.getName().trim();
        if (clusterRepository.findByName(trimmedName).isPresent()) {
            throw new IllegalArgumentException("A cluster with the name '" + trimmedName + "' already exists.");
        }

        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setName(trimmedName);
        cluster.setDescription(request.getDescription());
        cluster.setKubeconfigEncrypted(kubeconfigEncryptor.encrypt(request.getKubeconfig()));
        cluster.setProviderUser(providerUser);
        cluster.setStatus(ClusterStatus.PENDING_VERIFICATION); // Initial status

        KubernetesCluster savedCluster = clusterRepository.save(cluster);
        logger.info("Cluster '{}' registered with ID {} by provider user ID {}. Attempting initial verification.",
                savedCluster.getName(), savedCluster.getId(), providerUserId);

        // Attempt to verify connectivity and update status
        tryToVerifyConnectivity(savedCluster);

        return clusterMapper.toDto(savedCluster);
    }

    /**
     * Attempts to verify connectivity to the given Kubernetes cluster using its stored, decrypted kubeconfig.
     * Updates the cluster's status to ACTIVE if successful, or UNREACHABLE/ERROR otherwise.
     * This method is intended for internal use after registration or for explicit re-verification.
     *
     * @param cluster The KubernetesCluster entity to verify.
     */
    @Transactional
    // Ensures status update is part of the same transaction if called within one, or its own if called separately.
    public void tryToVerifyConnectivity(KubernetesCluster cluster) {
        if (cluster == null) {
            logger.warn("Attempted to verify connectivity for a null cluster object.");
            return;
        }
        if (cluster.getKubeconfigEncrypted() == null) {
            logger.warn("Kubeconfig is null for cluster ID: {}. Cannot verify.", cluster.getId());
            cluster.setStatus(ClusterStatus.ERROR); // Or a specific status like CONFIG_ERROR
            clusterRepository.save(cluster);
            return;
        }

        String decryptedKubeconfig = null;
        try {
            decryptedKubeconfig = kubeconfigEncryptor.decrypt(cluster.getKubeconfigEncrypted());
            if (decryptedKubeconfig == null) {
                logger.warn("Decrypted Kubeconfig is null for cluster ID: {}. Cannot verify.", cluster.getId());
                cluster.setStatus(ClusterStatus.ERROR);
                clusterRepository.save(cluster);
                return;
            }

            try (StringReader reader = new StringReader(decryptedKubeconfig)) {
                KubeConfig kubeConfig = KubeConfig.loadKubeConfig(reader);
                ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
                // Set fairly short timeouts for initial verification
                client.setConnectTimeout(5000); // 5 seconds
                client.setReadTimeout(10000);   // 10 seconds

                // It's good practice to use the customized ApiClient if you've set timeouts on it.
                CoreV1Api api = new CoreV1Api(client);
                // Perform a lightweight operation, like listing namespaces, limited to a few items.
                // The 'limit' parameter helps ensure the call doesn't fetch too much data.
                // If you want to apply a limit or timeout specifically to this call:
                V1NamespaceList response = api.listNamespace()
                        .limit(1) // Limit to 1 result
                        .timeoutSeconds(5) // Specific timeout for this API call
                        .execute();
                cluster.setStatus(ClusterStatus.ACTIVE);
                logger.info("Successfully verified connectivity for cluster ID: {}", cluster.getId());
            }
        } catch (Exception e) {
            // Catching a broad exception here because K8s client can throw various things.
            cluster.setStatus(ClusterStatus.UNREACHABLE);
            logger.warn("Failed to verify connectivity for cluster ID: {}. Error: {}", cluster.getId(), e.getMessage());
            // For security and to avoid information leakage, do not propagate raw K8s client exceptions to the user.
        } finally {
            // Persist status changes.
            // This is crucial because if tryToVerifyConnectivity is called standalone later (not within registerCluster's transaction),
            // the status update needs to be saved.
            clusterRepository.save(cluster);
        }
    }

    /**
     * Retrieves details of a specific cluster registered by the currently authenticated provider.
     *
     * @param clusterId The ID of the cluster to retrieve.
     * @return A DTO representing the cluster.
     */
    @Transactional(readOnly = true)
    public ClusterDto getCluster(Long clusterId) {
        Long providerUserId = SecurityContextHelper.getAuthenticatedUserId();
        KubernetesCluster cluster = clusterRepository.findByIdAndProviderUser_Id(clusterId, providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cluster not found with ID: " + clusterId + " for the current provider."));
        return clusterMapper.toDto(cluster);
    }

    /**
     * Retrieves a paginated list of clusters registered by the currently authenticated provider,
     * with support for filtering by name and status from the searchParams map.
     *
     * @param searchParams A map of query parameters (e.g., "name" -> "mycluster", "status" -> "ACTIVE").
     * @param pageable     Pagination information.
     * @return A page of ClusterResponse DTOs.
     */
    @Transactional(readOnly = true)
    public Page<ClusterDto> getClusters(Map<String, String> searchParams, Pageable pageable) {
        Long providerUserId = SecurityContextHelper.getAuthenticatedUserId();

        String nameFilter = searchParams.getOrDefault("name", "").trim();
        String statusFilterString = searchParams.getOrDefault("status", "").trim();

        ClusterStatus statusFilter = null;
        if (StringUtils.hasText(statusFilterString)) {
            try {
                statusFilter = ClusterStatus.valueOf(statusFilterString.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status value provided in filter: '{}'. Ignoring status filter.", statusFilterString);
                // statusFilter remains null, so it won't be used
            }
        }

        Page<KubernetesCluster> clusterPage;

        boolean hasNameFilter = StringUtils.hasText(nameFilter);

        if (hasNameFilter && statusFilter != null) {
            clusterPage = clusterRepository.findByProviderUser_IdAndNameContainingIgnoreCaseAndStatus(
                    providerUserId, nameFilter, statusFilter, pageable);
        } else if (hasNameFilter) {
            clusterPage = clusterRepository.findByProviderUser_IdAndNameContainingIgnoreCase(
                    providerUserId, nameFilter, pageable);
        } else if (statusFilter != null) {
            clusterPage = clusterRepository.findByProviderUser_IdAndStatus(
                    providerUserId, statusFilter, pageable);
        } else {
            clusterPage = clusterRepository.findByProviderUser_Id(providerUserId, pageable);
        }

        return clusterPage.map(clusterMapper::toDto);
    }

    /**
     * Allocates a registered cluster to a specific tenant.
     *
     * @param clusterId The ID of the cluster to allocate.
     * @param allocationRequest The request containing the ID of the tenant.
     * @return A message response indicating success.
     */
    @Transactional
    public RestMessageResponse allocateCluster(Long clusterId, ClusterAllocationRequest allocationRequest) {
        Long providerUserId = SecurityContextHelper.getAuthenticatedUserId();

        // 1. Validate that the cluster exists and is owned by the current provider
        KubernetesCluster cluster = clusterRepository.findByIdAndProviderUser_Id(clusterId, providerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cluster not found with ID: " + clusterId + " for the current provider."));

        // 2. Validate that the cluster is in an ACTIVE state and can be allocated
        if (cluster.getStatus() != ClusterStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cluster must be in ACTIVE state to be allocated. Current status: " + cluster.getStatus());
        }

        // 3. Validate that the cluster is not already allocated
        if (clusterAllocationRepository.existsByKubernetesClusterId(clusterId)) {
            throw new IllegalArgumentException("Cluster with ID " + clusterId + " is already allocated to a tenant.");
        }

        // 4. Validate that the tenant exists
        Long tenantId = allocationRequest.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + tenantId));

        // Prevent allocating to the "System" tenant
        if ("System".equalsIgnoreCase(tenant.getName())) {
            throw new IllegalArgumentException("Cannot allocate clusters to the 'System' tenant.");
        }

        // 5. Create and save the allocation
        ClusterAllocation allocation = new ClusterAllocation();
        allocation.setKubernetesCluster(cluster);
        allocation.setTenant(tenant);
        clusterAllocationRepository.save(allocation);

        logger.info("Cluster ID {} successfully allocated to Tenant ID {}", clusterId, tenantId);

        return new RestMessageResponse("Cluster " + cluster.getName() + " successfully allocated to tenant " + tenant.getName() + ".");
    }

    /**
     * De-allocates a cluster from a tenant, making it available again.
     * This operation will fail if the tenant has created any namespaces on the cluster.
     *
     * @param clusterId The ID of the cluster to de-allocate.
     * @return A message response indicating success.
     */
    @Transactional
    public RestMessageResponse deallocateCluster(Long clusterId) {
        Long providerUserId = SecurityContextHelper.getAuthenticatedUserId();

        // 1. Verify the cluster exists and is owned by the provider.
        // We also need to find the allocation record to know which tenant to check.
        ClusterAllocation allocation = clusterAllocationRepository.findByKubernetesClusterId(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException("No allocation found for cluster ID: " + clusterId));

        // Security check: Ensure the cluster related to the allocation is owned by the current provider.
        if (!allocation.getKubernetesCluster().getProviderUser().getId().equals(providerUserId)) {
            throw new ResourceNotFoundException(
                    "Cluster allocation not found for cluster ID: " + clusterId + " for the current provider.");
        }

        // 2. CRITICAL VALIDATION: Check if the tenant has any active resources on this cluster.
        // For now, we check for namespaces. This should be expanded to check tenant_workloads later.
        if (tenantNamespaceRepository.existsByKubernetesClusterId(clusterId)) {
            throw new IllegalStateException(
                    "Cannot de-allocate cluster. Tenant still has active namespaces on it. Please ensure all tenant resources are removed first.");
        }
        // TODO: Add a similar check for tenant_workloads once that repository exists.
        // if (tenantWorkloadRepository.existsByTenantNamespace_KubernetesClusterId(clusterId)) { ... }

        // 3. If validation passes, delete the allocation.
        clusterAllocationRepository.delete(allocation);

        logger.info("Cluster ID {} successfully de-allocated from Tenant ID {}.",
                clusterId, allocation.getTenant().getId());

        return new RestMessageResponse("Cluster " + allocation.getKubernetesCluster().getName() + " has been successfully de-allocated.");
    }
}