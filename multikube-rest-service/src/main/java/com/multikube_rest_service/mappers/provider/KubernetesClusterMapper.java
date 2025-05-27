package com.multikube_rest_service.mappers.provider;

import com.multikube_rest_service.dtos.responses.provider.ClusterResponse;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.mappers.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface KubernetesClusterMapper extends GenericMapper<KubernetesCluster, ClusterResponse> {

    @Mappings({
            @Mapping(source = "providerUser.id", target = "providerUserId"),
            @Mapping(source = "providerUser.username", target = "providerUsername")
    })
    @Override
    ClusterResponse toDto(KubernetesCluster kubernetesCluster);
}