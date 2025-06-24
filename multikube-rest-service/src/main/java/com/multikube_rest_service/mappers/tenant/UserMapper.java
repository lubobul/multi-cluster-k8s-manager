package com.multikube_rest_service.mappers.tenant;

import com.multikube_rest_service.dtos.responses.tenant.UserSummaryDto;
import com.multikube_rest_service.entities.User;
import org.mapstruct.Mapper;

/**
 * Maps {@link User} entities to {@link UserSummaryDto} objects.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummaryDto toSummaryDto(User user);
}