package com.ercopac.ercopac_tracker.customer.web;

import com.ercopac.ercopac_tracker.customer.dto.CustomerOptionDto;
import com.ercopac.ercopac_tracker.customer.service.CustomerManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only customer options for dropdowns (projects, tickets, ...): active customers of the
 * caller's organisation. Clients, employees and the platform owner have no customer picker.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerOptionsController {

    static final String CUSTOMER_PICKER_ROLES =
            "hasAnyAuthority('ORG_ADMIN','ROLE_ORG_ADMIN',"
                    + "'PROJECT_MANAGER','ROLE_PROJECT_MANAGER','PROJECT_MANAGER_LEAD','ROLE_PROJECT_MANAGER_LEAD',"
                    + "'DEPARTMENT_MANAGER','ROLE_DEPARTMENT_MANAGER','MANAGER','ROLE_MANAGER',"
                    + "'SALES_MANAGER_LEAD','ROLE_SALES_MANAGER_LEAD','SALES_MANAGER','ROLE_SALES_MANAGER',"
                    + "'SYSTEM_ENGINEER','ROLE_SYSTEM_ENGINEER','H24','ROLE_H24','H24_LEAD','ROLE_H24_LEAD')";

    private final CustomerManagementService service;

    public CustomerOptionsController(CustomerManagementService service) {
        this.service = service;
    }

    @GetMapping("/options")
    @PreAuthorize(CUSTOMER_PICKER_ROLES)
    public List<CustomerOptionDto> options(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long includeId
    ) {
        return service.options(search, includeId);
    }
}
