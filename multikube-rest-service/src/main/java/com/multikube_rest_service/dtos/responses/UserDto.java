package com.multikube_rest_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set; // Import Set

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private TenantDto tenant; // Assuming TenantDto is already part of UserDto as per previous discussion

    // Add roles
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Include only if not empty
    private Set<String> roles;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Timestamp createdAt;
}