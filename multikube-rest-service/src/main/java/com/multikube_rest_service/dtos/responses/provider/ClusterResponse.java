package com.multikube_rest_service.dtos.responses.provider;

import com.multikube_rest_service.common.enums.ClusterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResponse {
    private Long id;
    private String name;
    private String description;
    private Long providerUserId;
    private String providerUsername;
    private ClusterStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}