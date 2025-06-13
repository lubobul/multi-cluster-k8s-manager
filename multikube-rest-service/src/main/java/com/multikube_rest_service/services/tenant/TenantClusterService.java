package com.multikube_rest_service.services.tenant;

import com.multikube_rest_service.common.SecurityContextHelper;
import com.multikube_rest_service.common.enums.ClusterStatus;
import com.multikube_rest_service.dtos.responses.tenant.TenantClusterDto;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import com.multikube_rest_service.exceptions.ResourceNotFoundException;
import com.multikube_rest_service.mappers.tenant.TenantClusterMapper;
import com.multikube_rest_service.repositories.tenant.TenantKubernetesClusterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Service layer for tenant-facing cluster operations.
 */
@Service
public class TenantClusterService {

    private static final Logger logger = LoggerFactory.getLogger(TenantClusterService.class);

    private final TenantKubernetesClusterRepository clusterRepository;
    private final TenantClusterMapper tenantClusterMapper;

    public TenantClusterService(TenantKubernetesClusterRepository clusterRepository, TenantClusterMapper tenantClusterMapper) {
        this.clusterRepository = clusterRepository;
        this.tenantClusterMapper = tenantClusterMapper;
    }

    /**
     * Retrieves a paginated list of clusters allocated to the currently authenticated tenant.
     * Supports filtering by name and status.
     *
     * @param searchParams A map of search parameters (e.g., "name", "status").
     * @param pageable Pagination information.
     * @return A page of {@link TenantClusterDto} objects.
     */
    @Transactional(readOnly = true)
    public Page<TenantClusterDto> getClusters(Map<String, String> searchParams, Pageable pageable) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();

        String nameFilter = searchParams.getOrDefault("name", "").trim();
        String statusFilterString = searchParams.getOrDefault("status", "").trim();

        ClusterStatus statusFilter = null;
        if (StringUtils.hasText(statusFilterString)) {
            try {
                statusFilter = ClusterStatus.valueOf(statusFilterString.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status value provided in filter: '{}'. Ignoring status filter.", statusFilterString);
            }
        }

        Page<KubernetesCluster> clusterPage;
        boolean hasNameFilter = StringUtils.hasText(nameFilter);

        if (hasNameFilter && statusFilter != null) {
            clusterPage = clusterRepository.findByTenantIdAndNameContainingIgnoreCaseAndStatus(tenantId, nameFilter, statusFilter, pageable);
        } else if (hasNameFilter) {
            clusterPage = clusterRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, nameFilter, pageable);
        } else if (statusFilter != null) {
            clusterPage = clusterRepository.findByTenantIdAndStatus(tenantId, statusFilter, pageable);
        } else {
            clusterPage = clusterRepository.findByTenantId(tenantId, pageable);
        }

        return clusterPage.map(tenantClusterMapper::toDto);
    }

    /**
     * Retrieves details of a specific cluster allocated to the authenticated tenant.
     *
     * @param clusterId The ID of the cluster to retrieve.
     * @return A {@link TenantClusterDto} object.
     * @throws ResourceNotFoundException if the cluster is not found or not allocated to the tenant.
     */
    @Transactional(readOnly = true)
    public TenantClusterDto getCluster(Long clusterId) {
        Long tenantId = SecurityContextHelper.getAuthenticatedTenantId();
        KubernetesCluster cluster = clusterRepository.findByTenantIdAndId(tenantId, clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cluster not found with ID: " + clusterId + " for your tenant."));
        return tenantClusterMapper.toDto(cluster);
    }
}