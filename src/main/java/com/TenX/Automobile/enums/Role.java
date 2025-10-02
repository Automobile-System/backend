package com.TenX.Automobile.enums;

/**
 * Enterprise-level user roles enum
 * Defines the hierarchy: ADMIN > MANAGER > EMPLOYEE > USER
 */
public enum Role {
    USER("USER", 1),
    EMPLOYEE("EMPLOYEE", 2),
    MANAGER("MANAGER", 3),
    ADMIN("ADMIN", 4);

    private final String authority;
    private final int level;

    Role(String authority, int level) {
        this.authority = authority;
        this.level = level;
    }

    public String getAuthority() {
        return "ROLE_" + authority;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Check if this role has higher or equal authority than the given role
     */
    public boolean hasAuthorityOver(Role other) {
        return this.level >= other.level;
    }

    /**
     * Get role from string (case insensitive)
     */
    public static Role fromString(String role) {
        for (Role r : Role.values()) {
            if (r.authority.equalsIgnoreCase(role) || r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}