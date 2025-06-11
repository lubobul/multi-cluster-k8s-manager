package com.multikube_rest_service.repositories.provider;

import com.multikube_rest_service.entities.tenant.TenantNamespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}