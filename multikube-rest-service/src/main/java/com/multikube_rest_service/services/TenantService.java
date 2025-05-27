package com.multikube_rest_service.services;

import com.multikube_rest_service.dtos.responses.TenantDto;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.TenantMapper;
import com.multikube_rest_service.repositories.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List; // For Collections.singletonList

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private static final String SYSTEM_TENANT_NAME = "System";

    public TenantService(TenantRepository tenantRepository, TenantMapper tenantMapper) {
        this.tenantRepository = tenantRepository;
        this.tenantMapper = tenantMapper;
    }

    /**
     * Finds a non-System tenant by its ID.
     *
     * @param id The ID of the tenant.
     * @return The TenantDto if found and is not the "System" tenant.
     * @throws ResourceNotFoundException if no tenant is found or if the found tenant is the "System" tenant.
     */
    @Transactional(readOnly = true)
    public TenantDto getTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        if (SYSTEM_TENANT_NAME.equals(tenant.getName())) {
            // Or, depending on desired behavior, simply don't find it for external callers.
            // For strictness, preventing direct fetch of "System" tenant via general methods is safer.
            throw new ResourceNotFoundException("Tenant not found with ID: " + id + " (or access restricted).");
        }
        return tenantMapper.toDto(tenant);
    }

    /**
     * Finds a non-System tenant by its unique name.
     *
     * @param name The name of the tenant.
     * @return The TenantDto if found and is not the "System" tenant.
     * @throws ResourceNotFoundException if no tenant is found or if the found tenant is the "System" tenant.
     */
    @Transactional(readOnly = true)
    public TenantDto getTenant(String name) {
        if (SYSTEM_TENANT_NAME.equalsIgnoreCase(name)) {
            throw new ResourceNotFoundException("Tenant not found with name: " + name + " (or access restricted).");
        }
        Tenant tenant = tenantRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with name: " + name));
        return tenantMapper.toDto(tenant);
    }

    /**
     * Retrieves a paginated list of all tenants, EXCLUDING the "System" tenant.
     *
     * @param nameFilter Optional filter for the tenant name (case-insensitive, partial match).
     * If the filter targets "System", it will still be excluded.
     * @param pageable Pagination information.
     * @return A page of TenantDto objects.
     */
    @Transactional(readOnly = true)
    public Page<TenantDto> getTenants(String nameFilter, Pageable pageable) {
        Page<Tenant> tenantPage;
        List<String> excludedNames = Collections.singletonList(SYSTEM_TENANT_NAME);

        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            String trimmedFilter = nameFilter.trim();
            // Ensure the filter itself isn't "System" if we want to strictly exclude it even when searched.
            if (SYSTEM_TENANT_NAME.equalsIgnoreCase(trimmedFilter)) {
                // Return an empty page if "System" is explicitly searched for by a general user
                return Page.empty(pageable);
            }
            tenantPage = tenantRepository.findByNameContainingIgnoreCaseAndNameNotIn(trimmedFilter, excludedNames, pageable);
        } else {
            tenantPage = tenantRepository.findByNameNotIn(excludedNames, pageable);
        }
        return tenantPage.map(tenantMapper::toDto);
    }

    // This specific method allows internal services to fetch the "System" Tenant *entity* if absolutely necessary.
    // It's not part of the general public DTO-based API for tenants.
    // The UserAuthService or other internal setup logic might need this.
    Tenant getSystemTenantEntity() {
        return tenantRepository.findByName(SYSTEM_TENANT_NAME)
                .orElseThrow(() -> new IllegalStateException("CRITICAL: System tenant '" + SYSTEM_TENANT_NAME + "' not found in the database. Database seeding might have failed."));
    }
}