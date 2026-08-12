package com.ercopac.ercopac_tracker.org_admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class OrgAdminDtos {

    private OrgAdminDtos() {
    }

    public record OrganisationProfile(
            String name,
            String code,
            String country,
            String domain,
            String status,
            String plan,
            int userLimit,
            int orgAdminLicenceLimit,
            int projectManagerLicenceLimit,
            int departmentManagerLicenceLimit,
            int employeeLicenceLimit,
            int salesManagerLicenceLimit,
            int clientLicenceLimit,
            LocalDateTime createdAt
    ) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Organisation name is required")
            @Size(min = 2, max = 120, message = "Organisation name must contain 2 to 120 characters")
            String name,

            @Size(max = 80, message = "Country must not exceed 80 characters")
            String country,

            @Size(max = 120, message = "Domain must not exceed 120 characters")
            String domain
    ) {
    }

    public record SecuritySettings(
            String sessionTimeout,
            int maxFailedLogins,
            int passwordMinLength
    ) {
    }

    public record UpdateSecuritySettingsRequest(
            @NotBlank(message = "Session timeout is required") String sessionTimeout,
            @Min(value = 3, message = "Maximum failed logins must be at least 3")
            @Max(value = 10, message = "Maximum failed logins must not exceed 10")
            int maxFailedLogins,
            @Min(value = 8, message = "Minimum password length must be at least 8")
            @Max(value = 64, message = "Minimum password length must not exceed 64")
            int passwordMinLength
    ) {
    }

    public record RoleCount(String role, String label, long total, long active) {
    }

    public record Overview(
            OrganisationProfile organisation,
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long departments,
            long pendingPasswordResets,
            List<RoleCount> usersByRole,
            List<String> configurationWarnings
    ) {
    }

    public record UserSummary(
            Long id,
            String fullName,
            String email,
            String role,
            Long departmentId,
            String departmentCode,
            String departmentName,
            Long resourceTypeId,
            String resourceTypeCode,
            String resourceTypeName,
            String employeeCode,
            String jobTitle,
            boolean active
    ) {
    }

    public record CreateUserRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must not exceed 150 characters")
            String fullName,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 180, message = "Email must not exceed 180 characters")
            String email,

            @NotBlank(message = "A temporary password is required")
            @Size(min = 8, max = 128, message = "Temporary password must contain 8 to 128 characters")
            String password,

            @NotBlank(message = "Role is required") String role,
            Long departmentId,
            Long resourceTypeId,

            @Size(max = 40, message = "Employee code must not exceed 40 characters")
            String employeeCode,

            @Size(max = 80, message = "Job title must not exceed 80 characters")
            String jobTitle,
            Boolean active
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must not exceed 150 characters")
            String fullName,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 180, message = "Email must not exceed 180 characters")
            String email,

            @NotBlank(message = "Role is required") String role,
            Long departmentId,
            Long resourceTypeId,

            @Size(max = 40, message = "Employee code must not exceed 40 characters")
            String employeeCode,

            @Size(max = 80, message = "Job title must not exceed 80 characters")
            String jobTitle,

            @NotNull(message = "Account status is required") Boolean active
    ) {
    }

    public record UpdateUserStatusRequest(
            @NotNull(message = "Account status is required") Boolean active
    ) {
    }

    public record DepartmentSummary(
            Long id,
            String code,
            String name,
            Long managerId,
            String managerName,
            long userCount,
            LocalDateTime createdAt
    ) {
    }

    public record SaveDepartmentRequest(
            @NotBlank(message = "Department code is required")
            @Size(min = 2, max = 30, message = "Department code must contain 2 to 30 characters")
            @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Department code can only contain letters, numbers, hyphens and underscores")
            String code,

            @NotBlank(message = "Department name is required")
            @Size(min = 2, max = 100, message = "Department name must contain 2 to 100 characters")
            String name,
            Long managerId
    ) {
    }

    public record SupplierSummary(
            Long id,
            String name,
            String code,
            String contactPerson,
            String email,
            String phone,
            String address,
            String notes,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record SaveSupplierRequest(
            @NotBlank(message = "Supplier name is required")
            @Size(max = 150, message = "Supplier name must not exceed 150 characters")
            String name,

            @NotBlank(message = "Supplier code is required")
            @Size(max = 50, message = "Supplier code must not exceed 50 characters")
            @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Supplier code can only contain letters, numbers, hyphens and underscores")
            String code,

            @Size(max = 150, message = "Contact person must not exceed 150 characters")
            String contactPerson,

            @jakarta.validation.constraints.Email(message = "Enter a valid supplier email address")
            @Size(max = 180, message = "Email must not exceed 180 characters")
            String email,

            @Size(max = 50, message = "Phone must not exceed 50 characters")
            String phone,

            @Size(max = 500, message = "Address must not exceed 500 characters")
            String address,

            @Size(max = 2000, message = "Notes must not exceed 2000 characters")
            String notes,

            @NotNull(message = "Supplier status is required")
            Boolean active
    ) {
    }

    public record PermissionView(String module, String label, boolean canRead, boolean canWrite) {
    }

    public record RoleSummary(
            String role,
            String label,
            String description,
            long totalUsers,
            long activeUsers,
            List<PermissionView> permissions
    ) {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
