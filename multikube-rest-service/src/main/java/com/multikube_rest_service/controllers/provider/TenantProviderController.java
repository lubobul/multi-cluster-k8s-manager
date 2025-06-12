package com.multikube_rest_service.controllers.provider;

import com.multikube_rest_service.dtos.requests.provider.TenantCreateRequest;
import com.multikube_rest_service.dtos.responses.TenantDto;
import com.multikube_rest_service.services.TenantService;
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

/**
 * REST controller for PROVIDER_ADMIN users to manage Tenants.
 */
@RestController
@RequestMapping("/api/v1/provider/tenants")
@PreAuthorize("hasAuthority('PROVIDER_ADMIN')")
@Tag(name = "Provider - Tenant Management", description = "APIs for providers to manage tenant organizations")
@SecurityRequirement(name = "bearerAuth")
public class TenantProviderController {

    private final TenantService tenantService;

    /**
     * Constructs the controller with the TenantService.
     * @param tenantService Service for tenant operations.
     */
    public TenantProviderController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Creates a new tenant and its default TENANT_ADMIN user.
     *
     * @param tenantCreateRequest DTO containing tenant name, description, and default admin credentials.
     * @return ResponseEntity with the created TenantDto and HTTP status CREATED.
     */
    @Operation(summary = "Create a new tenant",
               description = "Allows a provider admin to create a new tenant organization along with a default admin user for that tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., duplicate tenant name, missing fields)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<TenantDto> createTenant(@RequestBody TenantCreateRequest tenantCreateRequest) {
        TenantDto createdTenant = tenantService.createTenant(tenantCreateRequest);
        return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific tenant by its ID.
     * The "System" tenant cannot be fetched via this endpoint.
     *
     * @param tenantId The ID of the tenant to retrieve.
     * @return ResponseEntity with the TenantDto.
     */
    @Operation(summary = "Get a tenant by ID",
               description = "Retrieves details for a specific tenant, excluding the 'System' tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tenant details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Tenant not found or access restricted")
    })
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantDto> getTenantById(
            @Parameter(description = "ID of the tenant to be retrieved") @PathVariable Long tenantId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        return ResponseEntity.ok(tenantDto);
    }

    /**
     * Lists all manageable tenants (excluding "System" tenant) with pagination and optional filtering.
     *
     * @param filterString Optional filter string (e.g., "name==some-tenant"). Currently supports 'name'.
     * @param pageable Pagination information.
     * @return ResponseEntity with a Page of TenantDto.
     */
    @Operation(summary = "List all manageable tenants",
               description = "Retrieves a paginated list of tenants, excluding the 'System' tenant. Supports filtering by name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of tenants"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<Page<TenantDto>> listTenants(
            @Parameter(description = "Filter string, e.g., 'name==some-tenant'. Supported key: 'name'.")
            @RequestParam(value = "filter", required = false) String filterString,
            @Parameter(hidden = true) Pageable pageable) {
        Page<TenantDto> tenants = tenantService.getTenants(filterString, pageable);
        return ResponseEntity.ok(tenants);
    }
}