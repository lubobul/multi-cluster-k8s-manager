package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link KubernetesCluster} entities.
 */
@Repository
public interface TenantKubernetesClusterRepository extends JpaRepository<KubernetesCluster, Long> {

    /**
     * Finds a single cluster by its ID, but only if it's allocated to the specified tenant.
     * @param tenantId The ID of the tenant.
     * @param clusterId The ID of the cluster.
     * @return An Optional containing the cluster if found and allocated, otherwise empty.
     */
    @Query("SELECT kc FROM KubernetesCluster kc JOIN ClusterAllocation ca ON kc.id = ca.kubernetesCluster.id WHERE ca.tenant.id = :tenantId AND kc.id = :clusterId")
    Optional<KubernetesCluster> findByTenantIdAndId(@Param("tenantId") Long tenantId, @Param("clusterId") Long clusterId);

    /**
     * Finds all clusters allocated to the specified tenant.
     * @param tenantId The ID of the tenant.
     * @param pageable Pagination information.
     * @return A page of clusters.
     */
    @Query("SELECT kc FROM KubernetesCluster kc JOIN ClusterAllocation ca ON kc.id = ca.kubernetesCluster.id WHERE ca.tenant.id = :tenantId")
    Page<KubernetesCluster> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds all clusters allocated to the specified tenant with a name containing the given string (case-insensitive).
     * @param tenantId The ID of the tenant.
     * @param nameFilter The string to filter cluster names by.
     * @param pageable Pagination information.
     * @return A page of clusters.
     */
    @Query("SELECT kc FROM KubernetesCluster kc JOIN ClusterAllocation ca ON kc.id = ca.kubernetesCluster.id WHERE ca.tenant.id = :tenantId AND lower(kc.name) LIKE lower(concat('%', :nameFilter, '%'))")
    Page<KubernetesCluster> findByTenantIdAndNameContainingIgnoreCase(@Param("tenantId") Long tenantId, @Param("nameFilter") String nameFilter, Pageable pageable);

    /**
     * Finds all clusters allocated to the specified tenant with a given status.
     * @param tenantId The ID of the tenant.
     * @param statusFilter The status to filter by.
     * @param pageable Pagination information.
     * @return A page of clusters.
     */
    @Query("SELECT kc FROM KubernetesCluster kc JOIN ClusterAllocation ca ON kc.id = ca.kubernetesCluster.id WHERE ca.tenant.id = :tenantId AND kc.status = :statusFilter")
    Page<KubernetesCluster> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("statusFilter") ClusterStatus statusFilter, Pageable pageable);

    /**
     * Finds all clusters allocated to the specified tenant with a name containing the given string and a given status.
     * @param tenantId The ID of the tenant.
     * @param nameFilter The string to filter cluster names by.
     * @param statusFilter The status to filter by.
     * @param pageable Pagination information.
     * @return A page of clusters.
     */
    @Query("SELECT kc FROM KubernetesCluster kc JOIN ClusterAllocation ca ON kc.id = ca.kubernetesCluster.id WHERE ca.tenant.id = :tenantId AND lower(kc.name) LIKE lower(concat('%', :nameFilter, '%')) AND kc.status = :statusFilter")
    Page<KubernetesCluster> findByTenantIdAndNameContainingIgnoreCaseAndStatus(@Param("tenantId") Long tenantId, @Param("nameFilter") String nameFilter, @Param("statusFilter") ClusterStatus statusFilter, Pageable pageable);
}