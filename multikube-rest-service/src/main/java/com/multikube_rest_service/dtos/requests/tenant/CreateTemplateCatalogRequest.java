package com.multikube_rest_service.dtos.requests.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for requesting the creation of a new workload template catalog.
 */
@Data
public class CreateTemplateCatalogRequest {

    @NotBlank(message = "Catalog name cannot be blank.")
    private String name;

    private String description;
}