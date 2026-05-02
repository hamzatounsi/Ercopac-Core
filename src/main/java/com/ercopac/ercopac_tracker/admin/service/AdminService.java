package com.ercopac.ercopac_tracker.admin.service;

import com.ercopac.ercopac_tracker.admin.domain.*;
import com.ercopac.ercopac_tracker.admin.dto.*;
import com.ercopac.ercopac_tracker.admin.repository.*;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ercopac.ercopac_tracker.user.Role;


import java.util.List;

@Service
@Transactional
public class AdminService {

    private final SecurityUtils securityUtils;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AdminLicenceAssignmentRepository licenceRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectTypeRepository typeRepository;
    private final CustomerRepository customerRepository;

    public AdminService(
            SecurityUtils securityUtils,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            AdminLicenceAssignmentRepository licenceRepository,
            ProjectCategoryRepository categoryRepository,
            ProjectTypeRepository typeRepository,
            CustomerRepository customerRepository
    ) {
        this.securityUtils = securityUtils;
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.licenceRepository = licenceRepository;
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.customerRepository = customerRepository;
    }

    // ================= LICENCES =================

    @Transactional(readOnly = true)
    public List<AdminLicenceAssignmentDto> getLicences() {
        Long organisationId = requireOrganisationId();

        return licenceRepository.findByOrganisation_IdOrderByUser_FullNameAsc(organisationId)
                .stream()
                .map(this::toLicenceDto)
                .toList();
    }

    private Role mapLicenceToRole(String licenceType) {
        return switch (licenceType) {
            case "ADMIN" -> Role.ORG_ADMIN;
            case "PM" -> Role.GENERAL_MANAGER;
            case "MANAGER" -> Role.PMO;
            case "DEPT_MANAGER" -> Role.DEPARTMENT_MANAGER;
            case "READ_ONLY" -> Role.EMPLOYEE;
            default -> throw new IllegalArgumentException("Unknown licence type: " + licenceType);
        };
    }

