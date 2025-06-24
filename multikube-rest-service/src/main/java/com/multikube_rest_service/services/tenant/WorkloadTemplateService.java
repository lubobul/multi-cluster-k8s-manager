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
import org.springframework.util.StringUtils;

import java.util.Map;

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

    @Transactional(readOnly = true)
    public Page<WorkloadTemplateSummaryDto> getTemplates(Map<String, String> searchParams, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Extract optional filters from the search params
        String templateNameFilter = searchParams.get("name");
        String catalogNameFilter = searchParams.get("catalogName");

        Page<WorkloadTemplate> templatePage = templateRepository.findAccessibleByTenantWithFilters(
                tenantId,
                StringUtils.hasText(templateNameFilter) ? templateNameFilter : null,
                StringUtils.hasText(catalogNameFilter) ? catalogNameFilter : null,
                pageable
        );

        return templatePage.map(templateMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public WorkloadTemplateDto getTemplate(Long templateId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        WorkloadTemplate template = templateRepository.findByIdAndAccessibleByTenant(templateId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload Template not found with ID: " + templateId));
        return templateMapper.toDto(template);
    }

    @Transactional
    public WorkloadTemplateDto createTemplate(CreateWorkloadTemplateRequest request) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        Long catalogId = request.getCatalogId();

        TemplateCatalog catalog = catalogRepository.findByIdAndAccessibleByTenant(catalogId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template Catalog not found with ID: " + catalogId));

        if (templateRepository.existsByTemplateCatalogIdAndName(catalogId, request.getName())) {
            throw new IllegalArgumentException("A template with the name '" + request.getName() + "' already exists in this catalog.");
        }

        WorkloadTemplate template = new WorkloadTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTemplateType(request.getTemplateType());
        template.setYamlContent(request.getYamlContent());
        template.setTemplateCatalog(catalog);

        WorkloadTemplate savedTemplate = templateRepository.save(template);
        logger.info("Created new workload template '{}' (ID: {}) in catalog ID {}", savedTemplate.getName(), savedTemplate.getId(), catalogId);

        return templateMapper.toDto(savedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        WorkloadTemplate template = templateRepository.findByIdAndAccessibleByTenant(templateId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workload Template not found with ID: " + templateId));

        // The check for workload instances is removed, as per our final "copy-on-create" design.

        templateRepository.delete(template);
        logger.info("Deleted workload template '{}' (ID: {})", template.getName(), templateId);
    }
}