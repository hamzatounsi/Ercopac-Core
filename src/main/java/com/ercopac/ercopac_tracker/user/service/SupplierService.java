package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.DepartmentScope;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.dto.CreateSupplierRequest;
import com.ercopac.ercopac_tracker.user.dto.SupplierDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateSupplierRequest;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Supplier CRUD for {@code /api/suppliers}.
 *
 * <p>Department Managers are limited by {@link DepartmentScope}: they only list, view, edit and
 * delete suppliers linked to at least one resource type of their department, and they can only
 * link suppliers to those resource types. Resource types outside their scope that a supplier
 * already has are kept untouched when they edit it.
 */
@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final OrganisationRepository organisationRepository;
    private final SecurityUtils securityUtils;
    private final ResourceTypeRepository resourceTypeRepository;
    private final DepartmentScope departmentScope;

    public SupplierService(
            SupplierRepository supplierRepository,
            OrganisationRepository organisationRepository,
            SecurityUtils securityUtils,
            ResourceTypeRepository resourceTypeRepository,
            DepartmentScope departmentScope
    ) {
        this.supplierRepository = supplierRepository;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
        this.resourceTypeRepository = resourceTypeRepository;
        this.departmentScope = departmentScope;
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> getSuppliers() {
        Long organisationId = getOrganisationIdOrNullForPlatform();

        if (organisationId == null) {
            return supplierRepository.findAll().stream()
                    .filter(Supplier::isActive)
                    .map(this::toDto)
                    .toList();
        }

        DepartmentScope.Scope scope = departmentScope.current();
        return supplierRepository.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId)
                .stream()
                .filter(scope::allowsSupplier)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplier(Long id) {
        Long organisationId = requireOrganisationIdForWrite();
        Supplier supplier = findSupplier(id, organisationId);
        departmentScope.assertSupplierAccess(supplier);
        return toDto(supplier);
    }

    public SupplierDto createSupplier(CreateSupplierRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier name is required");
        }

        DepartmentScope.Scope scope = departmentScope.current();
        Set<ResourceType> resourceTypes = resolveResourceTypes(request.resourceTypes(), organisationId);
        if (scope.restricted()) {
            assertTypesInScope(scope, resourceTypes, Set.of());
            requireTypeInScope(scope, resourceTypes);
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found"));

        Supplier supplier = new Supplier();
        supplier.setOrganisation(organisation);
        supplier.setName(request.name().trim());
        supplier.setShortCode(normalize(request.shortCode()));
        supplier.setCountry(normalize(request.country()));
        supplier.setContact(normalize(request.contact()));
        supplier.setWebsite(normalize(request.website()));
        supplier.setDepartmentsCsv(departmentsCsv(request.departments(), scope));
        supplier.setResourceTypes(resourceTypes);
        supplier.setNotes(normalize(request.notes()));
        supplier.setActive(true);

        return toDto(supplierRepository.save(supplier));
    }

    public SupplierDto updateSupplier(Long id, UpdateSupplierRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        Supplier supplier = findSupplier(id, organisationId);
        DepartmentScope.Scope scope = departmentScope.current();
        if (!scope.allowsSupplier(supplier)) {
            throw outOfScope();
        }

        Set<ResourceType> requested = resolveResourceTypes(request.resourceTypes(), organisationId);
        Set<ResourceType> resourceTypes = requested;
        if (scope.restricted()) {
            assertTypesInScope(scope, requested, supplier.getResourceTypes());
            // Keep the types this manager cannot see or change.
            resourceTypes = new LinkedHashSet<>(requested);
            for (ResourceType existing : supplier.getResourceTypes()) {
                if (!scope.allowsResourceType(existing.getCode())) {
                    resourceTypes.add(existing);
                }
            }
            requireTypeInScope(scope, resourceTypes);
        }

        if (request.name() != null && !request.name().isBlank()) {
            supplier.setName(request.name().trim());
        }

        supplier.setShortCode(normalize(request.shortCode()));
        supplier.setCountry(normalize(request.country()));
        supplier.setContact(normalize(request.contact()));
        supplier.setWebsite(normalize(request.website()));
        supplier.setDepartmentsCsv(departmentsCsv(request.departments(), scope));
        supplier.setResourceTypes(resourceTypes);
        supplier.setNotes(normalize(request.notes()));

        return toDto(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Long id) {
        Long organisationId = requireOrganisationIdForWrite();

        Supplier supplier = findSupplier(id, organisationId);
        departmentScope.assertSupplierAccess(supplier);

        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private Supplier findSupplier(Long id, Long organisationId) {
        return supplierRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    /** Every requested type must be in scope, except types the supplier already had. */
    private void assertTypesInScope(DepartmentScope.Scope scope, Set<ResourceType> requested, Set<ResourceType> existing) {
        Set<Long> existingIds = existing.stream().map(ResourceType::getId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> refused = requested.stream()
                .filter(type -> !scope.allowsResourceType(type.getCode()))
                .filter(type -> type.getId() == null || !existingIds.contains(type.getId()))
                .map(ResourceType::getCode)
                .sorted()
                .toList();
        if (!refused.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Resource type(s) outside your department: " + String.join(", ", refused));
        }
    }

    /** A Department Manager's supplier must keep at least one of their resource types, or they would lose it. */
    private void requireTypeInScope(DepartmentScope.Scope scope, Set<ResourceType> resourceTypes) {
        boolean anyInScope = resourceTypes.stream().map(ResourceType::getCode).anyMatch(scope::allowsResourceType);
        if (!anyInScope) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Select at least one resource type of your department.");
        }
    }

    private ResponseStatusException outOfScope() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This supplier is outside the resource types of your department.");
    }

    private SupplierDto toDto(Supplier supplier) {
        return new SupplierDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getShortCode(),
                supplier.getCountry(),
                supplier.getContact(),
                supplier.getWebsite(),
                fromCsv(supplier.getDepartmentsCsv()),
                supplier.getResourceTypes().stream().map(ResourceType::getCode).sorted().toList(),
                supplier.getNotes()
        );
    }

    private Long requireOrganisationIdForWrite() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            throw new IllegalStateException("No organisation context found for current user.");
        }
        return organisationId;
    }

    private Long getOrganisationIdOrNullForPlatform() {
        try {
            return securityUtils.getCurrentOrganisationId();
        } catch (IllegalStateException ex) {
            if (securityUtils.hasAnyRole("PLATFORM_OWNER")) {
                return null;
            }
            throw ex;
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** A Department Manager's new supplier defaults to their department when none is given. */
    private String departmentsCsv(List<String> departments, DepartmentScope.Scope scope) {
        String csv = toCsv(departments);
        if (csv == null && scope.restricted() && scope.departmentCode() != null) {
            return scope.departmentCode();
        }
        return csv;
    }

    private String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        String csv = values.stream()
                .map(this::normalize)
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        return csv.isEmpty() ? null : csv;
    }

    private Set<ResourceType> resolveResourceTypes(List<String> codes, Long organisationId) {
        if (codes == null || codes.isEmpty()) return new LinkedHashSet<>();
        Set<String> normalizedCodes = codes.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return normalizedCodes.stream()
                .map(code -> resourceTypeRepository.findByCodeAndOrganisation_Id(code, organisationId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Resource type not found in supplier organisation: " + code)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
    }
}
