package com.ercopac.ercopac_tracker.user;

public enum Role {
    PLATFORM_OWNER,
    ORG_ADMIN,
    PROJECT_MANAGER,
    PROJECT_MANAGER_LEAD,
    MANAGER,
    DEPARTMENT_MANAGER,
    EMPLOYEE,
    SALES_MANAGER_LEAD,
    SALES_MANAGER,
    SYSTEM_ENGINEER,
    CLIENT;

    /**
     * Delivery/workforce users participate in department capacity and need a
     * tenant-owned department and resource type. CRM roles consume a sales
     * licence, but are not project resources.
     */
    public boolean requiresResourceProfile() {
        return this == PROJECT_MANAGER
                || this == PROJECT_MANAGER_LEAD
                || this == DEPARTMENT_MANAGER
                || this == EMPLOYEE;
    }

    public boolean isPlatformRole() {
        return this == PLATFORM_OWNER;
    }

    public boolean isProjectManagerRole() {
        return this == PROJECT_MANAGER || this == PROJECT_MANAGER_LEAD;
    }

    public boolean isCrmRole() {
        return this == SALES_MANAGER_LEAD || this == SALES_MANAGER || this == SYSTEM_ENGINEER;
    }

    public boolean isSalesManagerRole() {
        return this == SALES_MANAGER_LEAD || this == SALES_MANAGER || this == SYSTEM_ENGINEER;
    }
}
