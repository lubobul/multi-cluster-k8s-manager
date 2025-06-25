package com.multikube_rest_service.controllers.tenant;

import com.multikube_rest_service.dtos.requests.tenant.CreateTemplateCatalogRequest;
import com.multikube_rest_service.dtos.responses.tenant.TemplateCatalogDto;
import com.multikube_rest_service.dtos.responses.tenant.WorkloadTemplateSummaryDto;
import com.multikube_rest_service.rest.RestErrorResponse;
import com.multikube_rest_service.rest.RestResponsePage;
import com.multikube_rest_service.services.tenant.TemplateCatalogService;
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
 * REST controller for tenant administrators to manage Workload Template Catalogs.
 */
@RestController
@RequestMapping("/api/v1/tenant/catalogs")
@Tag(name = "Tenant - Template Catalogs", description = "Endpoints for managing workload template catalogs.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('TENANT_ADMIN')")
public class TemplateCatalogController {

    private final TemplateCatalogService catalogService;

    public TemplateCatalogController(TemplateCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Get all accessible template catalogs",
            description = "Retrieves a paginated list of all template catalogs. This includes both system-default catalogs and catalogs created by the current tenant.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)."),
                    @Parameter(name = "size", description = "Number of items per page."),
                    @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc').")
            })
    @ApiResponse(responseCode = "200", description = "List of catalogs retrieved successfully.")
    @GetMapping
    public ResponseEntity<RestResponsePage<TemplateCatalogDto>> getCatalogs(
            @Parameter(hidden = true) Pageable pageable) {
        Page<TemplateCatalogDto> catalogPage = catalogService.getCatalogs(pageable);
        return ResponseEntity.ok(new RestResponsePage<>(catalogPage.getContent(), catalogPage.getPageable(), catalogPage.getTotalElements()));
    }

    @Operation(summary = "Get a single template catalog by ID",
            description = "Retrieves details for a specific template catalog, provided it is accessible by the current tenant.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Catalog found and returned successfully."),
                    @ApiResponse(responseCode = "404", description = "Catalog not found or not accessible by the tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @GetMapping("/{catalogId}")
    public ResponseEntity<TemplateCatalogDto> getCatalog(
            @Parameter(description = "The unique identifier of the template catalog.")
            @PathVariable Long catalogId) {
        TemplateCatalogDto catalogDto = catalogService.getCatalog(catalogId);
        return ResponseEntity.ok(catalogDto);
    }

    @Operation(summary = "Create a new template catalog",
            description = "Creates a new, empty template catalog owned by the current tenant.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Catalog created successfully.", content = @Content(schema = @Schema(implementation = TemplateCatalogDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., name is blank).", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<TemplateCatalogDto> createCatalog(
            @Valid @RequestBody CreateTemplateCatalogRequest request) {
        TemplateCatalogDto createdCatalog = catalogService.createCatalog(request);
        return new ResponseEntity<>(createdCatalog, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a template catalog",
            description = "Deletes a template catalog owned by the current tenant. The catalog must be empty before it can be deleted. System-default catalogs cannot be deleted.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Catalog deleted successfully."),
                    @ApiResponse(responseCode = "404", description = "Catalog not found or not owned by the tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete a catalog that is not empty.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
            })
    @DeleteMapping("/{catalogId}")
    public ResponseEntity<Void> deleteCatalog(
            @Parameter(description = "The unique identifier of the template catalog to delete.")
            @PathVariable Long catalogId) {
        catalogService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a paginated list of all workload templates within a specific catalog.
     *
     * @param catalogId The ID of the parent catalog.
     * @param pageable  Pagination information.
     * @return A paginated list of workload template summaries.
     */
    @Operation(summary = "Get all templates for a specific catalog",
            description = "Retrieves a paginated list of lightweight summaries for all workload templates within a specific, accessible catalog.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)."),
                    @Parameter(name = "size", description = "Number of items per page."),
                    @Parameter(name = "sort", description = "Sort order (e.g., 'name,asc').")
            })
    @ApiResponse(responseCode = "200", description = "List of templates retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "Parent catalog not found or not accessible by the tenant.", content = @Content(schema = @Schema(implementation = RestErrorResponse.class)))
    @GetMapping("/{catalogId}/templates")
    public ResponseEntity<RestResponsePage<WorkloadTemplateSummaryDto>> getTemplatesForCatalog(
            @Parameter(description = "The unique identifier of the parent template catalog.", required = true)
            @PathVariable Long catalogId,
            @Parameter(hidden = true) Pageable pageable) {

        Page<WorkloadTemplateSummaryDto> page = catalogService.getTemplatesForCatalog(catalogId, pageable);
        return ResponseEntity.ok(new RestResponsePage<>(page.getContent(), page.getPageable(), page.getTotalElements()));
    }
}