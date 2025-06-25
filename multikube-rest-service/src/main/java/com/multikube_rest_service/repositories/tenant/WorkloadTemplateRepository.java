package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.WorkloadTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkloadTemplateRepository extends JpaRepository<WorkloadTemplate, Long> {

    /**
     * Finds a single template by its ID, ensuring it belongs to a catalog accessible by the tenant.
     *
     * @param templateId The ID of the template.
     * @param tenantId   The ID of the current tenant.
     * @return An Optional containing the template if found and accessible.
     */
    @Query("SELECT wt FROM WorkloadTemplate wt JOIN wt.templateCatalog tc WHERE wt.id = :templateId AND (tc.tenant.id = :tenantId OR tc.tenant.id IS NULL)")
    Optional<WorkloadTemplate> findByIdAndAccessibleByTenant(@Param("templateId") Long templateId, @Param("tenantId") Long tenantId);

    /**
     * Finds a paginated list of all templates accessible by a tenant, with optional filters
     * for the template name and the name of its parent catalog.
     *
     * @param tenantId     The ID of the current tenant.
     * @param templateName An optional, case-insensitive filter for the workload template's name.
     * @param catalogName  An optional, case-insensitive filter for the parent catalog's name.
     * @param pageable     Pagination information.
     * @return A Page of accessible workload templates that match the filters.
     */
    @Query("SELECT wt FROM WorkloadTemplate wt JOIN wt.templateCatalog tc " +
            "WHERE (tc.tenant.id = :tenantId OR tc.tenant.id IS NULL) " +
            "AND (:templateName IS NULL OR LOWER(wt.name) LIKE LOWER(CONCAT('%', :templateName, '%'))) " +
            "AND (:catalogName IS NULL OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :catalogName, '%')))")
    Page<WorkloadTemplate> findAccessibleByTenantWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("templateName") String templateName,
            @Param("catalogName") String catalogName,
            Pageable pageable
    );

    /**
     * Checks if a workload template with a given name already exists within a specific catalog.
     *
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @param name              The name to check for.
     * @return true if a template with that name exists in the catalog, false otherwise.
     */
    boolean existsByTemplateCatalogIdAndName(Long templateCatalogId, String name);

    /**
     * Finds a paginated list of all workload templates belonging to a specific template catalog.
     *
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @param pageable          Pagination information.
     * @return A Page of workload templates.
     */
    Page<WorkloadTemplate> findByTemplateCatalogId(Long templateCatalogId, Pageable pageable);
}