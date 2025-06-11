package com.multikube_rest_service.services;

import com.multikube_rest_service.common.enums.RoleType;
import com.multikube_rest_service.dtos.auth.RegisterRequest;
import com.multikube_rest_service.dtos.requests.provider.TenantCreateRequest;
import com.multikube_rest_service.dtos.responses.TenantDto;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.provider.ClusterAllocation;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.TenantMapper;
import com.multikube_rest_service.repositories.TenantRepository;
import com.multikube_rest_service.common.utils.FilterStringParser; // Assuming this exists

import com.multikube_rest_service.repositories.provider.ClusterAllocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for managing Tenant entities.
 */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final UserAuthService userAuthService;
    private final ClusterAllocationRepository clusterAllocationRepository;
    private static final String SYSTEM_TENANT_NAME = "System";

    /**
     * Constructs a new TenantService.
     * @param tenantRepository The repository for tenant data.
     * @param tenantMapper The mapper for converting between tenant entities and DTOs.
     * @param userAuthService Service for user authentication and registration.
     */
    public TenantService(TenantRepository tenantRepository,
                         TenantMapper tenantMapper,
                         UserAuthService userAuthService, ClusterAllocationRepository clusterAllocationRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantMapper = tenantMapper;
        this.userAuthService = userAuthService;
        this.clusterAllocationRepository = clusterAllocationRepository;
    }

    /**
     * Creates a new tenant and its default admin user.
     * Prevents creation of a tenant with the reserved name "System".
     *
     * @param createRequest DTO containing details for the new tenant and its default admin.
     * @return The DTO of the newly created tenant.
     * @throws IllegalArgumentException if validation fails for tenant or admin user details.
     */
    @Transactional
    public TenantDto createTenant(TenantCreateRequest createRequest) {
        if (!StringUtils.hasText(createRequest.getName())) {
            throw new IllegalArgumentException("Tenant name cannot be empty.");
        }
        String trimmedTenantName = createRequest.getName().trim();
        if (SYSTEM_TENANT_NAME.equalsIgnoreCase(trimmedTenantName)) {
            throw new IllegalArgumentException("Tenant name '" + SYSTEM_TENANT_NAME + "' is reserved and cannot be used.");
        }
        if (tenantRepository.existsByName(trimmedTenantName)) {
            throw new IllegalArgumentException("Tenant with name '" + trimmedTenantName + "' already exists.");
        }

        // Validate default admin user details from the request
        if (!StringUtils.hasText(createRequest.getDefaultAdminName())) {
            throw new IllegalArgumentException("Default admin username cannot be empty.");
        }
        if (!StringUtils.hasText(createRequest.getDefaultAdminPassword())) {
            // Add more password policy checks if needed, similar to UserAuthService.validateRegisterRequest
            throw new IllegalArgumentException("Default admin password cannot be empty.");
        }
        // Consider adding email for default admin if your User entity requires it and it's unique.
        // For now, an email might be auto-generated or use a convention.
        String defaultAdminEmail = createRequest.getDefaultAdminName().trim().toLowerCase() + "@" + trimmedTenantName.toLowerCase().replaceAll("\\s+", "") + ".multikube.com";


        Tenant tenant = new Tenant();
        tenant.setName(trimmedTenantName);
        tenant.setDescription(createRequest.getDescription());
        tenant.setIsActive(true); // New tenants are active by default

        Tenant savedTenant = tenantRepository.save(tenant);
        String defaultAdminUsername = createRequest.getDefaultAdminName().trim();

        // Create the default admin user for this new tenant
        // This uses a simplified RegisterRequest; UserAuthService.register might need adjustment
        // if it expects more fields or has complex tenant assignment logic not covered here.
        // We are directly calling a modified registration logic here or UserAuthService would need
        // to be refactored to accept a Tenant object.
        try {
            // Construct a RegisterRequest for the UserAuthService
            // Note: UserAuthService.register was previously noted to have a gap in tenant assignment.
            // For this to work, UserAuthService.register needs to be able to assign a user to a *provided* Tenant entity.
            // Let's assume we will refactor UserAuthService.register or add a new method for this.
            // For now, we bypass a full RegisterRequest and construct the User more directly if UserAuthService is not ready.

            RegisterRequest adminRegisterRequest = new RegisterRequest(
                            defaultAdminUsername,
                            defaultAdminEmail, // Auto-generated email
                            createRequest.getDefaultAdminPassword(),
                            RoleType.TENANT_ADMIN.getRoleName()
                            // Tenant ID will be set by a potentially refactored UserAuthService or a new specialized method
                    );

            // This call assumes UserAuthService.register can handle assigning the user to the 'savedTenant'.
            // If not, UserAuthService needs a method like:
            // userAuthService.registerUserForTenant(adminRegisterRequest, savedTenant);
            // For now, let's call the existing register method. It will need refactoring.
            userAuthService.registerUserForTenant(adminRegisterRequest, savedTenant);


        } catch (Exception e) {
            // If admin creation fails, we might want to roll back tenant creation.
            // @Transactional should handle this.
            throw new RuntimeException("Failed to create default admin user for tenant '" + savedTenant.getName() + "': " + e.getMessage(), e);
        }

        return tenantMapper.toDto(savedTenant);
    }


    @Transactional(readOnly = true)
    public TenantDto getTenant(Long id) {
        Tenant tenantEntity = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        if (SYSTEM_TENANT_NAME.equals(tenantEntity.getName())) {
            throw new ResourceNotFoundException("Access to '" + SYSTEM_TENANT_NAME + "' tenant via this API is restricted.");
        }

        // Map to DTO first
        TenantDto tenantDto = tenantMapper.toDto(tenantEntity);

        // Fetch and set allocated cluster IDs
        List<Long> allocatedClusterIds = clusterAllocationRepository.findByTenantIdIn(Collections.singletonList(id))
                .stream()
                .map(allocation -> allocation.getKubernetesCluster().getId())
                .collect(Collectors.toList());
        tenantDto.setAllocatedClusterIds(allocatedClusterIds);

        return tenantDto;
    }

    @Transactional(readOnly = true)
    public Page<TenantDto> getTenants(String filterString, Pageable pageable) {
        Map<String, String> filters = FilterStringParser.parse(filterString);
        String nameFilter = filters.getOrDefault("name", "").trim();

        Page<Tenant> tenantPage;
        List<String> excludedNames = Collections.singletonList(SYSTEM_TENANT_NAME);

        if (StringUtils.hasText(nameFilter)) {
            if (SYSTEM_TENANT_NAME.equalsIgnoreCase(nameFilter)) {
                return Page.empty(pageable);
            }
            tenantPage = tenantRepository.findByNameContainingIgnoreCaseAndNameNotIn(nameFilter, excludedNames, pageable);
        } else {
            tenantPage = tenantRepository.findByNameNotIn(excludedNames, pageable);
        }

        // Efficiently fetch all allocations for the tenants on the current page
        List<Long> tenantIdsOnPage = tenantPage.getContent().stream()
                .map(Tenant::getId)
                .collect(Collectors.toList());

        Map<Long, List<Long>> tenantToClusterIdsMap = new java.util.HashMap<>();
        if (!tenantIdsOnPage.isEmpty()) {
            List<ClusterAllocation> allocations = clusterAllocationRepository.findByTenantIdIn(tenantIdsOnPage);
            tenantToClusterIdsMap = allocations.stream()
                    .collect(Collectors.groupingBy(
                            allocation -> allocation.getTenant().getId(),
                            Collectors.mapping(allocation -> allocation.getKubernetesCluster().getId(), Collectors.toList())
                    ));
        }

        // Map the Page<Tenant> to Page<TenantDto> and set the allocatedClusterIds for each DTO
        final Map<Long, List<Long>> finalTenantToClusterIdsMap = tenantToClusterIdsMap;
        return tenantPage.map(tenant -> {
            TenantDto dto = tenantMapper.toDto(tenant);
            dto.setAllocatedClusterIds(finalTenantToClusterIdsMap.getOrDefault(tenant.getId(), Collections.emptyList()));
            return dto;
        });
    }

    /**
     * Allows internal services to fetch the "System" Tenant *entity*.
     *
     * @return The System Tenant entity.
     * @throws IllegalStateException if the System tenant is not found.
     */
    Tenant getSystemTenantEntity() {
        return tenantRepository.findByName(SYSTEM_TENANT_NAME)
                .orElseThrow(() -> new IllegalStateException("CRITICAL: System tenant '" + SYSTEM_TENANT_NAME + "' not found. Database seeding might have failed."));
    }
}