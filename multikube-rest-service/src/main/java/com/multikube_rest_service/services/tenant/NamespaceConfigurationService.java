package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationDto;
import com.multikube_rest_service.dtos.responses.tenant.NamespaceConfigurationSummaryDto;
import com.multikube_rest_service.entities.tenant.TenantNamespaceConfiguration;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.NamespaceConfigurationMapper;
import com.multikube_rest_service.repositories.tenant.TenantNamespaceConfigurationRepository;
import com.multikube_rest_service.repositories.tenant.TenantNamespaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing namespace configuration resources.
 */
@Service
public class NamespaceConfigurationService {

    private final TenantNamespaceRepository namespaceRepository;
    private final TenantNamespaceConfigurationRepository configurationRepository;
    private final NamespaceConfigurationMapper configurationMapper;

    public NamespaceConfigurationService(
            TenantNamespaceRepository namespaceRepository,
            TenantNamespaceConfigurationRepository configurationRepository,
            NamespaceConfigurationMapper configurationMapper) {
        this.namespaceRepository = namespaceRepository;
        this.configurationRepository = configurationRepository;
        this.configurationMapper = configurationMapper;
    }

    /**
     * Retrieves a paginated list of configuration summaries for a given namespace.
     * Ensures the user has access to the parent namespace.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param pageable Pagination information.
     * @return A Page of configuration summaries.
     */
    @Transactional(readOnly = true)
    public Page<NamespaceConfigurationSummaryDto> getConfigurations(Long namespaceId, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Security Check: Ensure the parent namespace exists and belongs to the current tenant.
        if (!namespaceRepository.existsByIdAndTenantId(namespaceId, tenantId)) {
            throw new ResourceNotFoundException("Namespace not found with ID: " + namespaceId);
        }

        Page<TenantNamespaceConfiguration> configurationsPage = configurationRepository.findByTenantNamespaceId(namespaceId, pageable);
        return configurationsPage.map(configurationMapper::toSummaryDto);
    }

    /**
     * Retrieves a single, detailed configuration resource by its ID.
     * Ensures the user has access to the parent namespace before returning the resource.
     *
     * @param namespaceId The ID of the parent namespace.
     * @param configurationId The ID of the configuration to retrieve.
     * @return A detailed DTO of the configuration, including its YAML content.
     */
    @Transactional(readOnly = true)
    public NamespaceConfigurationDto getConfiguration(Long namespaceId, Long configurationId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        // Security Check: First, ensure access to the parent namespace.
        if (!namespaceRepository.existsByIdAndTenantId(namespaceId, tenantId)) {
            throw new ResourceNotFoundException("Namespace not found with ID: " + namespaceId);
        }
        
        // Fetch the specific configuration
        TenantNamespaceConfiguration config = configurationRepository.findById(configurationId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with ID: " + configurationId));

        // Security Check: Verify that the found configuration actually belongs to the specified namespace.
        // This prevents accessing a configuration from another namespace by guessing its ID.
        if (!config.getTenantNamespace().getId().equals(namespaceId)) {
            throw new ResourceNotFoundException("Configuration with ID " + configurationId + " does not belong to namespace " + namespaceId);
        }

        return configurationMapper.toDto(config);
    }
}