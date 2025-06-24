package com.multikube_rest_service.dtos.responses.tenant;

import lombok.Data;

/**
 * A lightweight DTO representing a summary of a user, typically for ownership information.
 */
@Data
public class UserSummaryDto {
    private Long id;
    private String username;
}