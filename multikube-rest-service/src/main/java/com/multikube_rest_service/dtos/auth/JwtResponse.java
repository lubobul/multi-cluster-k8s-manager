package com.multikube_rest_service.dtos.auth;

import com.multikube_rest_service.dtos.responses.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    UserDto user;
    private String token;
}
