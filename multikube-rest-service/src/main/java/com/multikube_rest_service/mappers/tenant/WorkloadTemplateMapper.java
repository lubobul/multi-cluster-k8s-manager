package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateDto;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateSummaryDto;
import com.multikube_rest_service.entities.tenant.WorkloadTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link WorkloadTemplate} entities to their corresponding DTOs,
 * including nested information about the parent catalog.
 */
@Mapper(componentModel = "spring")
public interface WorkloadTemplateMapper {

    /**
     * Maps to the detailed DTO, including the full yamlContent.
     * @param entity The source WorkloadTemplate entity.
     * @return The detailed WorkloadTemplateDto.
     */
    @Mapping(source = "templateCatalog", target = "catalog")
    WorkloadTemplateDto toDto(WorkloadTemplate entity);

    /**
     * Maps to the summary DTO, omitting the yamlContent.
     * @param entity The source WorkloadTemplate entity.
     * @return The lightweight WorkloadTemplateSummaryDto.
     */
    @Mapping(source = "templateCatalog", target = "catalog")
    WorkloadTemplateSummaryDto toSummaryDto(WorkloadTemplate entity);
}