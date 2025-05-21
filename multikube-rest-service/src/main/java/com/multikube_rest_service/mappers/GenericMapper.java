package com.multikube_rest_service.mappers;

public interface GenericMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
}
