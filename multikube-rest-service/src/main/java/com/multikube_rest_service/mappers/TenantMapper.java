package com.multikube_rest_service.mappers;

import com.multikube_rest_service.dtos.responses.TenantDto;
import com.multikube_rest_service.entities.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TenantMapper extends GenericMapper<Tenant, TenantDto> {

    @Override
    @Mapping(target = "createdAt", source = "createdAt") // Explicit mapping if needed
    @Mapping(target = "updatedAt", source = "updatedAt") // Explicit mapping if needed
    @Mapping(target = "allocatedClusterIds", ignore = true)
    TenantDto toDto(Tenant tenant);

    @Override
    @Mapping(target = "createdAt", source = "createdAt") // Explicit mapping if needed
    @Mapping(target = "updatedAt", source = "updatedAt") // Explicit mapping if needed
    Tenant toEntity(TenantDto tenantDto);
}