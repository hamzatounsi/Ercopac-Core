package com.ercopac.ercopac_tracker.platform_permissions.dto;

public class RolePermissionDto {
    public String module;
    public String label;
    public String group;
    public String icon;
    public boolean canRead;
    public boolean canWrite;

    public RolePermissionDto() {}

    public RolePermissionDto(String module, String label, String group, String icon, boolean canRead, boolean canWrite) {
        this.module = module;
        this.label = label;
        this.group = group;
        this.icon = icon;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }
}