package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.common.enums.NamespaceStatus;
import com.multikube_rest_service.entities.tenant.TenantNamespace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantNamespaceRepository extends JpaRepository<TenantNamespace, Long> {

    /**
     * Checks if any namespace records exist for a given Kubernetes cluster ID.
     * This is used to validate if a cluster has active resources before de-allocation.
     *
     * @param kubernetesClusterId The ID of the Kubernetes cluster.
     * @return true if any namespaces exist for the cluster, false otherwise.
     */
    boolean existsByKubernetesClusterId(Long kubernetesClusterId);

    /**
     * Finds a namespace by its name and the ID of the cluster it belongs to.
     * Used to check for duplicate names within a cluster before creation.
     *
     * @param name The name of the namespace.
     * @param clusterId The ID of the Kubernetes cluster.
     * @return An Optional containing the TenantNamespace if found.
     */
    Optional<TenantNamespace> findByNameAndKubernetesClusterId(String name, Long clusterId);

    /**
     * Finds a namespace by its ID, but only if it belongs to the specified tenant.
     * Ensures a tenant can only retrieve their own namespaces.
     *
     * @param tenantId The ID of the current tenant.
     * @param id The ID of the namespace to find.
     * @return An Optional containing the TenantNamespace if found and owned by the tenant.
     */
    Optional<TenantNamespace> findByTenantIdAndId(Long tenantId, Long id);

    /**
     * Finds all namespaces for a tenant within a specific cluster.
     */
    Page<TenantNamespace> findByTenantIdAndKubernetesClusterId(Long tenantId, Long clusterId, Pageable pageable);

    /**
     * Finds namespaces for a tenant within a specific cluster, filtered by name.
     */
    Page<TenantNamespace> findByTenantIdAndKubernetesClusterIdAndNameContainingIgnoreCase(Long tenantId, Long clusterId, String name, Pageable pageable);

    /**
     * Finds namespaces for a tenant within a specific cluster, filtered by status.
     */
    Page<TenantNamespace> findByTenantIdAndKubernetesClusterIdAndStatus(Long tenantId, Long clusterId, NamespaceStatus status, Pageable pageable);

    /**
     * Finds namespaces for a tenant within a specific cluster, filtered by both name and status.
     */
    Page<TenantNamespace> findByTenantIdAndKubernetesClusterIdAndNameContainingIgnoreCaseAndStatus(Long tenantId, Long clusterId, String name, NamespaceStatus status, Pageable pageable);
    /**
     * Checks if a namespace exists with a given ID and belongs to a specific tenant.
     * This is a more efficient way to perform security checks than fetching the full entity.
     *
     * @param id The ID of the namespace.
     * @param tenantId The ID of the tenant.
     * @return true if a matching namespace exists, false otherwise.
     */
    boolean existsByIdAndTenantId(Long id, Long tenantId);
}