package com.ercopac.ercopac_tracker.customer.web;

import com.ercopac.ercopac_tracker.customer.dto.CustomerRequest;
import com.ercopac.ercopac_tracker.customer.dto.CustomerResponse;
import com.ercopac.ercopac_tracker.customer.service.CustomerManagementService;
import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Customer Management for the organisation administrator. Every operation is scoped to the
 * administrator's own organisation (SecurityConfig: /api/org-admin/** is ORG_ADMIN only).
 */
@RestController
@RequestMapping("/api/org-admin/customers")
@PreAuthorize("hasAnyAuthority('ORG_ADMIN','ROLE_ORG_ADMIN')")
public class OrgAdminCustomerController {

    private final CustomerManagementService service;

    public OrgAdminCustomerController(CustomerManagementService service) {
        this.service = service;
    }

    @GetMapping
    public OrgAdminDtos.PageResponse<CustomerResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.list(search, active, page, size, sort, direction);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request);
    }

    /** Activate or deactivate: {"active": false}. Deactivated customers leave every dropdown. */
    @PatchMapping("/{id}/status")
    public CustomerResponse setStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        return service.setActive(id, Boolean.TRUE.equals(payload.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
