package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.dto.CreateSupplierRequest;
import com.ercopac.ercopac_tracker.user.dto.SupplierDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateSupplierRequest;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final OrganisationRepository organisationRepository;
    private final SecurityUtils securityUtils;

    public SupplierService(
            SupplierRepository supplierRepository,
            OrganisationRepository organisationRepository,
            SecurityUtils securityUtils
    ) {
        this.supplierRepository = supplierRepository;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
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

        return supplierRepository.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SupplierDto createSupplier(CreateSupplierRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Supplier name is required");
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        Supplier supplier = new Supplier();
        supplier.setOrganisation(organisation);
        supplier.setName(request.name().trim());
        supplier.setShortCode(normalize(request.shortCode()));
        supplier.setCountry(normalize(request.country()));
        supplier.setContact(normalize(request.contact()));
        supplier.setWebsite(normalize(request.website()));
        supplier.setDepartmentsCsv(toCsv(request.departments()));
        supplier.setResourceTypesCsv(toCsv(request.resourceTypes()));
        supplier.setNotes(normalize(request.notes()));
        supplier.setActive(true);

        return toDto(supplierRepository.save(supplier));
    }

    public SupplierDto updateSupplier(Long id, UpdateSupplierRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        Supplier supplier = supplierRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        if (request.name() != null && !request.name().isBlank()) {
            supplier.setName(request.name().trim());
        }

        supplier.setShortCode(normalize(request.shortCode()));
        supplier.setCountry(normalize(request.country()));
        supplier.setContact(normalize(request.contact()));
        supplier.setWebsite(normalize(request.website()));
        supplier.setDepartmentsCsv(toCsv(request.departments()));
        supplier.setResourceTypesCsv(toCsv(request.resourceTypes()));
        supplier.setNotes(normalize(request.notes()));

        return toDto(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Long id) {
        Long organisationId = requireOrganisationIdForWrite();

        Supplier supplier = supplierRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        supplier.setActive(false);
        supplierRepository.save(supplier);
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
                fromCsv(supplier.getResourceTypesCsv()),
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
            if (securityUtils.hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "OWNER")) {
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

    private String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.stream()
                .map(this::normalize)
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
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