package com.multikube_rest_service.dtos.responses.tenant;

import lombok.Data;

/**
 * A lightweight summary of a TemplateCatalog, used for nesting in other DTOs.
 */
@Data
public class CatalogSummaryDto {
    private Long id;
    private String name;
}