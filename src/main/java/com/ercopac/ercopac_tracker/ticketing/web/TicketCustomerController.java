package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.TicketCustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Customers of the organisation with their sites, contacts and equipment (Settings > Customers),
// plus the choices offered on the ticket form. Leads change customer records; the service checks it again.
@RestController
@RequestMapping("/api/tickets")
public class TicketCustomerController {

    private static final String STANDARD_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24', 'H24_LEAD', 'CLIENT')";
    private static final String STAFF_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24', 'H24_LEAD')";
    private static final String LEAD_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24_LEAD')";

    private final TicketCustomerService service;

    public TicketCustomerController(TicketCustomerService service) {
        this.service = service;
    }

    @GetMapping("/form-options")
    @PreAuthorize(STANDARD_ACCESS)
    public FormOptions formOptions(@RequestParam(required = false) Long customerId) {
        return service.formOptions(customerId);
    }

    @GetMapping("/equipment-types")
    @PreAuthorize(STAFF_ACCESS)
    public List<Ref> equipmentTypes() {
        return service.equipmentTypes();
    }

    @GetMapping("/portal-users")
    @PreAuthorize(STAFF_ACCESS)
    public List<UserSummary> portalUsers() {
        return service.portalUsers();
    }

    @GetMapping("/customers")
    @PreAuthorize(STAFF_ACCESS)
    public List<CustomerDto> customers() {
        return service.list();
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize(STAFF_ACCESS)
    public CustomerDto customer(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/customers")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto createCustomer(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize(LEAD_ACCESS)
    public CustomerDto updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/customers/{id}/sites")
    @PreAuthorize(STAFF_ACCESS)
    public List<SiteDto> sites(@PathVariable Long id) {
        return service.sites(id);
    }

    @PostMapping("/customers/{id}/sites")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.CREATED)
    public SiteDto createSite(@PathVariable Long id, @Valid @RequestBody SiteRequest request) {
        return service.createSite(id, request);
    }

    @PutMapping("/customers/{id}/sites/{siteId}")
    @PreAuthorize(LEAD_ACCESS)
    public SiteDto updateSite(@PathVariable Long id, @PathVariable Long siteId, @Valid @RequestBody SiteRequest request) {
        return service.updateSite(id, siteId, request);
    }

    @DeleteMapping("/customers/{id}/sites/{siteId}")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSite(@PathVariable Long id, @PathVariable Long siteId) {
        service.deleteSite(id, siteId);
    }

    @GetMapping("/customers/{id}/contacts")
    @PreAuthorize(STAFF_ACCESS)
    public List<ContactDto> contacts(@PathVariable Long id) {
        return service.contacts(id);
    }

    @PostMapping("/customers/{id}/contacts")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.CREATED)
    public ContactDto createContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        return service.createContact(id, request);
    }

    @PutMapping("/customers/{id}/contacts/{contactId}")
    @PreAuthorize(LEAD_ACCESS)
    public ContactDto updateContact(@PathVariable Long id, @PathVariable Long contactId, @Valid @RequestBody ContactRequest request) {
        return service.updateContact(id, contactId, request);
    }

    @DeleteMapping("/customers/{id}/contacts/{contactId}")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id, @PathVariable Long contactId) {
        service.deleteContact(id, contactId);
    }

    @GetMapping("/customers/{id}/equipment")
    @PreAuthorize(STAFF_ACCESS)
    public List<EquipmentDto> equipment(@PathVariable Long id) {
        return service.equipment(id);
    }

    @PostMapping("/customers/{id}/equipment")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentDto createEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return service.createEquipment(id, request);
    }

    @PutMapping("/customers/{id}/equipment/{equipmentId}")
    @PreAuthorize(LEAD_ACCESS)
    public EquipmentDto updateEquipment(@PathVariable Long id, @PathVariable Long equipmentId, @Valid @RequestBody EquipmentRequest request) {
        return service.updateEquipment(id, equipmentId, request);
    }

    @DeleteMapping("/customers/{id}/equipment/{equipmentId}")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipment(@PathVariable Long id, @PathVariable Long equipmentId) {
        service.deleteEquipment(id, equipmentId);
    }

    @GetMapping("/customers/{id}/projects")
    @PreAuthorize(STAFF_ACCESS)
    public List<ProjectDto> projects(@PathVariable Long id) {
        return service.projects(id);
    }
}
