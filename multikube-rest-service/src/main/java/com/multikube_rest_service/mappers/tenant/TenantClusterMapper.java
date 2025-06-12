package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.TenantClusterDto;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.mappers.GenericMapper;
import org.mapstruct.Mapper;

/**
 * Mapper for converting {@link KubernetesCluster} entities to {@link TenantClusterDto} objects.
 */
@Mapper(componentModel = "spring")
public interface TenantClusterMapper extends GenericMapper<KubernetesCluster, TenantClusterDto> {
}