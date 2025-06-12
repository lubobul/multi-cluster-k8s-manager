package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.TenantClusterDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.TenantClusterService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for tenant administrators to view their allocated Kubernetes clusters.
 */
@RestController
@RequestMapping("/api/v1/tenant/clusters")
@Tag(name = "Tenant - Cluster Management", description = "Endpoints for tenants to view their allocated clusters.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class TenantClusterController {

    private final TenantClusterService tenantClusterService;

    public TenantClusterController(TenantClusterService tenantClusterService) {
        this.tenantClusterService = tenantClusterService;
    }

    /**
     * GET /api/v1/tenant/clusters : Get a paginated list of clusters allocated to the tenant.
     *
     * @param searchParams Optional search parameters for filtering (e.g., name, status).
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a page of {@link TenantClusterDto}.
     */
    @Operation(summary = "Get a paginated list of allocated clusters",
            description = "Retrieves a paginated list of clusters that have been allocated to the currently authenticated tenant. " +
                    "Only users with the TENANT_ADMIN role can access this endpoint.",
            parameters = {
                    @Parameter(name = "page", description = "Page number, starting from 0.", example = "0"),
                    @Parameter(name = "size", description = "Number of items per page.", example = "10"),
                    @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc' or 'status,desc')."),
                    @Parameter(name = "name", description = "Filter by cluster name (case-insensitive, partial match).", example = "prod"),
                    @Parameter(name = "status", description = "Filter by cluster status.", example = "ACTIVE")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of clusters retrieved successfully."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the TENANT_ADMIN role",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @GetMapping
    public ResponseEntity<RestResponsePage<TenantClusterDto>> getClusters(
            @Parameter(hidden = true) @RequestParam(required = false) Map<String, String> searchParams,
            @Parameter(hidden = true) Pageable pageable) {
        Page<TenantClusterDto> clusterPage = tenantClusterService.getClusters(searchParams, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(clusterPage.getContent(), clusterPage.getPageable(), clusterPage.getTotalElements()));
    }

    /**
     * GET /api/v1/tenant/clusters/{clusterId} : Get a single cluster by its ID.
     *
     * @param clusterId The ID of the cluster to retrieve.
     * @return A ResponseEntity containing the {@link TenantClusterDto}.
     */
    @Operation(summary = "Get a single allocated cluster by ID",
            description = "Retrieves details of a specific cluster by its ID, provided it is allocated to the currently authenticated tenant.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cluster details retrieved successfully.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantClusterDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the TENANT_ADMIN role",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found - The cluster does not exist or is not allocated to this tenant",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @GetMapping("/{clusterId}")
    public ResponseEntity<TenantClusterDto> getCluster(
            @Parameter(description = "ID of the cluster to retrieve.", example = "1")
            @PathVariable Long clusterId) {
        TenantClusterDto clusterDto = tenantClusterService.getCluster(clusterId);
        return ResponseEntity.ok(clusterDto);
    }
}