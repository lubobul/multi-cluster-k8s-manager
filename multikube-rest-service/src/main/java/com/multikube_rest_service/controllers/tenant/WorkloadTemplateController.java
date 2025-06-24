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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenant/workload-templates")
@Tag(name = "Tenant - Workload Templates", description = "Endpoints for managing workload templates.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class WorkloadTemplateController {

    private final WorkloadTemplateService templateService;

    public WorkloadTemplateController(WorkloadTemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "Get all accessible workload templates",
            description = "Retrieves a paginated list of all templates accessible to the tenant. Can be filtered by template name and/or catalog name.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)."),
                    @Parameter(name = "size", description = "Number of items per page."),
                    @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc')."),
                    @Parameter(name = "name", description = "Filter by template name (case-insensitive, partial match)."),
                    @Parameter(name = "catalogName", description = "Filter by catalog name (case-insensitive, partial match).")
            })
    @GetMapping
    public ResponseEntity<RestResponsePage<WorkloadTemplateSummaryDto>> getTemplates(
            @Parameter(hidden = true) @RequestParam(required = false) Map<String, String> searchParams,
            @Parameter(hidden = true) Pageable pageable) {
        Page<WorkloadTemplateSummaryDto> page = templateService.getTemplates(searchParams, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements()));
    }

    @Operation(summary = "Get a single workload template by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template found.", content = @Content(schema = @Schema(implementation = WorkloadTemplateDto.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found or not accessible.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @GetMapping("/{templateId}")
    public ResponseEntity<WorkloadTemplateDto> getTemplate(
            @Parameter(description = "The unique identifier of the workload template.", required = true)
            @PathVariable Long templateId) {
        WorkloadTemplateDto dto = templateService.getTemplate(templateId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create a new workload template",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template created successfully.", content = @Content(schema = @Schema(implementation = WorkloadTemplateDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Parent catalog not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<WorkloadTemplateDto> createTemplate(
            @Valid @RequestBody CreateWorkloadTemplateRequest request) {
        WorkloadTemplateDto createdTemplate = templateService.createTemplate(request);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a workload template",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Template deleted successfully."),
                    @ApiResponse(responseCode = "404", description = "Template not found or not accessible.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "The unique identifier of the workload template to delete.", required = true)
            @PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}