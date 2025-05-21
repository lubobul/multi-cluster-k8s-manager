package com.multikube_rest_service.mappers;

import com.multikube_rest_service.dtos.responses.UserDto;
import com.multikube_rest_service.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper extends GenericMapper<User, UserDto> {
    @Override
    @Mapping(target = "createdAt", source = "createdAt")
        // Explicit mapping
    UserDto toDto(User user);

    @Override
    @Mapping(target = "createdAt", source = "createdAt")
        // Explicit mapping
    User toEntity(UserDto userDto);
}
