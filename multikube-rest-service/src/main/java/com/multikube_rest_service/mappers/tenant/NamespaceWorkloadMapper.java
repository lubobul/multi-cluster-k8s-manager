package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.NamespaceWorkloadDto;
import com.multikube_rest_service.entities.tenant.TenantWorkload;
import org.mapstruct.Mapper;

/**
 * Maps {@link TenantWorkload} entities to {@link NamespaceWorkloadDto} objects.
 */
@Mapper(componentModel = "spring")
public interface NamespaceWorkloadMapper {
    NamespaceWorkloadDto toDto(TenantWorkload entity);
}