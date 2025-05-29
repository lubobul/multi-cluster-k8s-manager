package com.multikube_rest_service.mappers;

import com.multikube_rest_service.dtos.responses.UserDto;
import com.multikube_rest_service.entities.Role;
import com.multikube_rest_service.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // For custom mapping method

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TenantMapper.class})
public interface UserMapper extends GenericMapper<User, UserDto> {

    @Override
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "tenant", source = "tenant")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleNames") // Use custom mapping
    UserDto toDto(User user);

    @Override
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "tenant", source = "tenant")
    // Mapping roles back from Set<String> to Set<Role> is more complex
    // and usually handled in the service layer if needed for user creation/update.
    // For now, we'll focus on toDto. If toEntity needs this, it requires fetching Role entities by name.
    @Mapping(target = "roles", ignore = true) // Or implement a reverse mapping if needed
    User toEntity(UserDto userDto);

    /**
     * Custom mapping method to convert a Set of Role entities to a Set of role name strings.
     * @param roles The set of Role entities.
     * @return A set of role name strings.
     */
    @Named("rolesToRoleNames")
    default Set<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}