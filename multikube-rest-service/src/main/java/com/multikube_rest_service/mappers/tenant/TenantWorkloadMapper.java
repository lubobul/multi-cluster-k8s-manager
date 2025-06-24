package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadSummaryDto;
import com.multikube_rest_service.entities.tenant.TenantWorkload;
import com.multikube_rest_service.mappers.UserMapper; // Import the new UserMapper
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link TenantWorkload} entities to their corresponding
 * detailed ({@link TenantWorkloadDto}) and summary ({@link TenantWorkloadSummaryDto}) DTOs.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class}) // Add UserMapper to the 'uses' clause
public interface TenantWorkloadMapper {

    /**
     * Maps to the detailed DTO, including the full yamlContent and owner details.
     * @param entity The source TenantWorkload entity.
     * @return The detailed TenantWorkloadDto.
     */
    @Mapping(source = "tenantNamespace.id", target = "namespaceId")
    @Mapping(source = "createdByUser", target = "owner") // Add this mapping
    TenantWorkloadDto toDto(TenantWorkload entity);

    /**
     * Maps to the summary DTO, omitting the yamlContent but including owner details.
     * @param entity The source TenantWorkload entity.
     * @return The lightweight TenantWorkloadSummaryDto.
     */
    @Mapping(source = "tenantNamespace.id", target = "namespaceId")
    @Mapping(source = "createdByUser", target = "owner") // Add this mapping
    TenantWorkloadSummaryDto toSummaryDto(TenantWorkload entity);
}