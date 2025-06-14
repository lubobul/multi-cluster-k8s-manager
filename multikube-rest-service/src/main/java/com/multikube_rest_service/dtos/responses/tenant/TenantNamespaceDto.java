package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.NamespaceStatus;
import lombok.Data;

import java.sql.Timestamp;

/**
 * DTO representing a detailed view of a TenantNamespace, including its
 * full list of configurations and workloads.
 */
@Data
public class TenantNamespaceDto {
    private Long id;
    private String name;
    private String description;
    private NamespaceStatus status;
    private String statusDetails;
    private Long clusterId;
    private String clusterName;
    private int configurationsCount;
    private int workloadsCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}