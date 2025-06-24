package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.dtos.requests.tenant.CreateWorkloadTemplateRequest;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateDto;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateSummaryDto;
import com.multikube_rest_service.entities.tenant.TemplateCatalog;
import com.multikube_rest_service.entities.tenant.WorkloadTemplate;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.WorkloadTemplateMapper;
import com.multikube_rest_service.repositories.tenant.TemplateCatalogRepository;
import com.multikube_rest_service.repositories.tenant.WorkloadTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing Workload Templates within a Template Catalog.
 */
@Service
public class WorkloadTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadTemplateService.class);

    private final WorkloadTemplateRepository templateRepository;
    private final TemplateCatalogRepository catalogRepository;
    private final WorkloadTemplateMapper templateMapper;

    public WorkloadTemplateService(
            WorkloadTemplateRepository templateRepository,
            TemplateCatalogRepository catalogRepository,
            WorkloadTemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.catalogRepository = catalogRepository;
        this.templateMapper = templateMapper;
    }

    /**
     * Retrieves a paginated list of all workload template summaries for a given catalog.
     * Ensures the current tenant has access to the parent catalog.
     *
     * @param catalogId The ID of the parent template catalog.
     * @param pageable  Pagination information.
     * @return A Page of WorkloadTemplateSummaryDto objects.
     */
    @Transactional(readOnly = true)
    public Page<WorkloadTemplateSummaryDto> getTemplates(Long catalogId, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Security Check using the corrected method names from the repository
        boolean isTenantCatalog = catalogRepository.existsByIdAndTenantId(catalogId, tenantId);
        boolean isSystemCatalog = catalogRepository.existsByIdAndTenantIsNull(catalogId);

        if (!isTenantCatalog && !isSystemCatalog) {
            throw new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId);
        }

        Page<WorkloadTemplate> templatePage = templateRepository.findByTemplateCatalogId(catalogId, pageable);
        return templatePage.map(templateMapper::toSummaryDto);
    }

    /**
     * Retrieves a single, detailed workload template by its ID.
     *
     * @param catalogId  The ID of the parent catalog.
     * @param templateId The ID of the template to retrieve.
     * @return A DTO of the template, including its YAML content.
     */
    @Transactional(readOnly = true)
    public WorkloadTemplateDto getTemplate(Long catalogId, Long templateId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Security Check using the corrected method names from the repository
        boolean isTenantCatalog = catalogRepository.existsByIdAndTenantId(catalogId, tenantId);
        boolean isSystemCatalog = catalogRepository.existsByIdAndTenantIsNull(catalogId);

        if (!isTenantCatalog && !isSystemCatalog) {
            throw new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId);
        }

        WorkloadTemplate template = templateRepository.findByIdAndTemplateCatalogId(templateId, catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload Template not found with ID: " + templateId));

        return templateMapper.toDto(template);
    }

    /**
     * Creates a new workload template within a specified catalog.
     *
     * @param catalogId The ID of the catalog to add the template to.
     * @param request   The request DTO containing the template details.
     * @return A DTO of the newly created template.
     */
    @Transactional
    public WorkloadTemplateDto createTemplate(Long catalogId, CreateWorkloadTemplateRequest request) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        TemplateCatalog catalog = catalogRepository.findByIdAndAccessibleByTenant(catalogId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId));

        // Integrity Check: Prevent duplicate template names within the same catalog.
        if (templateRepository.existsByTemplateCatalogIdAndName(catalogId, request.getName())) {
            throw new IllegalArgumentException("A template with the name '" + request.getName() + "' already exists in this catalog.");
        }

        // TODO: Add YAML validation logic here before saving.

        WorkloadTemplate template = new WorkloadTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTemplateType(request.getTemplateType());
        template.setYamlContent(request.getYamlContent());
        template.setTemplateCatalog(catalog);

        WorkloadTemplate savedTemplate = templateRepository.save(template);
        logger.info("Created new workload template '{}' (ID: {}) in catalog ID {}",
                savedTemplate.getName(), savedTemplate.getId(), catalogId);

        return templateMapper.toDto(savedTemplate);
    }

    /**
     * Deletes a workload template.
     * In the "copy-on-create" model, deleting a template is a safe operation
     * and will not affect any workloads that were previously created from it.
     *
     * @param catalogId  The ID of the parent catalog.
     * @param templateId The ID of the template to delete.
     */
    @Transactional
    public void deleteTemplate(Long catalogId, Long templateId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Security Check: Ensure the user has access to the parent catalog.
        boolean isTenantCatalog = catalogRepository.existsByIdAndTenantId(catalogId, tenantId);
        boolean isSystemCatalog = catalogRepository.existsByIdAndTenantIsNull(catalogId);

        if (!isTenantCatalog && !isSystemCatalog) {
            throw new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId);
        }

        WorkloadTemplate template = templateRepository.findByIdAndTemplateCatalogId(templateId, catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload Template not found with ID: " + templateId));
        
        templateRepository.delete(template);
        logger.info("Deleted workload template '{}' (ID: {}) from catalog ID {}",
                template.getName(), templateId, catalogId);
    }
}