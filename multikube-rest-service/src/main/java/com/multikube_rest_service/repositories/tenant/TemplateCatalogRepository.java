package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.TemplateCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing {@link TemplateCatalog} entities.
 */
@Repository
public interface TemplateCatalogRepository extends JpaRepository<TemplateCatalog, Long> {

    /**
     * Finds a paginated list of all catalogs that are either system-wide defaults (tenantId is null)
     * or belong to the specified tenant.
     *
     * @param tenantId The ID of the current tenant.
     * @param pageable Pagination information.
     * @return A Page of accessible template catalogs.
     */
    @Query("SELECT tc FROM TemplateCatalog tc WHERE tc.tenant.id = :tenantId OR tc.tenant.id IS NULL")
    Page<TemplateCatalog> findByTenantIdOrSystemDefault(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds a paginated list of catalogs that belong exclusively to the specified tenant.
     *
     * @param tenantId The ID of the tenant.
     * @param pageable Pagination information.
     * @return A Page of the tenant's private template catalogs.
     */
    Page<TemplateCatalog> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds a catalog by its ID, ensuring it is either a system-default catalog or owned by the specified tenant.
     *
     * @param id The ID of the catalog.
     * @param tenantId The ID of the current tenant.
     * @return An Optional containing the TemplateCatalog if found and accessible.
     */
    @Query("SELECT tc FROM TemplateCatalog tc WHERE tc.id = :id AND (tc.tenant.id = :tenantId OR tc.tenant.id IS NULL)")
    Optional<TemplateCatalog> findByIdAndAccessibleByTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);


    /**
     * Checks if a catalog with a given name already exists for a specific tenant.
     * Used for validation before creating a new tenant-specific catalog.
     *
     * @param tenantId The ID of the tenant.
     * @param name     The name to check for.
     * @return true if a catalog with that name exists for the tenant, false otherwise.
     */
    boolean existsByTenantIdAndName(Long tenantId, String name);
}