package com.multikube_rest_service.services.provider;

import com.multikube_rest_service.common.SecurityContextHelper; //
import com.multikube_rest_service.common.encryption.KubeconfigEncryptor;
import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.dtos.requests.provider.ClusterRegistrationRequest;
import com.multikube_rest_service.dtos.responses.provider.ClusterDto;
import com.multikube_rest_service.entities.User;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.provider.KubernetesClusterMapper;
import com.multikube_rest_service.repositories.UserRepository; //
import com.multikube_rest_service.repositories.provider.KubernetesClusterRepository;
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

    /**
     * Constructs a new ProviderClusterService.
     *
     * @param clusterRepository     The repository for Kubernetes cluster data.
     * @param userRepository        The repository for user data.
     * @param kubeconfigEncryptor   The utility for encrypting/decrypting kubeconfigs.
     * @param clusterMapper         The mapper for converting between cluster entities and DTOs.
     */
    public ProviderClusterService(KubernetesClusterRepository clusterRepository,
                                  UserRepository userRepository,
                                  KubeconfigEncryptor kubeconfigEncryptor,
                                  KubernetesClusterMapper clusterMapper) {
        this.clusterRepository = clusterRepository;
        this.userRepository = userRepository;
        this.kubeconfigEncryptor = kubeconfigEncryptor;
        this.clusterMapper = clusterMapper;
    }

    /**
     * Registers a new Kubernetes cluster.
     * This involves validating the request, encrypting the kubeconfig,
     * saving the cluster information, and attempting an initial connectivity verification.
     *
     * @param request The cluster registration request DTO containing name, description, and kubeconfig.
     * @return A DTO representing the newly registered cluster, including its initial status.
     * @throws ResourceNotFoundException if the authenticated provider user is not found.
     * @throws IllegalArgumentException if the cluster name or kubeconfig is invalid or if a cluster with the same name already exists.
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
    @Transactional // Ensures status update is part of the same transaction if called within one, or its own if called separately.
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
     * @param pageable Pagination information.
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
}