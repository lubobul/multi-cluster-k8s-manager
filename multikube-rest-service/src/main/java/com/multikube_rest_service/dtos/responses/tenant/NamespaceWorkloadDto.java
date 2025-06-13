package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.common.enums.SyncStatus;
import lombok.Data;

import java.sql.Timestamp;

/**
 * DTO representing a workload resource within a namespace (e.g., a Deployment, Service).
 */
@Data
public class NamespaceWorkloadDto {
    private Long id;
    private String name;
    private String k8sName;
    private String k8sKind;
    private ResourceStatus status;
    private SyncStatus syncStatus;
    private String statusDetails;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}