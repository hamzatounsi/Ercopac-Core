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
@org.springframework.security.access.prepost.PreAuthorize(
    "hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','OWNER','PLATFORM_OWNER','PLATFORM_ADMIN')"
)
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        return ResponseEntity.ok(supplierService.getSuppliers());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<SupplierDto> createSupplier(@RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<SupplierDto> updateSupplier(
            @PathVariable Long id,
            @RequestBody UpdateSupplierRequest request
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}