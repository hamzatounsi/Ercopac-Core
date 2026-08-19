package com.ercopac.ercopac_tracker.user;

public enum Role {
    PLATFORM_OWNER,
    ORG_ADMIN,
    PROJECT_MANAGER,
    PROJECT_MANAGER_LEAD,
    MANAGER,
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
                || this == PROJECT_MANAGER_LEAD
                || this == DEPARTMENT_MANAGER
                || this == EMPLOYEE
                || this == SALES_MANAGER;
    }

    public boolean isPlatformRole() {
        return this == PLATFORM_OWNER;
    }

    public boolean isProjectManagerRole() {
        return this == PROJECT_MANAGER || this == PROJECT_MANAGER_LEAD;
    }
}
