package com.multikube_rest_service.mappers;

import com.multikube_rest_service.dtos.responses.UserDto;
import com.multikube_rest_service.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Instruct UserMapper to use TenantMapper for mapping Tenant objects
@Mapper(componentModel = "spring", uses = {TenantMapper.class})
public interface UserMapper extends GenericMapper<User, UserDto> {
    /**
     * Maps a User entity to a UserDto, including the nested TenantDto.
     *
     * @param user The User entity to map.
     * @return The mapped UserDto.
     */
    @Override
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "tenant", source = "tenant") // Explicitly map the tenant object
    UserDto toDto(User user);

    /**
     * Maps a UserDto to a User entity.
     *
     * @param userDto The UserDto to map.
     * @return The mapped User entity.
     */
    @Override
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "tenant", source = "tenant") // Explicitly map the tenant object
    User toEntity(UserDto userDto);
}