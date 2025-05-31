package com.multikube_rest_service.dtos.requests.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreateRequest {
    private String name;
    private String description;
    private String defaultAdminName;
    private String defaultAdminPassword;
}