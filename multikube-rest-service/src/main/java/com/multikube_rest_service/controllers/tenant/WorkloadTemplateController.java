package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.requests.tenant.CreateWorkloadTemplateRequest;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateDto;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateSummaryDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.WorkloadTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant administrators to manage Workload Templates within a catalog.
 */
@RestController
@RequestMapping("/api/v1/tenant/catalogs/{catalogId}/templates")
@Tag(name = "Tenant - Workload Templates", description = "Endpoints for managing workload templates within a specific catalog.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class WorkloadTemplateController {

    private final WorkloadTemplateService templateService;

    public WorkloadTemplateController(WorkloadTemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "Get all templates in a catalog",
        description = "Retrieves a paginated list of lightweight summaries for all workload templates within a specific catalog.",
        parameters = {
            @Parameter(name = "page", description = "Page number (zero-based)."),
            @Parameter(name = "size", description = "Number of items per page."),
            @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc').")
        })
    @ApiResponse(responseCode = "200", description = "List of templates retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "Parent catalog not found or not accessible by the tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
    @GetMapping
    public ResponseEntity<RestResponsePage<WorkloadTemplateSummaryDto>> getTemplates(
            @Parameter(description = "The unique identifier of the parent catalog.", required = true)
            @PathVariable Long catalogId,
            @Parameter(hidden = true) Pageable pageable) {
        Page<WorkloadTemplateSummaryDto> page = templateService.getTemplates(catalogId, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements()));
    }

    @Operation(summary = "Get a single workload template by ID",
        description = "Retrieves a detailed view of a specific workload template, including its full YAML content.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Template found and returned successfully.", content = @Content(schema = @Schema(implementation = WorkloadTemplateDto.class))),
            @ApiResponse(responseCode = "404", description = "Parent catalog or specific template not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @GetMapping("/{templateId}")
    public ResponseEntity<WorkloadTemplateDto> getTemplate(
            @Parameter(description = "The unique identifier of the parent catalog.", required = true)
            @PathVariable Long catalogId,
            @Parameter(description = "The unique identifier of the workload template.", required = true)
            @PathVariable Long templateId) {
        WorkloadTemplateDto dto = templateService.getTemplate(catalogId, templateId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create a new workload template",
        description = "Creates a new, reusable workload template within a specified catalog.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Template created successfully.", content = @Content(schema = @Schema(implementation = WorkloadTemplateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., name is blank, YAML is invalid).", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent catalog not found or not accessible by the tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @PostMapping
    public ResponseEntity<WorkloadTemplateDto> createTemplate(
            @Parameter(description = "The unique identifier of the parent catalog.", required = true)
            @PathVariable Long catalogId,
            @Valid @RequestBody CreateWorkloadTemplateRequest request) {
        WorkloadTemplateDto createdTemplate = templateService.createTemplate(catalogId, request);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a workload template",
        description = "Deletes a workload template. This action will fail if the template is currently in use by any active workloads.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Template deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Parent catalog or specific template not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete a template that is currently in use.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "The unique identifier of the parent catalog.", required = true)
            @PathVariable Long catalogId,
            @Parameter(description = "The unique identifier of the workload template to delete.", required = true)
            @PathVariable Long templateId) {
        templateService.deleteTemplate(catalogId, templateId);
        return ResponseEntity.noContent().build();
    }
}