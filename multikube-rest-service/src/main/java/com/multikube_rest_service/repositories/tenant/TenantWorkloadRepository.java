package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.TenantWorkload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing {@link TenantWorkload} entities.
 */
@Repository
public interface TenantWorkloadRepository extends JpaRepository<TenantWorkload, Long> {

    /**
     * Finds a paginated list of all workloads deployed in a specific tenant namespace.
     *
     * @param tenantNamespaceId The ID of the parent TenantNamespace.
     * @param pageable          Pagination information.
     * @return A Page of workloads.
     */
    Page<TenantWorkload> findByTenantNamespaceId(Long tenantNamespaceId, Pageable pageable);

    /**
     * Finds a paginated list of all workloads in a specific tenant namespace,
     * filtered by a case-insensitive name match.
     *
     * @param tenantNamespaceId The ID of the parent TenantNamespace.
     * @param name              The search string for the workload's UI name.
     * @param pageable          Pagination information.
     * @return A Page of matching workloads.
     */
    Page<TenantWorkload> findByTenantNamespaceIdAndNameContainingIgnoreCase(Long tenantNamespaceId, String name, Pageable pageable);

    /**
     * Finds a specific workload by its ID and its parent namespace's ID.
     * This is used to securely fetch a single workload.
     *
     * @param id The ID of the TenantWorkload.
     * @param tenantNamespaceId The ID of the parent TenantNamespace.
     * @return An Optional containing the workload if found.
     */
    Optional<TenantWorkload> findByIdAndTenantNamespaceId(Long id, Long tenantNamespaceId);
    
    /**
     * Checks if a workload with a given Kubernetes resource name and kind already exists
     * within a specific namespace. Used for validation before creating a new workload.
     *
     * @param tenantNamespaceId The ID of the parent TenantNamespace.
     * @param k8sName           The Kubernetes metadata.name to check for.
     * @param k8sKind           The Kubernetes kind to check for.
     * @return true if a workload with that name and kind exists in the namespace, false otherwise.
     */
    boolean existsByTenantNamespaceIdAndK8sNameAndK8sKind(Long tenantNamespaceId, String k8sName, String k8sKind);
}