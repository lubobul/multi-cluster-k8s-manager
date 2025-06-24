package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.dtos.requests.tenant.CreateTemplateCatalogRequest;
import com.multikube_rest_service.dtos.responses.tenant.TemplateCatalogDto;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.tenant.TemplateCatalog;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.TemplateCatalogMapper;
import com.multikube_rest_service.repositories.TenantRepository;
import com.multikube_rest_service.repositories.tenant.TemplateCatalogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing Workload Template Catalogs.
 */
@Service
public class TemplateCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateCatalogService.class);

    private final TemplateCatalogRepository catalogRepository;
    private final TenantRepository tenantRepository;
    private final TemplateCatalogMapper catalogMapper;

    public TemplateCatalogService(
            TemplateCatalogRepository catalogRepository,
            TenantRepository tenantRepository,
            TemplateCatalogMapper catalogMapper) {
        this.catalogRepository = catalogRepository;
        this.tenantRepository = tenantRepository;
        this.catalogMapper = catalogMapper;
    }

    /**
     * Retrieves a paginated list of all template catalogs accessible to the current tenant.
     * This includes both system-default catalogs and catalogs owned by the tenant.
     *
     * @param pageable Pagination information.
     * @return A Page of TemplateCatalogDto objects.
     */
    @Transactional(readOnly = true)
    public Page<TemplateCatalogDto> getCatalogs(Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        Page<TemplateCatalog> catalogPage = catalogRepository.findByTenantIdOrSystemDefault(tenantId, pageable);
        return catalogPage.map(catalogMapper::toDto);
    }

    /**
     * Retrieves a single template catalog by its ID, ensuring the current tenant has access to it.
     *
     * @param catalogId The ID of the catalog to retrieve.
     * @return A DTO representation of the catalog.
     * @throws ResourceNotFoundException if the catalog is not found or not accessible by the tenant.
     */
    @Transactional(readOnly = true)
    public TemplateCatalogDto getCatalog(Long catalogId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        TemplateCatalog catalog = catalogRepository.findByIdAndAccessibleByTenant(catalogId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId));
        return catalogMapper.toDto(catalog);
    }

    /**
     * Creates a new template catalog for the currently authenticated tenant.
     *
     * @param request The request DTO containing the name and description.
     * @return A DTO representation of the newly created catalog.
     * @throws IllegalArgumentException if a catalog with the same name already exists for the tenant.
     */
    @Transactional
    public TemplateCatalogDto createCatalog(CreateTemplateCatalogRequest request) {
        JwtUserDetails userDetails = SecurityContextHelper.getAuthenticatedUser();
        Long tenantId = userDetails.getTenantId();

        if (catalogRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new IllegalArgumentException("A catalog with the name '" + request.getName() + "' already exists for your tenant.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user's tenant with ID " + tenantId + " not found."));

        TemplateCatalog catalog = new TemplateCatalog();
        catalog.setName(request.getName());
        catalog.setDescription(request.getDescription());
        catalog.setTenant(tenant);

        TemplateCatalog savedCatalog = catalogRepository.save(catalog);
        logger.info("Created new template catalog '{}' (ID: {}) for tenant ID {}", savedCatalog.getName(), savedCatalog.getId(), tenantId);

        return catalogMapper.toDto(savedCatalog);
    }

    /**
     * Deletes a template catalog, ensuring it is owned by the tenant and is empty.
     * System-default catalogs cannot be deleted.
     *
     * @param catalogId The ID of the catalog to delete.
     * @throws ResourceNotFoundException if the catalog is not found or not owned by the tenant.
     * @throws SecurityException if the user attempts to delete a system-default catalog.
     * @throws IllegalStateException if the catalog is not empty.
     */
    @Transactional
    public void deleteCatalog(Long catalogId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        TemplateCatalog catalog = catalogRepository.findByIdAndAccessibleByTenant(catalogId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId));

        // Security check: Prevent deletion of system-default catalogs.
        if (catalog.getTenant() == null || !catalog.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Cannot delete a system-default catalog or a catalog you do not own.");
        }

        // Integrity check: Prevent deletion of non-empty catalogs.
        if (!catalog.getWorkloadTemplates().isEmpty()) {
            throw new IllegalStateException("Cannot delete catalog: It is not empty. Please delete all templates from the catalog first.");
        }

        catalogRepository.delete(catalog);
        logger.info("Deleted template catalog '{}' (ID: {}) for tenant ID {}", catalog.getName(), catalogId, tenantId);
    }
}