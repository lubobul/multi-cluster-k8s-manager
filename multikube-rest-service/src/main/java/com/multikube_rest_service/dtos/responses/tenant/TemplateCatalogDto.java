package com.multikube_rest_service.dtos.responses.tenant;

import lombok.Data;

/**
 * A DTO for a TemplateCatalog. It is kept lightweight by including a count of its
 * templates rather than the full list. The full list of templates should be fetched
 * from a dedicated endpoint.
 */
@Data
public class TemplateCatalogDto {
    private Long id;
    private String name;
    private String description;
    private boolean isSystemDefault;
    private int templatesCount; // Replaces the List<...>
}