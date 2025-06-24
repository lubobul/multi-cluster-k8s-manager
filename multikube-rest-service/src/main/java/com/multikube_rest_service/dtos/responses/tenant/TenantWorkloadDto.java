package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.common.enums.SyncStatus;
import lombok.Data;
import java.sql.Timestamp;

/**
 * A detailed DTO for a TenantWorkload, including the full final YAML content.
 * Intended for single-item views.
 */
@Data
public class TenantWorkloadDto {
    private Long id;
    private String name;
    private String k8sName;
    private String k8sKind;
    private String yamlContent; // The final YAML for this specific instance
    private UserSummaryDto owner; // Added owner field
    private ResourceStatus status;
    private SyncStatus syncStatus;
    private String statusDetails;
    private Long namespaceId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}