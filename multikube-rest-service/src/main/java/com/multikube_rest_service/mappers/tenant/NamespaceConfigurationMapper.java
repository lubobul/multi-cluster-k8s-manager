package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationDto;
import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationSummaryDto;
import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import org.mapstruct.Mapper;

/**
 * Maps {@link TenantNamespaceConfiguration} entities to their corresponding
 * detailed ({@link NamespaceConfigurationDto}) and summary ({@link NamespaceConfigurationSummaryDto}) DTOs.
 */
@Mapper(componentModel = "spring")
public interface NamespaceConfigurationMapper {

    /**
     * Maps to the detailed DTO, including yamlContent.
     */
    NamespaceConfigurationDto toDto(TenantNamespaceConfiguration entity);

    /**
     * Maps to the summary DTO, omitting yamlContent.
     */
    NamespaceConfigurationSummaryDto toSummaryDto(TenantNamespaceConfiguration entity);
}