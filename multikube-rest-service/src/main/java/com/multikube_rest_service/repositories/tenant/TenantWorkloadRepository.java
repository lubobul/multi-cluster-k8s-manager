package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.TenantWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link TenantWorkload} entities.
 * These represent application workloads within a namespace, like Deployments and Services.
 */
@Repository
public interface TenantWorkloadRepository extends JpaRepository<TenantWorkload, Long> {
}