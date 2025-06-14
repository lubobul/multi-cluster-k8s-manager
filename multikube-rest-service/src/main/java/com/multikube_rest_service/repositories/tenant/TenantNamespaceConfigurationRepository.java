package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link TenantNamespaceConfiguration} entities.
 */
@Repository
public interface TenantNamespaceConfigurationRepository extends JpaRepository<TenantNamespaceConfiguration, Long> {

    /**
     * Finds a paginated list of all configurations belonging to a specific tenant namespace.
     * @param tenantNamespaceId The ID of the parent TenantNamespace.
     * @param pageable Pagination information.
     * @return A Page of namespace configurations.
     */
    Page<TenantNamespaceConfiguration> findByTenantNamespaceId(Long tenantNamespaceId, Pageable pageable);
}