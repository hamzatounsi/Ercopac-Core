package com.ercopac.ercopac_tracker.customer.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.common.NameNormalizer;
import com.ercopac.ercopac_tracker.customer.dto.CustomerOptionDto;
import com.ercopac.ercopac_tracker.customer.dto.CustomerReferences;
import com.ercopac.ercopac_tracker.customer.dto.CustomerRequest;
import com.ercopac.ercopac_tracker.customer.dto.CustomerResponse;
import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Customer rules shared by every customer endpoint: organisation scope (always the signed-in
 * user's organisation), case-insensitive unique name and code per organisation, and delete
 * only when the customer is not referenced anywhere (otherwise deactivate).
 */
@Service
@Transactional
public class CustomerManagementService {

    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_OPTIONS = 50;

    /** Sort keys accepted from the UI, mapped to entity properties. */
    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "name", "name",
            "customerCode", "customerCode",
            "country", "country",
            "town", "town",
            "contactPerson", "contactPerson",
            "active", "active"
    );

    private final CustomerRepository customerRepository;
    private final OrganisationRepository organisationRepository;
    private final CustomerUsageService usageService;
    private final SecurityUtils securityUtils;

    public CustomerManagementService(
            CustomerRepository customerRepository,
            OrganisationRepository organisationRepository,
            CustomerUsageService usageService,
            SecurityUtils securityUtils
    ) {
        this.customerRepository = customerRepository;
        this.organisationRepository = organisationRepository;
        this.usageService = usageService;
        this.securityUtils = securityUtils;
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public OrgAdminDtos.PageResponse<CustomerResponse> list(
            String search, Boolean active, int page, int size, String sort, String direction
    ) {
        Long organisationId = currentOrganisationId();
        String property = SORT_PROPERTIES.getOrDefault(sort == null ? "" : sort, "name");
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort order = Sort.by(sortDirection, property);
        if (!"name".equals(property)) {
            order = order.and(Sort.by(Sort.Direction.ASC, "name"));
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), order);

        Page<Customer> result = customerRepository.search(
                organisationId, active != null, active != null && active, likePattern(search), pageRequest);

        return new OrgAdminDtos.PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return toResponse(requireInOrganisation(id));
    }

    /**
     * Options for customer dropdowns: active customers of the current organisation only.
     * {@code includeId} adds that customer even when inactive, so a form editing a record that
     * still points at a deactivated customer can display it.
     */
    @Transactional(readOnly = true)
    public List<CustomerOptionDto> options(String search, Long includeId) {
        Long organisationId = currentOrganisationId();
        List<CustomerOptionDto> options = new ArrayList<>(customerRepository
                .findActiveOptions(organisationId, likePattern(search), PageRequest.of(0, MAX_OPTIONS))
                .stream()
                .map(this::toOption)
                .toList());

        if (includeId != null && options.stream().noneMatch(option -> option.id().equals(includeId))) {
            customerRepository.findByIdAndOrganisation_Id(includeId, organisationId)
                    .ifPresent(customer -> options.add(0, toOption(customer)));
        }
        return options;
    }

    // ---------------------------------------------------------------- commands

    public CustomerResponse create(CustomerRequest request) {
        return toResponse(createCustomer(request));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        return toResponse(updateCustomer(id, request));
    }

    public CustomerResponse setActive(Long id, boolean active) {
        Customer customer = requireInOrganisation(id);
        customer.setActive(active);
        return toResponse(customerRepository.save(customer));
    }

    /** Creates a customer in the current organisation (also used by the older /api/admin contract). */
    public Customer createCustomer(CustomerRequest request) {
        Long organisationId = currentOrganisationId();
        String name = requireName(request.name());
        assertNameAvailable(organisationId, name, null);

        String code = normalizeCode(request.customerCode());
        if (code == null) {
            code = nextCustomerCode(organisationId);
        } else if (customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCase(organisationId, code)) {
            throw CustomerConflictException.codeExists(code);
        }

        Customer customer = new Customer();
        customer.setOrganisation(organisation(organisationId));
        customer.setCustomerCode(code);
        apply(customer, request, name);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer customer = requireInOrganisation(id);
        Long organisationId = customer.getOrganisation().getId();
        String name = requireName(request.name());
        assertNameAvailable(organisationId, name, id);

        String code = normalizeCode(request.customerCode());
        if (code != null && !code.equalsIgnoreCase(customer.getCustomerCode())) {
            if (customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCaseAndIdNot(organisationId, code, id)) {
                throw CustomerConflictException.codeExists(code);
            }
            customer.setCustomerCode(code);
        }

        apply(customer, request, name);
        return customerRepository.save(customer);
    }

    /** Deletes an unreferenced customer; a referenced one is rejected with 409 CUSTOMER_IN_USE. */
    public void delete(Long id) {
        Customer customer = requireInOrganisation(id);
        CustomerReferences references = usageService.referencesOf(customer);
        if (references.inUse()) {
            throw CustomerConflictException.inUse(references);
        }
        customerRepository.delete(customer);
    }

    /**
     * Throws 409 CUSTOMER_NAME_EXISTS when another customer of the organisation already has
     * this name, ignoring case and spacing. Used by every place that creates or renames customers.
     */
    public void assertNameAvailable(Long organisationId, String name, Long excludeId) {
        String normalized = NameNormalizer.upper(name);
        if (normalized == null) {
            return;
        }
        boolean exists = excludeId == null
                ? customerRepository.existsByOrganisation_IdAndNameIgnoreCase(organisationId, normalized)
                : customerRepository.existsByOrganisation_IdAndNameIgnoreCaseAndIdNot(organisationId, normalized, excludeId);
        if (exists) {
            throw CustomerConflictException.nameExists(normalized);
        }
    }

    /** Finds a customer of the organisation by name, ignoring case and spacing. */
    @Transactional(readOnly = true)
    public Optional<Customer> findByName(Long organisationId, String name) {
        String normalized = NameNormalizer.upper(name);
        return normalized == null
                ? Optional.empty()
                : customerRepository.findFirstByOrganisation_IdAndNameIgnoreCase(organisationId, normalized);
    }

    /** First free CUST-nnnn code of the organisation. */
    public String nextCustomerCode(Long organisationId) {
        long candidate = customerRepository.findByOrganisation_IdOrderByNameAsc(organisationId).size() + 1L;
        String code;
        do {
            code = String.format(Locale.ROOT, "CUST-%04d", candidate++);
        } while (customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCase(organisationId, code));
        return code;
    }

    @Transactional(readOnly = true)
    public CustomerResponse toResponse(Customer customer) {
        Organisation organisation = customer.getOrganisation();
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getName(),
                customer.getCountry(),
                customer.getTown(),
                customer.getAddress(),
                customer.getVatTaxId(),
                customer.getContactPerson(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getErpId(),
                customer.isActive(),
                organisation == null ? null : organisation.getId(),
                organisation == null ? null : organisation.getName(),
                usageService.referencesOf(customer)
        );
    }

    // ---------------------------------------------------------------- helpers

    private Customer requireInOrganisation(Long id) {
        // A customer of another organisation is reported as not found, never as forbidden,
        // so ids of other tenants cannot be probed.
        return customerRepository.findByIdAndOrganisation_Id(id, currentOrganisationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private Long currentOrganisationId() {
        try {
            return securityUtils.getCurrentOrganisationId();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organisation context for the current user");
        }
    }

    private Organisation organisation(Long organisationId) {
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found"));
    }

    private void apply(Customer customer, CustomerRequest request, String name) {
        customer.setName(name);
        customer.setCountry(trim(request.country()));
        customer.setTown(trim(request.town()));
        customer.setAddress(trim(request.address()));
        customer.setVatTaxId(trim(request.vatTaxId()));
        customer.setContactPerson(trim(request.contactPerson()));
        String email = trim(request.email());
        customer.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));
        customer.setPhone(trim(request.phone()));
        customer.setErpId(trim(request.erpId()));
        customer.setActive(request.active() == null || request.active());
    }

    private CustomerOptionDto toOption(Customer customer) {
        return new CustomerOptionDto(customer.getId(), customer.getCustomerCode(), customer.getName(), customer.isActive());
    }

    private static String requireName(String name) {
        String normalized = NameNormalizer.upper(name);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer name is required");
        }
        return normalized;
    }

    private static String normalizeCode(String code) {
        String trimmed = trim(code);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private static String likePattern(String search) {
        String trimmed = trim(search);
        return trimmed == null ? "%" : "%" + trimmed.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
