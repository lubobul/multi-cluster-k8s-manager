package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.common.enums.NamespaceStatus;
import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.dtos.requests.tenant.CreateNamespaceRequest;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        } catch (Exception e) {
            logger.error("Failed to apply resource {}/{} in namespace '{}'", resource.k8sKind(), resource.k8sName(), namespace.getName(), e);
            config.setStatus(ResourceStatus.ERROR);
            config.setStatusDetails(e.getMessage());
        }
        namespace.getConfigurations().add(config);
    }
}