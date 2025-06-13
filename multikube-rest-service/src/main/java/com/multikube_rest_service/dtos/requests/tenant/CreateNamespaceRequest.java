package com.multikube_rest_service.dtos.requests.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for requesting the creation of a new tenant namespace.
 */
@Data
public class CreateNamespaceRequest {

    @NotBlank(message = "Namespace name cannot be blank.")
    @Pattern(regexp = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$", message = "Invalid Kubernetes namespace name. It must be DNS-compliant.")
    private String name;

    private String description;

    @NotNull(message = "A target cluster ID must be provided.")
    private Long clusterId;

    // Optional YAML manifest for ResourceQuota
    private String resourceQuotaYaml;

    // Optional YAML manifest for LimitRange
    private String limitRangeYaml;
}