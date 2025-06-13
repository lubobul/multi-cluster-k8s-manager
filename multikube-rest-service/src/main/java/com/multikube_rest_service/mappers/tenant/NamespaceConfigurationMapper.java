package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationDto;
import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import org.mapstruct.Mapper;

/**
 * Maps {@link TenantNamespaceConfiguration} entities to {@link NamespaceConfigurationDto} objects.
 */
@Mapper(componentModel = "spring")
public interface NamespaceConfigurationMapper {
    NamespaceConfigurationDto toDto(TenantNamespaceConfiguration entity);
}