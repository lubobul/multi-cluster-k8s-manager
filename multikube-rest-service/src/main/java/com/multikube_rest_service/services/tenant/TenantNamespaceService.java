package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.common.enums.NamespaceStatus;
import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.common.enums.SyncStatus;
import com.multikube_rest_service.dtos.requests.tenant.CreateNamespaceRequest;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceSummaryDto;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.entities.tenant.TenantNamespace;
import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.TenantNamespaceMapper;
import com.multikube_rest_service.repositories.TenantRepository;
import com.multikube_rest_service.repositories.provider.ClusterAllocationRepository;
import com.multikube_rest_service.repositories.provider.KubernetesClusterRepository;
import com.multikube_rest_service.repositories.tenant.TenantNamespaceRepository;
import com.multikube_rest_service.services.kubernetes.KubernetesClientService;
import com.multikube_rest_service.services.kubernetes.factories.KubernetesResource;
import com.multikube_rest_service.services.kubernetes.factories.KubernetesResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@Service
public class TenantNamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(TenantNamespaceService.class);

    private final TenantNamespaceRepository namespaceRepository;
    private final KubernetesClusterRepository clusterRepository;
    private final ClusterAllocationRepository clusterAllocationRepository;
    private final TenantRepository tenantRepository;
    private final TenantNamespaceMapper namespaceMapper;
    private final KubernetesClientService kubernetesClientService;
    private final KubernetesResourceFactory resourceFactory;

    public TenantNamespaceService(
            TenantNamespaceRepository namespaceRepository,
            KubernetesClusterRepository clusterRepository,
            ClusterAllocationRepository clusterAllocationRepository,
            TenantRepository tenantRepository,
            TenantNamespaceMapper namespaceMapper,
            KubernetesClientService kubernetesClientService,
            KubernetesResourceFactory resourceFactory) {
        this.namespaceRepository = namespaceRepository;
        this.clusterRepository = clusterRepository;
        this.clusterAllocationRepository = clusterAllocationRepository;
        this.tenantRepository = tenantRepository;
        this.namespaceMapper = namespaceMapper;
        this.kubernetesClientService = kubernetesClientService;
        this.resourceFactory = resourceFactory;
    }

    /**
     * Creates a new namespace for the authenticated tenant. This is a transactional
     * operation that orchestrates several actions:
     * <ol>
     * <li>Validates that the tenant has access to the target cluster and the namespace name is unique.</li>
     * <li>Creates the actual namespace resource in the Kubernetes cluster.</li>
     * <li>Applies a default, isolating NetworkPolicy.</li>
     * <li>Creates a default admin Role and a RoleBinding for the creating user.</li>
     * <li>Applies any optional ResourceQuota or LimitRange manifests provided in the request.</li>
     * <li>Persists the final state of the namespace and all its configurations into the Multikube database.</li>
     * </ol>
     * If any Kubernetes operation fails after the namespace is created, the overall status is marked
     * as FAILED_CREATION for auditing purposes.
     *
     * @param request The DTO containing the details for the new namespace, such as its name,
     * description, target cluster ID, and optional YAML configurations.
     * @return A comprehensive DTO (TenantNamespaceDto) representing the final state of the
     * newly created namespace and its initial set of configuration resources.
     * @throws SecurityException if the specified cluster is not allocated to the current tenant.
     * @throws IllegalArgumentException if a namespace with the same name already exists in the target cluster.
     * @throws ResourceNotFoundException if the target cluster ID does not exist.
     */
    @Transactional
    public TenantNamespaceDto createNamespace(CreateNamespaceRequest request) {
        JwtUserDetails userDetails = SecurityContextHelper.getAuthenticatedUser();
        Long tenantId = userDetails.getTenantId();
        String username = userDetails.getUsername();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user's tenant with ID " + tenantId + " not found in database."));

        Long clusterId = request.getClusterId();
        String namespaceName = request.getName();

        // Step 1: Validation
        if (!clusterAllocationRepository.existsByKubernetesClusterIdAndTenantId(clusterId, tenantId)) {
            throw new SecurityException("Access denied: Cluster with ID " + clusterId + " is not allocated to your tenant.");
        }
        if (namespaceRepository.findByNameAndKubernetesClusterId(namespaceName, clusterId).isPresent()) {
            throw new IllegalArgumentException("A namespace with the name '" + namespaceName + "' already exists in this cluster.");
        }
        KubernetesCluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException("Target cluster not found with ID: " + clusterId));

        // Step 2: Create and save the initial parent record
        TenantNamespace namespace = new TenantNamespace();
        namespace.setName(namespaceName);
        namespace.setDescription(request.getDescription());
        namespace.setTenant(tenant); // Use the fetched Tenant entity
        namespace.setKubernetesCluster(cluster);
        namespace.setStatus(NamespaceStatus.CREATING);
        TenantNamespace savedNamespace = namespaceRepository.saveAndFlush(namespace);

        try {
            // Step 3: Create the actual Namespace in Kubernetes
            logger.info("Creating namespace '{}' in cluster '{}'", namespaceName, cluster.getName());
            kubernetesClientService.createNamespace(cluster, namespaceName);

            // Step 4: Create default configurations
            createDefaultConfigurations(savedNamespace, username);

            // Step 5: Apply optional user-provided configurations
            applyOptionalConfiguration(savedNamespace, request.getResourceQuotaYaml());
            applyOptionalConfiguration(savedNamespace, request.getLimitRangeYaml());

            savedNamespace.setStatus(NamespaceStatus.ACTIVE);
            logger.info("Successfully created and configured namespace '{}'", namespaceName);

        } catch (Exception e) {
            logger.error("Failed during creation of namespace '{}' in cluster '{}'.",
                    namespaceName, cluster.getName(), e);
            savedNamespace.setStatus(NamespaceStatus.FAILED_CREATION);
            savedNamespace.setStatusDetails("Failed during resource application: " + e.getMessage());
        }

        // Step 6: Save the final state and return the mapped DTO
        return namespaceMapper.toDetailDto(namespaceRepository.save(savedNamespace));
    }

    /**
     * Retrieves a paginated list of namespaces for the authenticated tenant within a specific cluster.
     *
     * @param clusterId    The ID of the cluster to scope the search to.
     * @param searchParams Filters for 'name' and 'status'.
     * @param pageable     Pagination information.
     * @return A page of {@link TenantNamespaceSummaryDto} objects.
     */
    @Transactional(readOnly = true)
    public Page<TenantNamespaceSummaryDto> getNamespaces(Long clusterId, Map<String, String> searchParams, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // First, verify the tenant has access to this cluster to prevent unauthorized access.
        if (!clusterAllocationRepository.existsByKubernetesClusterIdAndTenantId(clusterId, tenantId)) {
            throw new SecurityException("Access denied: Cluster with ID " + clusterId + " is not allocated to your tenant.");
        }

        String nameFilter = searchParams.getOrDefault("name", "").trim();
        String statusFilterString = searchParams.getOrDefault("status", "").trim();

        NamespaceStatus statusFilter = null;
        if (StringUtils.hasText(statusFilterString)) {
            try {
                statusFilter = NamespaceStatus.valueOf(statusFilterString.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid namespace status filter provided: '{}'. Ignoring filter.", statusFilterString);
            }
        }

        Page<TenantNamespace> namespacePage;
        boolean hasNameFilter = StringUtils.hasText(nameFilter);

        // Call the new repository methods that include the clusterId
        if (hasNameFilter && statusFilter != null) {
            namespacePage = namespaceRepository.findByTenantIdAndKubernetesClusterIdAndNameContainingIgnoreCaseAndStatus(tenantId, clusterId, nameFilter, statusFilter, pageable);
        } else if (hasNameFilter) {
            namespacePage = namespaceRepository.findByTenantIdAndKubernetesClusterIdAndNameContainingIgnoreCase(tenantId, clusterId, nameFilter, pageable);
        } else if (statusFilter != null) {
            namespacePage = namespaceRepository.findByTenantIdAndKubernetesClusterIdAndStatus(tenantId, clusterId, statusFilter, pageable);
        } else {
            namespacePage = namespaceRepository.findByTenantIdAndKubernetesClusterId(tenantId, clusterId, pageable);
        }

        return namespacePage.map(namespaceMapper::toSummaryDto);
    }

    /**
     * Retrieves a single, detailed view of a namespace by its ID. It ensures the namespace
     * belongs to the authenticated tenant before returning it.
     *
     * @param namespaceId The unique identifier of the namespace.
     * @return A comprehensive DTO of the namespace, including its configurations and workloads.
     * @throws ResourceNotFoundException if no namespace with the given ID is found for the current tenant.
     */
    @Transactional(readOnly = true)
    public TenantNamespaceDto getNamespace(Long namespaceId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // The repository method enforces security by checking both tenantId and namespaceId
        TenantNamespace namespace = namespaceRepository.findByTenantIdAndId(tenantId, namespaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Namespace not found with ID: " + namespaceId + " for your tenant."));

        // The mapper converts the entity to the detailed DTO, which will trigger
        // the lazy loading of the configurations and workloads collections within the transaction.
        return namespaceMapper.toDetailDto(namespace);
    }

    private void createDefaultConfigurations(TenantNamespace namespace, String username) {
        logger.debug("Applying default configurations for namespace '{}'", namespace.getName());
        applyAndSaveConfiguration(namespace, resourceFactory.createDefaultNetworkPolicy(namespace.getName()));
        applyAndSaveConfiguration(namespace, resourceFactory.createDefaultAdminRole(namespace.getName()));
        applyAndSaveConfiguration(namespace, resourceFactory.createAdminRoleBinding(namespace.getName(), username));
    }

    private void applyOptionalConfiguration(TenantNamespace namespace, String yamlContent) {
        if (!StringUtils.hasText(yamlContent)) {
            return;
        }
        KubernetesResource resource = resourceFactory.parseYaml(yamlContent);
        logger.debug("Applying optional resource {}/{} for namespace '{}'", resource.k8sKind(), resource.k8sName(), namespace.getName());
        applyAndSaveConfiguration(namespace, resource);
    }

    private void applyAndSaveConfiguration(TenantNamespace namespace, KubernetesResource resource) {
        var config = new TenantNamespaceConfiguration();
        config.setTenantNamespace(namespace);
        config.setK8sKind(resource.k8sKind());
        config.setK8sName(resource.k8sName());
        config.setName(resource.k8sName());
        config.setYamlContent(resource.yaml());

        try {
            kubernetesClientService.apply(namespace.getKubernetesCluster(), namespace.getName(), resource.yaml());
            config.setStatus(ResourceStatus.ACTIVE);
            config.setSyncStatus(SyncStatus.IN_SYNC);
        } catch (Exception e) {
            logger.error("Failed to apply resource {}/{} in namespace '{}'", resource.k8sKind(), resource.k8sName(), namespace.getName(), e);
            config.setStatus(ResourceStatus.ERROR);
            config.setSyncStatus(SyncStatus.IN_SYNC);
            config.setStatusDetails(e.getMessage());
        }
        namespace.getConfigurations().add(config);
    }
}