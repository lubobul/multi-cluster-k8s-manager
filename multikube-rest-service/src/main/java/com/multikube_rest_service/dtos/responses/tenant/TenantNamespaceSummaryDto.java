package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.NamespaceStatus;
import lombok.Data;

import java.sql.Timestamp;

/**
 * A lightweight summary DTO for representing a namespace in a list view.
 * It omits detailed child collections in favor of simple counts for performance.
 */
@Data
public class TenantNamespaceSummaryDto {
    private Long id;
    private String name;
    private NamespaceStatus status;
    private Long clusterId;
    private String clusterName;
}