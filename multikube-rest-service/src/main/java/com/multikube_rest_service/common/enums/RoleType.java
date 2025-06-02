package com.multikube_rest_service.common.enums;

public enum RoleType {
    PROVIDER_ADMIN("PROVIDER_ADMIN"),
    TENANT_ADMIN("TENANT_ADMIN"),
    TENANT_USER("TENANT_USER");

    private final String roleName;

    RoleType(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Gets the string representation of the role name.
     * This is useful for when you need to interact with parts of the system
     * that expect the role as a string (e.g., database queries for Role entity by name,
     * JWT claims if they store string names).
     *
     * @return The string name of the role.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Provides the string representation of the enum constant.
     * This is often implicitly used by @Enumerated(EnumType.STRING) in JPA
     * and for default serialization if not otherwise configured.
     *
     * @return The string name of the role.
     */
    @Override
    public String toString() {
        return this.roleName;
    }

    /**
     * Converts a string to the corresponding RoleType enum constant.
     * This is useful for converting input strings (e.g., from a request DTO) to an enum.
     *
     * @param text The string representation of the role.
     * @return The matching RoleType.
     * @throws IllegalArgumentException if no matching RoleType is found.
     */
    public static RoleType fromString(String text) {
        if (text != null) {
            for (RoleType b : RoleType.values()) {
                if (text.equalsIgnoreCase(b.roleName)) {
                    return b;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text '" + text + "' found in RoleType enum");
    }
}