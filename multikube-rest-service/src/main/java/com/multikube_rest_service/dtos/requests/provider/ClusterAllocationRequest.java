package com.multikube_rest_service.dtos.requests.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterAllocationRequest {
    private Long tenantId;
}