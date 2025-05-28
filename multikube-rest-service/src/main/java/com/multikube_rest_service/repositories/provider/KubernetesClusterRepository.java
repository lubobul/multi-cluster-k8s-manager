package com.multikube_rest_service.repositories.provider;

import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KubernetesClusterRepository extends JpaRepository<KubernetesCluster, Long> {

    /**
     * Finds a cluster by its unique name.
     *
     * @param name The name of the cluster.
     * @return An Optional containing the cluster if found.
     */
    Optional<KubernetesCluster> findByName(String name);

    /**
     * Finds a cluster by its ID and the ID of the provider user who registered it.
     *
     * @param clusterId The ID of the cluster.
     * @param providerUserId The ID of the provider user.
     * @return An Optional containing the cluster if found and owned by the provider.
     */
    Optional<KubernetesCluster> findByIdAndProviderUser_Id(Long clusterId, Long providerUserId);

    /**
     * Finds all clusters registered by a specific provider user, with pagination.
     *
     * @param providerUserId The ID of the provider user.
     * @param pageable Pagination information.
     * @return A page of KubernetesCluster entities.
     */
    Page<KubernetesCluster> findByProviderUser_Id(Long providerUserId, Pageable pageable);

    /**
     * Finds clusters registered by a specific provider user where the name contains the given filter string (case-insensitive).
     *
     * @param providerUserId The ID of the provider user.
     * @param nameFilter The string to search for in cluster names.
     * @param pageable Pagination information.
     * @return A page of KubernetesCluster entities.
     */
    Page<KubernetesCluster> findByProviderUser_IdAndNameContainingIgnoreCase(Long providerUserId, String nameFilter, Pageable pageable);

    /**
     * Finds clusters registered by a specific provider user with a specific status.
     *
     * @param providerUserId The ID of the provider user.
     * @param status The status to filter by.
     * @param pageable Pagination information.
     * @return A page of KubernetesCluster entities.
     */
    Page<KubernetesCluster> findByProviderUser_IdAndStatus(Long providerUserId, ClusterStatus status, Pageable pageable);

    /**
     * Finds clusters registered by a specific provider user where the name contains the given filter string (case-insensitive)
     * AND have a specific status.
     *
     * @param providerUserId The ID of the provider user.
     * @param nameFilter The string to search for in cluster names.
     * @param status The status to filter by.
     * @param pageable Pagination information.
     * @return A page of KubernetesCluster entities.
     */
    Page<KubernetesCluster> findByProviderUser_IdAndNameContainingIgnoreCaseAndStatus(Long providerUserId, String nameFilter, ClusterStatus status, Pageable pageable);
}