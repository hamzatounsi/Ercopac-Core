package com.ercopac.ercopac_tracker.user;

public enum Role {
    PLATFORM_OWNER,
    ORG_ADMIN,
    PROJECT_MANAGER,
    DEPARTMENT_MANAGER,
    EMPLOYEE,
    SALES_MANAGER,
    CLIENT;

    /**
     * Internal operational users participate in department capacity and need
     * a tenant-owned department and resource type.
     */
    public boolean requiresResourceProfile() {
        return this == PROJECT_MANAGER
                || this == DEPARTMENT_MANAGER
                || this == EMPLOYEE
                || this == SALES_MANAGER;
    }

    public boolean isPlatformRole() {
        return this == PLATFORM_OWNER;
    }
}