    public AdminLicenceAssignmentDto assignLicence(AssignLicenceRequest request) {
        Long organisationId = requireOrganisationId();

        if (request.userId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        AdminLicenceType licenceType = parseLicenceType(request.licenceType());

        AppUser user = userRepository.findByIdAndOrganisation_Id(request.userId(), organisationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in current organisation"));

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        AdminLicenceAssignment assignment = licenceRepository
                .findByOrganisation_IdAndUser_Id(organisationId, request.userId())
                .orElseGet(AdminLicenceAssignment::new);

            assignment.setOrganisation(organisation);
            assignment.setUser(user);
            assignment.setLicenceType(licenceType);

            user.setRole(mapLicenceToRole(licenceType.name()));
            userRepository.save(user);

            return toLicenceDto(licenceRepository.save(assignment));
    }

    public void removeLicence(Long userId) {
        Long organisationId = requireOrganisationId();

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        licenceRepository.deleteByOrganisation_IdAndUser_Id(organisationId, userId);
    }

    // ================= CATEGORIES =================

    @Transactional(readOnly = true)
    public List<ProjectCategoryDto> getCategories() {
        Long organisationId = requireOrganisationId();

        return categoryRepository.findByOrganisation_IdOrderByNameAsc(organisationId)
                .stream()
                .map(category -> toCategoryDto(category, organisationId))
                .toList();
    }

    public ProjectCategoryDto createCategory(SaveProjectCategoryRequest request) {
        Long organisationId = requireOrganisationId();
        validateNameAndCode(request.name(), request.code());

        String code = normalizeUpper(request.code());

        if (categoryRepository.existsByOrganisation_IdAndCodeIgnoreCase(organisationId, code)) {
            throw new IllegalArgumentException("Category code already exists");
        }

        ProjectCategory category = new ProjectCategory();
        category.setOrganisation(getOrganisation(organisationId));
        applyCategoryRequest(category, request);

        return toCategoryDto(categoryRepository.save(category), organisationId);
    }

    public ProjectCategoryDto updateCategory(Long id, SaveProjectCategoryRequest request) {
        Long organisationId = requireOrganisationId();
        validateNameAndCode(request.name(), request.code());

        ProjectCategory category = categoryRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String newCode = normalizeUpper(request.code());
        if (!category.getCode().equalsIgnoreCase(newCode)
                && categoryRepository.existsByOrganisation_IdAndCodeIgnoreCase(organisationId, newCode)) {
            throw new IllegalArgumentException("Category code already exists");
        }

        applyCategoryRequest(category, request);

        return toCategoryDto(categoryRepository.save(category), organisationId);
    }

    public void deleteCategory(Long id) {
        Long organisationId = requireOrganisationId();

        ProjectCategory category = categoryRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        categoryRepository.delete(category);
    }

    // ================= PROJECT TYPES =================

    @Transactional(readOnly = true)
    public List<ProjectTypeDto> getTypes() {
        Long organisationId = requireOrganisationId();

        return typeRepository.findByOrganisation_IdOrderByNameAsc(organisationId)
                .stream()
                .map(type -> toTypeDto(type, organisationId))
                .toList();
    }

    public ProjectTypeDto createType(SaveProjectTypeRequest request) {
        Long organisationId = requireOrganisationId();
        validateNameAndCode(request.name(), request.code());

        String code = normalizeUpper(request.code());

        if (typeRepository.existsByOrganisation_IdAndCodeIgnoreCase(organisationId, code)) {
            throw new IllegalArgumentException("Project type code already exists");
        }

        ProjectType type = new ProjectType();
        type.setOrganisation(getOrganisation(organisationId));
        applyTypeRequest(type, request);

        return toTypeDto(typeRepository.save(type), organisationId);
    }

    public ProjectTypeDto updateType(Long id, SaveProjectTypeRequest request) {
        Long organisationId = requireOrganisationId();
        validateNameAndCode(request.name(), request.code());

        ProjectType type = typeRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Project type not found"));

        String newCode = normalizeUpper(request.code());
        if (!type.getCode().equalsIgnoreCase(newCode)
                && typeRepository.existsByOrganisation_IdAndCodeIgnoreCase(organisationId, newCode)) {
            throw new IllegalArgumentException("Project type code already exists");
        }

        applyTypeRequest(type, request);

        return toTypeDto(typeRepository.save(type), organisationId);
    }

    public void deleteType(Long id) {
        Long organisationId = requireOrganisationId();

        ProjectType type = typeRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Project type not found"));

        typeRepository.delete(type);
    }

    // ================= CUSTOMERS =================

    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomers() {
        Long organisationId = requireOrganisationId();

        return customerRepository.findByOrganisation_IdOrderByNameAsc(organisationId)
                .stream()
                .map(customer -> toCustomerDto(customer, organisationId))
                .toList();
    }

    public CustomerDto createCustomer(SaveCustomerRequest request) {
        Long organisationId = requireOrganisationId();

        validateCustomerRequest(request);

        String code = normalizeUpper(request.customerCode());

        if (customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCase(organisationId, code)) {
            throw new IllegalArgumentException("Customer code already exists");
        }

        Customer customer = new Customer();
        customer.setOrganisation(getOrganisation(organisationId));
        applyCustomerRequest(customer, request);

        return toCustomerDto(customerRepository.save(customer), organisationId);
    }

    public CustomerDto updateCustomer(Long id, SaveCustomerRequest request) {
        Long organisationId = requireOrganisationId();

        validateCustomerRequest(request);

        Customer customer = customerRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String newCode = normalizeUpper(request.customerCode());
        if (!customer.getCustomerCode().equalsIgnoreCase(newCode)
                && customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCase(organisationId, newCode)) {
            throw new IllegalArgumentException("Customer code already exists");
        }

        applyCustomerRequest(customer, request);

        return toCustomerDto(customerRepository.save(customer), organisationId);
    }

    public void deleteCustomer(Long id) {
        Long organisationId = requireOrganisationId();

        Customer customer = customerRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customerRepository.delete(customer);
    }

    // ================= HELPERS =================

    private AdminLicenceAssignmentDto toLicenceDto(AdminLicenceAssignment assignment) {
        AppUser user = assignment.getUser();

        return new AdminLicenceAssignmentDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDepartmentCode(),
                user.getResourceType(),
                assignment.getLicenceType().name()
        );
    }

    private ProjectCategoryDto toCategoryDto(ProjectCategory category, Long organisationId) {
        long usage = projectRepository.countByOrganisationIdAndCategory(
                organisationId,
                category.getName()
        );

        return new ProjectCategoryDto(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getIcon(),
                category.getColor(),
                category.isActive(),
                usage
        );
    }

    private ProjectTypeDto toTypeDto(ProjectType type, Long organisationId) {
        long usage = projectRepository.countByOrganisationIdAndProjectType(
                organisationId,
                type.getName()
        );

        return new ProjectTypeDto(
                type.getId(),
                type.getName(),
                type.getCode(),
                type.getDescription(),
                type.getIcon(),
                type.getColor(),
                type.isBillable(),
                type.isActive(),
                usage
        );
    }

    private CustomerDto toCustomerDto(Customer customer, Long organisationId) {
        long usage = projectRepository.countByOrganisationIdAndCustomer(
                organisationId,
                customer.getName()
        );

        return new CustomerDto(
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
                usage
        );
    }

    private void applyCategoryRequest(ProjectCategory category, SaveProjectCategoryRequest request) {
        category.setName(normalizeRequired(request.name(), "Category name is required"));
        category.setCode(normalizeUpper(request.code()));
        category.setDescription(normalize(request.description()));
        category.setIcon(normalizeOrDefault(request.icon(), "📊"));
        category.setColor(normalizeOrDefault(request.color(), "#1565c0"));
        category.setActive(request.active() == null || request.active());
    }

    private void applyTypeRequest(ProjectType type, SaveProjectTypeRequest request) {
        type.setName(normalizeRequired(request.name(), "Project type name is required"));
        type.setCode(normalizeUpper(request.code()));
        type.setDescription(normalize(request.description()));
        type.setIcon(normalizeOrDefault(request.icon(), "📄"));
        type.setColor(normalizeOrDefault(request.color(), "#16a34a"));
        type.setBillable(Boolean.TRUE.equals(request.billable()));
        type.setActive(request.active() == null || request.active());
    }

    private void applyCustomerRequest(Customer customer, SaveCustomerRequest request) {
        customer.setCustomerCode(normalizeUpper(request.customerCode()));
        customer.setName(normalizeRequired(request.name(), "Customer name is required"));
        customer.setCountry(normalize(request.country()));
        customer.setTown(normalize(request.town()));
        customer.setAddress(normalize(request.address()));
        customer.setVatTaxId(normalize(request.vatTaxId()));
        customer.setContactPerson(normalize(request.contactPerson()));
        customer.setEmail(normalizeLower(request.email()));
        customer.setPhone(normalize(request.phone()));
        customer.setErpId(normalize(request.erpId()));
        customer.setActive(request.active() == null || request.active());
    }

    private AdminLicenceType parseLicenceType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Licence type is required");
        }

        try {
            return AdminLicenceType.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid licence type: " + value);
        }
    }

    private void validateNameAndCode(String name, String code) {
        normalizeRequired(name, "Name is required");
        normalizeRequired(code, "Code is required");
    }

    private void validateCustomerRequest(SaveCustomerRequest request) {
        normalizeRequired(request.customerCode(), "Customer code is required");
        normalizeRequired(request.name(), "Customer name is required");
    }

    private Organisation getOrganisation(Long organisationId) {
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));
    }

    private Long requireOrganisationId() {
        return securityUtils.getCurrentOrganisationId();
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}