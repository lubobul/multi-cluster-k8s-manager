package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceSummaryDto;
import com.multikube_rest_service.entities.tenant.TenantNamespace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link TenantNamespace} entities to {@link TenantNamespaceDto} (detailed view)
 * and {@link TenantNamespaceSummaryDto} (list view) objects.
 */
@Mapper(componentModel = "spring", uses = {NamespaceConfigurationMapper.class, NamespaceWorkloadMapper.class})
public interface TenantNamespaceMapper {

    /**
     * Maps to the detailed DTO, including the full lists of child resources.
     * This is intended for the single-item view (e.g., GET /namespaces/{id}).
     * It uses the other mappers to convert the collections.
     *
     * @param entity The source TenantNamespace entity.
     * @return The comprehensive TenantNamespaceDto.
     */
    @Mapping(source = "kubernetesCluster.id", target = "clusterId")
    @Mapping(source = "kubernetesCluster.name", target = "clusterName")
    @Mapping(target = "configurationsCount", expression = "java(entity.getConfigurations().size())")
    @Mapping(target = "workloadsCount", expression = "java(entity.getWorkloads().size())")
    TenantNamespaceDto toDetailDto(TenantNamespace entity);

    /**
     * Maps to the lightweight summary DTO, which is optimized for list views.
     * Instead of full child resource lists, it calculates and includes counts.
     *
     * @param entity The source TenantNamespace entity.
     * @return The lightweight TenantNamespaceSummaryDto.
     */
    @Mapping(source = "kubernetesCluster.id", target = "clusterId")
    @Mapping(source = "kubernetesCluster.name", target = "clusterName")
    TenantNamespaceSummaryDto toSummaryDto(TenantNamespace entity);
}