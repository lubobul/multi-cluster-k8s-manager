package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.requests.tenant.CreateWorkloadRequest;
import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantWorkloadSummaryDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.TenantWorkloadService;
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
 * REST controller for tenants to manage their application workloads within a namespace.
 */
@RestController
@RequestMapping("/api/v1/tenant/namespaces/{namespaceId}/workloads")
@Tag(name = "Tenant - Workloads", description = "Endpoints for managing application workloads within a specific namespace.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('TENANT_USER')")
public class TenantWorkloadController {

    private final TenantWorkloadService workloadService;

    public TenantWorkloadController(TenantWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @Operation(summary = "Get all workloads in a namespace",
        description = "Retrieves a paginated list of lightweight summaries for all workloads within a specific namespace.",
        parameters = {
            @Parameter(name = "page", description = "Page number (zero-based)."),
            @Parameter(name = "size", description = "Number of items per page."),
            @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc').")
        })
    @ApiResponse(responseCode = "200", description = "List of workloads retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "Parent namespace not found for the current tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
    @GetMapping
    public ResponseEntity<RestResponsePage<TenantWorkloadSummaryDto>> getWorkloads(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Parameter(hidden = true) Pageable pageable) {
        Page<TenantWorkloadSummaryDto> page = workloadService.getWorkloads(namespaceId, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements()));
    }

    @Operation(summary = "Get a single workload by ID",
        description = "Retrieves a detailed view of a specific workload, including its full final YAML content.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Workload found and returned successfully.", content = @Content(schema = @Schema(implementation = TenantWorkloadDto.class))),
            @ApiResponse(responseCode = "404", description = "Parent namespace or specific workload not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @GetMapping("/{workloadId}")
    public ResponseEntity<TenantWorkloadDto> getWorkload(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Parameter(description = "The unique identifier of the workload.", required = true)
            @PathVariable Long workloadId) {
        TenantWorkloadDto dto = workloadService.getWorkload(namespaceId, workloadId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create a new workload",
        description = "Creates a new workload instance in a namespace. The workload can be created from a template or from raw YAML.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Workload created successfully.", content = @Content(schema = @Schema(implementation = TenantWorkloadDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., name is blank, YAML is invalid).", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent namespace not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @PostMapping
    public ResponseEntity<TenantWorkloadDto> createWorkload(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Valid @RequestBody CreateWorkloadRequest request) {
        TenantWorkloadDto createdWorkload = workloadService.createWorkload(namespaceId, request);
        return new ResponseEntity<>(createdWorkload, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a workload",
        description = "Deletes a workload. TENANT_ADMINs can delete any workload. TENANT_USERs can only delete workloads they created.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Workload deleted successfully."),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to delete this workload.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent namespace or specific workload not found.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @DeleteMapping("/{workloadId}")
    public ResponseEntity<Void> deleteWorkload(
            @Parameter(description = "The unique identifier of the parent namespace.", required = true)
            @PathVariable Long namespaceId,
            @Parameter(description = "The unique identifier of the workload to delete.", required = true)
            @PathVariable Long workloadId) {
        workloadService.deleteWorkload(namespaceId, workloadId);
        return ResponseEntity.noContent().build();
    }
}