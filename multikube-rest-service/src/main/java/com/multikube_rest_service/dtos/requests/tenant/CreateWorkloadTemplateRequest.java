package com.multikube_rest_service.dtos.requests.tenant;

import com.multikube_rest_service.common.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for requesting the creation of a new workload template within a catalog.
 */
@Data
public class CreateWorkloadTemplateRequest {

    @NotBlank(message = "Template name cannot be blank.")
    private String name;

    private String description;

    @NotNull(message = "Template type must be specified.")
    private TemplateType templateType;

    @NotBlank(message = "YAML content cannot be blank.")
    private String yamlContent;
}