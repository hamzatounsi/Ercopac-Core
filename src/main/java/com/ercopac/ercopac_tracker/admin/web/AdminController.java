package com.ercopac.ercopac_tracker.admin.web;

import com.ercopac.ercopac_tracker.admin.dto.*;
import com.ercopac.ercopac_tracker.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@org.springframework.security.access.prepost.PreAuthorize(
    "hasAnyAuthority('ROLE_PLATFORM_OWNER','ROLE_PLATFORM_ADMIN','ROLE_ORG_ADMIN')"
)
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ================= LICENCES =================

    @GetMapping("/licences")
    public ResponseEntity<List<AdminLicenceAssignmentDto>> getLicences() {
        return ResponseEntity.ok(adminService.getLicences());
    }

    @PostMapping("/licences")
    public ResponseEntity<AdminLicenceAssignmentDto> assignLicence(
            @RequestBody AssignLicenceRequest request
    ) {
        return ResponseEntity.ok(adminService.assignLicence(request));
    }

    @DeleteMapping("/licences/{userId}")
    public ResponseEntity<Void> removeLicence(@PathVariable Long userId) {
        adminService.removeLicence(userId);
        return ResponseEntity.noContent().build();
    }

    // ================= CATEGORIES =================

    @GetMapping("/categories")
    public ResponseEntity<List<ProjectCategoryDto>> getCategories() {
        return ResponseEntity.ok(adminService.getCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<ProjectCategoryDto> createCategory(
            @RequestBody SaveProjectCategoryRequest request
    ) {
        return ResponseEntity.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ProjectCategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody SaveProjectCategoryRequest request
    ) {
        return ResponseEntity.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ================= PROJECT TYPES =================

    @GetMapping("/types")
    public ResponseEntity<List<ProjectTypeDto>> getTypes() {
        return ResponseEntity.ok(adminService.getTypes());
    }

    @PostMapping("/types")
    public ResponseEntity<ProjectTypeDto> createType(
            @RequestBody SaveProjectTypeRequest request
    ) {
        return ResponseEntity.ok(adminService.createType(request));
    }

    @PutMapping("/types/{id}")
    public ResponseEntity<ProjectTypeDto> updateType(
            @PathVariable Long id,
            @RequestBody SaveProjectTypeRequest request
    ) {
        return ResponseEntity.ok(adminService.updateType(id, request));
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id) {
        adminService.deleteType(id);
        return ResponseEntity.noContent().build();
    }

    // ================= CUSTOMERS =================

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDto>> getCustomers() {
        return ResponseEntity.ok(adminService.getCustomers());
    }

    @PostMapping("/customers")
    public ResponseEntity<CustomerDto> createCustomer(
            @RequestBody SaveCustomerRequest request
    ) {
        return ResponseEntity.ok(adminService.createCustomer(request));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody SaveCustomerRequest request
    ) {
        return ResponseEntity.ok(adminService.updateCustomer(id, request));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}