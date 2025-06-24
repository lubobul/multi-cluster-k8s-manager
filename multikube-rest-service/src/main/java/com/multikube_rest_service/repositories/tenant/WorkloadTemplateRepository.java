package com.multikube_rest_service.repositories.tenant;

import com.multikube_rest_service.entities.tenant.WorkloadTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing {@link WorkloadTemplate} entities.
 */
@Repository
public interface WorkloadTemplateRepository extends JpaRepository<WorkloadTemplate, Long> {

    /**
     * Finds a paginated list of all workload templates belonging to a specific template catalog.
     *
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @param pageable          Pagination information.
     * @return A Page of workload templates.
     */
    Page<WorkloadTemplate> findByTemplateCatalogId(Long templateCatalogId, Pageable pageable);

    /**
     * Finds a paginated list of all workload templates belonging to a specific template catalog,
     * filtered by a case-insensitive name match.
     *
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @param name              The search string for the template name.
     * @param pageable          Pagination information.
     * @return A Page of matching workload templates.
     */
    Page<WorkloadTemplate> findByTemplateCatalogIdAndNameContainingIgnoreCase(Long templateCatalogId, String name, Pageable pageable);
    
    /**
     * Finds a specific template by its ID and its parent catalog's ID.
     *
     * @param id The ID of the WorkloadTemplate.
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @return An Optional containing the template if found.
     */
    Optional<WorkloadTemplate> findByIdAndTemplateCatalogId(Long id, Long templateCatalogId);

    /**
     * Checks if a workload template with a given name already exists within a specific catalog.
     * Used for validation before creating a new template.
     *
     * @param templateCatalogId The ID of the parent TemplateCatalog.
     * @param name              The name to check for.
     * @return true if a template with that name exists in the catalog, false otherwise.
     */
    boolean existsByTemplateCatalogIdAndName(Long templateCatalogId, String name);
}