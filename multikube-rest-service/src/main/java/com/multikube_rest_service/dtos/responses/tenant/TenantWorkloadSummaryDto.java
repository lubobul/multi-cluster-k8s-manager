package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.common.enums.SyncStatus;
import lombok.Data;
import java.sql.Timestamp;

/**
 * A lightweight summary DTO for a TenantWorkload.
 * It omits the yamlContent to keep paginated list views fast and responsive.
 */
@Data
public class TenantWorkloadSummaryDto {
    private Long id;
    private String name;
    private String k8sName;
    private String k8sKind;
    private ResourceStatus status;
    private SyncStatus syncStatus;
    private String statusDetails;
    private UserSummaryDto owner; // Added owner field
    private Long namespaceId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}