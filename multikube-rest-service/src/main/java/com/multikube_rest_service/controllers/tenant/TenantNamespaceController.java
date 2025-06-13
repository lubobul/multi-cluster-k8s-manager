package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.requests.tenant.CreateNamespaceRequest;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceDto;
import com.multikube_rest_service.dtos.responses.tenant.TenantNamespaceSummaryDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.TenantNamespaceService;
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

/**
 * REST controller for tenant administrators to manage their namespaces.
 */
@RestController
@RequestMapping("/api/v1/tenant/namespaces")
@Tag(name = "Tenant - Namespace Management", description = "Endpoints for tenants to manage namespaces.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class TenantNamespaceController {

    private final TenantNamespaceService namespaceService;

    public TenantNamespaceController(TenantNamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * Creates a new namespace for the current tenant.
     * @param request The request body containing details for the new namespace.
     * @return A detailed DTO of the newly created namespace.
     */
    @Operation(summary = "Create a new namespace",
        description = "Creates a namespace in a specified cluster. This operation also applies default security policies (NetworkPolicy, RBAC) and any optional resource limits provided.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Namespace created successfully.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantNamespaceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., invalid name, missing clusterId).",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a TENANT_ADMIN.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - A namespace with the same name already exists in the target cluster.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class)))
        })
    @PostMapping
    public ResponseEntity<TenantNamespaceDto> createNamespace(@Valid @RequestBody CreateNamespaceRequest request) {
        TenantNamespaceDto createdNamespace = namespaceService.createNamespace(request);
        return new ResponseEntity<>(createdNamespace, HttpStatus.CREATED);
    }

//    /**
//     * Retrieves a paginated list of namespaces for the current tenant.
//     * @param searchParams Optional query parameters for filtering by 'name' or 'status'.
//     * @param pageable Pagination information.
//     * @return A paginated list of namespace summaries.
//     */
//    @Operation(summary = "Get a paginated list of namespaces",
//        description = "Retrieves a paginated list of lightweight namespace summaries belonging to the currently authenticated tenant.",
//        parameters = {
//            @Parameter(name = "page", description = "Page number (zero-based).", example = "0"),
//            @Parameter(name = "size", description = "Number of items per page.", example = "10"),
//            @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc')."),
//            @Parameter(name = "name", description = "Filter by namespace name (case-insensitive, partial match)."),
//            @Parameter(name = "status", description = "Filter by namespace status (e.g., 'ACTIVE', 'FAILED_CREATION').")
//        })
//    @GetMapping
//    public ResponseEntity<RestResponsePage<TenantNamespaceSummaryDto>> getNamespaces(
//            @Parameter(hidden = true) @RequestParam(required = false) Map<String, String> searchParams,
//            @Parameter(hidden = true) Pageable pageable) {
//        Page<TenantNamespaceSummaryDto> namespacePage = namespaceService.getNamespaces(searchParams, pageable);
//        return ResponseEntity.ok(new RestResponsePage<>(namespacePage));
//    }
//
//    /**
//     * Retrieves a single, detailed view of a namespace by its ID.
//     * @param namespaceId The ID of the namespace to retrieve.
//     * @return A detailed DTO of the namespace, including its configurations and workloads.
//     */
//    @Operation(summary = "Get a single namespace by ID",
//        description = "Retrieves a comprehensive, detailed view of a specific namespace, including all its associated configuration resources.",
//        responses = {
//            @ApiResponse(responseCode = "200", description = "Namespace found and returned successfully.",
//                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantNamespaceDto.class))),
//            @ApiResponse(responseCode = "404", description = "Namespace not found for the current tenant.",
//                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class)))
//        })
//    @GetMapping("/{namespaceId}")
//    public ResponseEntity<TenantNamespaceDto> getNamespace(
//            @Parameter(description = "The unique identifier of the namespace.", required = true)
//            @PathVariable Long namespaceId) {
//        TenantNamespaceDto namespaceDto = namespaceService.getNamespace(namespaceId);
//        return ResponseEntity.ok(namespaceDto);
//    }
}