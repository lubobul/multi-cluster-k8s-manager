package com.multikube_rest_service.repositories.provider;

import com.multikube_rest_service.entities.provider.ClusterAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterAllocationRepository extends JpaRepository<ClusterAllocation, Long> {
    boolean existsByKubernetesClusterId(Long kubernetesClusterId);
    List<ClusterAllocation> findByTenantIdIn(List<Long> tenantIds);
    Optional<ClusterAllocation> findByKubernetesClusterId(Long kubernetesClusterId);
    long deleteByKubernetesClusterId(Long kubernetesClusterId);
    List<ClusterAllocation> findByKubernetesClusterIdIn(List<Long> kubernetesClusterIds);
    boolean existsByKubernetesClusterIdAndTenantId(Long kubernetesClusterId, Long tenantId);
}