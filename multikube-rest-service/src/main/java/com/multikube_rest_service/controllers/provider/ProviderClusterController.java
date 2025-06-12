package com.multikube_rest_service.controllers.provider;

import com.multikube_rest_service.common.utils.FilterStringParser;
import com.multikube_rest_service.dtos.requests.provider.ClusterAllocationRequest;
import com.multikube_rest_service.dtos.requests.provider.ClusterRegistrationRequest;
import com.multikube_rest_service.dtos.responses.provider.ClusterDto;
import com.multikube_rest_service.rest.RestMessageResponse;
import com.multikube_rest_service.services.provider.ProviderClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for provider-specific operations related to Kubernetes cluster management.
 * All operations in this controller require the user to have the 'PROVIDER_ADMIN' authority.
 */
@RestController
@RequestMapping("/api/v1/provider/clusters")
@PreAuthorize("hasAuthority('PROVIDER_ADMIN')") // Secures all endpoints in this controller
@Tag(name = "Provider - Cluster Management", description = "APIs for providers to manage Kubernetes clusters")
// Assuming 'bearerAuth' is defined in your OpenAPI/Swagger configuration for JWT
@SecurityRequirement(name = "bearerAuth")
public class ProviderClusterController {

    private final ProviderClusterService providerClusterService;

    /**
     * Constructs the controller with the necessary service.
     *
     * @param providerClusterService The service for cluster management operations.
     */
    public ProviderClusterController(ProviderClusterService providerClusterService) {
        this.providerClusterService = providerClusterService;
    }

    /**
     * Endpoint for a provider to register a new Kubernetes cluster.
     * The request body should contain the cluster name, an optional description, and the kubeconfig.
     *
     * @param registrationRequest DTO containing the details for the new cluster.
     * @return A ResponseEntity containing the DTO of the registered cluster with an HTTP status of CREATED.
     */
    @Operation(
            summary = "Register a new Kubernetes cluster",
            description = "Allows a provider admin to register a new Kubernetes cluster by providing its name, description, and kubeconfig. " +
                    "The system will attempt an initial verification of the cluster's connectivity."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cluster registered successfully and initial verification attempted."),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing name, kubeconfig, duplicate name, or invalid kubeconfig format)."),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid."),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the 'PROVIDER_ADMIN' role.")
    })
    @PostMapping
    public ResponseEntity<ClusterDto> registerCluster(@RequestBody ClusterRegistrationRequest registrationRequest) {
        ClusterDto clusterResponse = providerClusterService.registerCluster(registrationRequest);
        return new ResponseEntity<>(clusterResponse, HttpStatus.CREATED);
    }

    /**
     * Retrieves details of a specific cluster registered by the authenticated provider.
     *
     * @param clusterId The ID of the cluster to retrieve.
     * @return A ResponseEntity containing the cluster details if found.
     */
    @Operation(summary = "Get cluster details by ID")
    @ApiResponses(value = { /* Ensure these are complete */})
    @GetMapping("/{clusterId}")
    public ResponseEntity<ClusterDto> getCluster(
            @Parameter(description = "ID of the cluster to be retrieved") @PathVariable Long clusterId) {
        ClusterDto clusterResponse = providerClusterService.getCluster(clusterId);
        return ResponseEntity.ok(clusterResponse);
    }

    /**
     * Lists clusters registered by the authenticated provider, with optional filtering and pagination.
     * The filter string should be in the format "key1==value1,key2==value2".
     *
     * @param filterString Optional query parameter string to filter clusters.
     * Supported filter keys: 'name' (contains, case-insensitive), 'status' (exact, case-insensitive for value).
     * Example: "name==my-cluster,status==ACTIVE"
     * @param pageable Pagination information (e.g., page, size, sort).
     * @return A ResponseEntity containing a page of cluster DTOs.
     */
    @Operation(summary = "List clusters for the provider with filtering")
    @ApiResponses(value = { /* Ensure these are complete */ })
    @GetMapping
    public ResponseEntity<Page<ClusterDto>> getClusters(
            @Parameter(description = "Filter string, e.g., 'name==my-cluster,status==ACTIVE'. Supported keys: 'name', 'status'.")
            @RequestParam(value = "filter", required = false) String filterString,
            @Parameter(hidden = true) Pageable pageable) { // Pageable is resolved by Spring

        Map<String, String> searchParams = FilterStringParser.parse(filterString);
        Page<ClusterDto> clusters = providerClusterService.getClusters(searchParams, pageable);
        return ResponseEntity.ok(clusters);
    }

    /**
     * Allocates a cluster to a specific tenant.
     * This creates the relationship that grants a tenant exclusive use of a registered cluster.
     *
     * @param clusterId The ID of the cluster to be allocated.
     * @param allocationRequest The request body containing the tenantId.
     * @return A ResponseEntity with a success message.
     */
    @Operation(summary = "Allocate a cluster to a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cluster successfully allocated"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., cluster already allocated, not active)"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Cluster or Tenant not found")
    })
    @PostMapping("/{clusterId}/allocation")
    public ResponseEntity<RestMessageResponse> allocateCluster(
            @Parameter(description = "ID of the cluster to allocate") @PathVariable Long clusterId,
            @RequestBody ClusterAllocationRequest allocationRequest) {
        RestMessageResponse response = providerClusterService.allocateCluster(clusterId, allocationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * De-allocates a cluster from a tenant.
     * This removes the relationship, making the cluster available for allocation to another tenant.
     * The operation will fail if the tenant has any resources (e.g., namespaces) on the cluster.
     *
     * @param clusterId The ID of the cluster to be de-allocated.
     * @return A ResponseEntity with a success message.
     */
    @Operation(summary = "De-allocate a cluster from a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cluster successfully de-allocated"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., cluster has active tenant resources)"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Cluster or allocation not found for this provider")
    })
    @DeleteMapping("/{clusterId}/allocation")
    public ResponseEntity<RestMessageResponse> deallocateCluster(
            @Parameter(description = "ID of the cluster whose allocation is to be removed") @PathVariable Long clusterId) {
        RestMessageResponse response = providerClusterService.deallocateCluster(clusterId);
        return ResponseEntity.ok(response);
    }
}