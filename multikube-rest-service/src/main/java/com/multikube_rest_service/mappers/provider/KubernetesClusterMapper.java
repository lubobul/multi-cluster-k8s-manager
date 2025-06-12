package com.multikube_rest_service.mappers.provider;

import com.multikube_rest_service.dtos.responses.provider.ClusterDto;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.mappers.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface KubernetesClusterMapper extends GenericMapper<KubernetesCluster, ClusterDto> {

    @Mappings({
            @Mapping(source = "providerUser.id", target = "providerUserId"),
            @Mapping(source = "providerUser.username", target = "providerUsername"),
            @Mapping(target = "allocation", ignore = true)
    })
    @Override
    ClusterDto toDto(KubernetesCluster kubernetesCluster);
}