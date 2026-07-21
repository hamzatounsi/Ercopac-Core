package com.ercopac.ercopac_tracker.org_admin.web;

import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.org_admin.service.OrganisationAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org-admin")
@PreAuthorize("hasAnyAuthority('ORG_ADMIN','ROLE_ORG_ADMIN')")
public class OrganisationAdminController {

    private final OrganisationAdminService service;

    public OrganisationAdminController(OrganisationAdminService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public OrgAdminDtos.Overview overview() {
        return service.getOverview();
    }

    @GetMapping("/profile")
    public OrgAdminDtos.OrganisationProfile profile() {
        return service.getProfile();
    }

    @PutMapping("/profile")
    public OrgAdminDtos.OrganisationProfile updateProfile(
            @Valid @RequestBody OrgAdminDtos.UpdateProfileRequest request
    ) {
        return service.updateProfile(request);
    }

    @GetMapping("/settings/security")
    public OrgAdminDtos.SecuritySettings securitySettings() {
        return service.getSecuritySettings();
    }

    @PutMapping("/settings/security")
    public OrgAdminDtos.SecuritySettings updateSecuritySettings(
            @Valid @RequestBody OrgAdminDtos.UpdateSecuritySettingsRequest request
    ) {
        return service.updateSecuritySettings(request);
    }

    @GetMapping("/users")
    public OrgAdminDtos.PageResponse<OrgAdminDtos.UserSummary> users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.getUsers(search, departmentId, role, active, page, size, sort, direction);
    }

    @PostMapping("/users")
    public ResponseEntity<OrgAdminDtos.UserSummary> createUser(
            @Valid @RequestBody OrgAdminDtos.CreateUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(request));
    }

    @PutMapping("/users/{id}")
    public OrgAdminDtos.UserSummary updateUser(
            @PathVariable Long id,
            @Valid @RequestBody OrgAdminDtos.UpdateUserRequest request
    ) {
        return service.updateUser(id, request);
    }

    @PatchMapping("/users/{id}/status")
    public OrgAdminDtos.UserSummary updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrgAdminDtos.UpdateUserStatusRequest request
    ) {
        return service.updateUserStatus(id, request.active());
    }

    @GetMapping("/departments")
    public List<OrgAdminDtos.DepartmentSummary> departments() {
        return service.getDepartments();
    }

    @PostMapping("/departments")
    public ResponseEntity<OrgAdminDtos.DepartmentSummary> createDepartment(
            @Valid @RequestBody OrgAdminDtos.SaveDepartmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDepartment(request));
    }

    @PutMapping("/departments/{id}")
    public OrgAdminDtos.DepartmentSummary updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody OrgAdminDtos.SaveDepartmentRequest request
    ) {
        return service.updateDepartment(id, request);
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        service.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public List<OrgAdminDtos.RoleSummary> roles() {
        return service.getRoles();
    }
}
