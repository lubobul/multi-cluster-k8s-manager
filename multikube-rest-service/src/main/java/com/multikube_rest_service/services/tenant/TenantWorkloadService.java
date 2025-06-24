package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.dtos.requests.tenant.CreateWorkloadRequest;
import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadSummaryDto;
import com.multikube_rest_service.entities.User;
import com.multikube_rest_service.entities.tenant.TenantNamespace;
import com.multikube_rest_service.entities.tenant.TenantWorkload;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.TenantWorkloadMapper;
import com.multikube_rest_service.repositories.UserRepository;
import com.multikube_rest_service.repositories.tenant.TenantNamespaceRepository;
import com.multikube_rest_service.repositories.tenant.TenantWorkloadRepository;
import com.multikube_rest_service.services.kubernetes.KubernetesClientService;
import com.multikube_rest_service.services.kubernetes.factories.KubernetesResource;
import com.multikube_rest_service.services.kubernetes.factories.KubernetesResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing Tenant Workloads within a Namespace,
 * enforcing role-based permissions for all CRUD operations.
 */
@Service
public class TenantWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TenantWorkloadService.class);

    private final TenantWorkloadRepository workloadRepository;
    private final TenantNamespaceRepository namespaceRepository;
    private final UserRepository userRepository;
    private final TenantWorkloadMapper workloadMapper;
    private final KubernetesResourceFactory resourceFactory;
    private final KubernetesClientService kubernetesClientService;

    public TenantWorkloadService(
            TenantWorkloadRepository workloadRepository,
            TenantNamespaceRepository namespaceRepository,
            UserRepository userRepository,
            TenantWorkloadMapper workloadMapper,
            KubernetesResourceFactory resourceFactory,
            KubernetesClientService kubernetesClientService) {
        this.workloadRepository = workloadRepository;
        this.namespaceRepository = namespaceRepository;
        this.userRepository = userRepository;
        this.workloadMapper = workloadMapper;
        this.resourceFactory = resourceFactory;
        this.kubernetesClientService = kubernetesClientService;
    }

    /**
     * Retrieves a paginated list of all workload summaries for a given namespace.
     * Accessible by any tenant user who has access to the namespace.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param pageable    Pagination information.
     * @return A Page of Workload summaries.
     */
    @Transactional(readOnly = true)
    public Page<TenantWorkloadSummaryDto> getWorkloads(Long namespaceId, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        // Security Check: Ensure the user has access to the parent namespace.
        if (!namespaceRepository.existsByIdAndTenantId(namespaceId, tenantId)) {
            throw new ResourceNotFoundException("Namespace not found with ID: " + namespaceId);
        }

        Page<TenantWorkload> workloadPage = workloadRepository.findByTenantNamespaceId(namespaceId, pageable);
        return workloadPage.map(workloadMapper::toSummaryDto);
    }

    /**
     * Retrieves a single, detailed workload by its ID.
     * Accessible by any tenant user who has access to the namespace.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param workloadId  The ID of the workload to retrieve.
     * @return A detailed DTO of the workload, including its YAML content.
     */
    @Transactional(readOnly = true)
    public TenantWorkloadDto getWorkload(Long namespaceId, Long workloadId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        // Security Check: Ensure access to the parent namespace.
        if (!namespaceRepository.existsByIdAndTenantId(namespaceId, tenantId)) {
            throw new ResourceNotFoundException("Namespace not found with ID: " + namespaceId);
        }

        TenantWorkload workload = workloadRepository.findByIdAndTenantNamespaceId(workloadId, namespaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload not found with ID: " + workloadId));

        return workloadMapper.toDto(workload);
    }

    /**
     * Creates a new workload instance in a namespace from a YAML manifest.
     * Associates the created workload with the current user.
     *
     * @param namespaceId The ID of the namespace to deploy the workload into.
     * @param request     The request DTO containing the workload details and final YAML.
     * @return A DTO of the newly created workload record.
     */
    @Transactional
    public TenantWorkloadDto createWorkload(Long namespaceId, CreateWorkloadRequest request) {
        JwtUserDetails userDetails = SecurityContextHelper.getAuthenticatedUser();
        Long tenantId = userDetails.getTenantId();
        User creator = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        TenantNamespace namespace = namespaceRepository.findByTenantIdAndId(tenantId, namespaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Namespace not found with ID: " + namespaceId));

        KubernetesResource resource = resourceFactory.parseYaml(request.getYamlContent());

        if (workloadRepository.existsByTenantNamespaceIdAndK8sNameAndK8sKind(namespaceId, resource.k8sName(), resource.k8sKind())) {
            throw new IllegalArgumentException("A resource with name '" + resource.k8sName() + "' and kind '" + resource.k8sKind() + "' already exists in this namespace.");
        }

        TenantWorkload workload = new TenantWorkload();
        workload.setName(request.getName());
        workload.setK8sName(resource.k8sName());
        workload.setK8sKind(resource.k8sKind());
        workload.setYamlContent(request.getYamlContent());
        workload.setTenantNamespace(namespace);
        workload.setCreatedByUser(creator); // Track who created the workload
        workload.setStatus(ResourceStatus.PROCESSING);

        try {
            logger.info("Applying workload {}/{} in namespace '{}'", resource.k8sKind(), resource.k8sName(), namespace.getName());
            kubernetesClientService.apply(namespace.getKubernetesCluster(), namespace.getName(), request.getYamlContent());
            workload.setStatus(ResourceStatus.ACTIVE);
        } catch (Exception e) {
            logger.error("Failed to apply workload {}/{} in namespace '{}'", resource.k8sKind(), resource.k8sName(), namespace.getName(), e);
            workload.setStatus(ResourceStatus.ERROR);
            workload.setStatusDetails(e.getMessage());
        }

        TenantWorkload savedWorkload = workloadRepository.save(workload);
        return workloadMapper.toDto(savedWorkload);
    }

    /**
     * Deletes a workload.
     * A TENANT_ADMIN can delete any workload in their tenant's namespaces.
     * A TENANT_USER can only delete workloads that they created.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param workloadId  The ID of the workload to delete.
     */
    @Transactional
    public void deleteWorkload(Long namespaceId, Long workloadId) {
        JwtUserDetails userDetails = SecurityContextHelper.getAuthenticatedUser();
        Long tenantId = userDetails.getTenantId();

        // Step 1: Fetch the workload and ensure it's in a namespace the user has access to.
        TenantWorkload workload = workloadRepository.findByIdAndTenantNamespaceId(workloadId, namespaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload not found with ID: " + workloadId + " in namespace " + namespaceId));

        // This check is implicitly handled by the query above, which scopes to the namespace.
        if (!workload.getTenantNamespace().getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Access denied.");
        }

        // Step 2: Granular Permission Check
        boolean isTenantAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TENANT_ADMIN"));

        boolean isCreator = workload.getCreatedByUser() != null &&
                workload.getCreatedByUser().getId().equals(userDetails.getUserId());

        if (!isTenantAdmin && !isCreator) {
            throw new SecurityException("Access denied: You must be an admin or the creator to delete this workload.");
        }

        // Step 3: Proceed with deletion logic
        // This would involve calling kubernetesClientService.delete(...)
        // For now, we will just delete the record from our database.

        workloadRepository.delete(workload);
        logger.info("User '{}' deleted workload '{}' (ID: {})", userDetails.getUsername(), workload.getName(), workloadId);
    }
}