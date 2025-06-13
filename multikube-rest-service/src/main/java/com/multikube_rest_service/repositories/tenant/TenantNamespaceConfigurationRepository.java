package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link TenantNamespaceConfiguration} entities.
 * These represent administrative resources within a namespace, like ResourceQuotas and NetworkPolicies.
 */
@Repository
public interface TenantNamespaceConfigurationRepository extends JpaRepository<TenantNamespaceConfiguration, Long> {
}