package com.multikube_rest_service.dtos.requests.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for requesting the creation of a new workload instance in a namespace.
 */
@Data
public class CreateWorkloadRequest {

    @NotBlank(message = "A name for this workload instance is required.")
    private String name; // The UI-friendly name for this specific instance

    private String description; // Optional description for this instance

    /**
     * The ID of the WorkloadTemplate to use as a starting point.
     * The user can then modify the YAML before submission.
     */
    private Long templateId;

    /**
     * The final, potentially edited YAML manifest to be applied.
     * This is required if no templateId is provided, or can be the customized
     * version of the YAML from the selected template.
     */
    @NotBlank(message = "YAML content must be provided.")
    private String yamlContent;
}