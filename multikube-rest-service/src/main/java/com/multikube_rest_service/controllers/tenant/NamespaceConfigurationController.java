package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationDto;
import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationSummaryDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.NamespaceConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for tenant administrators to view configuration resources
 * within a specific namespace.
 */
@RestController
@RequestMapping("/api/v1/tenant/namespaces/{namespaceId}/configurations")
@Tag(name = "Tenant - Namespace Configurations", description = "Endpoints for viewing namespace configuration resources.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class NamespaceConfigurationController {

    private final NamespaceConfigurationService configurationService;

    public NamespaceConfigurationController(NamespaceConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Retrieves a paginated list of configuration summaries for a specific namespace.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param pageable    Pagination information.
     * @return A paginated list of configuration summaries.
     */
    @Operation(summary = "Get all configurations for a namespace",
        description = "Retrieves a paginated list of lightweight summaries for all configuration resources (like NetworkPolicies, ResourceQuotas, etc.) within a specific namespace.",
        parameters = {
            @Parameter(name = "page", description = "Page number (zero-based).", example = "0"),
            @Parameter(name = "size", description = "Number of items per page.", example = "10"),
            @Parameter(name = "sort", description = "Sort order (e.g., 'k8sName,asc').")
        })
    @ApiResponse(responseCode = "200", description = "List of configurations retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "Parent namespace not found for the current tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
    @GetMapping
    public ResponseEntity<RestResponsePage<NamespaceConfigurationSummaryDto>> getConfigurations(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Parameter(hidden = true) Pageable pageable) {

        Page<NamespaceConfigurationSummaryDto> page = configurationService.getConfigurations(namespaceId, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements()));
    }

    /**
     * Retrieves a single, detailed configuration resource by its ID.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param configurationId The ID of the configuration resource to retrieve.
     * @return A detailed DTO of the configuration, including its full YAML content.
     */
    @Operation(summary = "Get a single configuration resource by ID",
        description = "Retrieves a comprehensive, detailed view of a specific configuration resource, including its full YAML content.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Configuration resource found and returned successfully.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = NamespaceConfigurationDto.class))),
            @ApiResponse(responseCode = "404", description = "Parent namespace or specific configuration not found.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @GetMapping("/{configurationId}")
    public ResponseEntity<NamespaceConfigurationDto> getConfiguration(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Parameter(description = "The unique identifier of the configuration resource.", required = true)
            @PathVariable Long configurationId) {

        NamespaceConfigurationDto dto = configurationService.getConfiguration(namespaceId, configurationId);
        return ResponseEntity.ok(dto);
    }
}