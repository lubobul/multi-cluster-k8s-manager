package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.TemplateCatalogDto;
import com.multikube_rest_service.entities.tenant.TemplateCatalog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link TemplateCatalog} entities to {@link TemplateCatalogDto} objects.
 */
@Mapper(componentModel = "spring")
public interface TemplateCatalogMapper {

    /**
     * Maps a TemplateCatalog entity to its DTO representation.
     * Note: It calculates the templatesCount and does not map the full list of templates
     * to ensure the response is lightweight.
     *
     * @param entity The source TemplateCatalog entity.
     * @return The mapped TemplateCatalogDto.
     */
    @Mapping(target = "systemDefault", expression = "java(entity.getTenant() == null)")
    @Mapping(target = "templatesCount", expression = "java(entity.getWorkloadTemplates().size())")
    TemplateCatalogDto toDto(TemplateCatalog entity);
}