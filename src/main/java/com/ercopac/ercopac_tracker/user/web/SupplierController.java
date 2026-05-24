package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.user.dto.CreateSupplierRequest;
import com.ercopac.ercopac_tracker.user.dto.SupplierDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateSupplierRequest;
import com.ercopac.ercopac_tracker.user.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private static final String SUPPLIERS_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).SUPPLIERS)";

    private static final String SUPPLIERS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).SUPPLIERS)";

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize(SUPPLIERS_READ)
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        return ResponseEntity.ok(supplierService.getSuppliers());
    }

    @PostMapping
    @PreAuthorize(SUPPLIERS_WRITE)
    public ResponseEntity<SupplierDto> createSupplier(@RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SUPPLIERS_WRITE)
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable Long id,
            @RequestBody UpdateSupplierRequest request
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SUPPLIERS_WRITE)
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}