package com.multikube_rest_service.dtos.responses.tenant;

import com.multikube_rest_service.common.enums.ClusterStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * Data Transfer Object for representing a cluster's information to a tenant.
 * It omits provider-specific details for security and simplicity.
 */
@Data
@NoArgsConstructor
public class TenantClusterDto {
    private Long id;
    private String name;
    private String description;
    private ClusterStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}