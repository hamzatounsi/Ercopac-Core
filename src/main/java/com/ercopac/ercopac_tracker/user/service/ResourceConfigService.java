package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.dto.DepartmentDto;
import com.ercopac.ercopac_tracker.department.dto.SaveDepartmentRequest;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.dto.ResourceTypeConfigDto;
import com.ercopac.ercopac_tracker.user.dto.SaveResourceTypeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResourceConfigService {

    private final SecurityUtils securityUtils;
    private final OrganisationRepository organisationRepository;
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceConfigService(
            SecurityUtils securityUtils,
            OrganisationRepository organisationRepository,
            DepartmentRepository departmentRepository,
            ResourceTypeRepository resourceTypeRepository
    ) {
        this.securityUtils = securityUtils;
        this.organisationRepository = organisationRepository;
        this.departmentRepository = departmentRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartments() {
        Long organisationId = requireOrganisationId();
        return departmentRepository.findByOrganisation_IdOrderByCodeAsc(organisationId)
                .stream()
                .map(this::toDepartmentDto)
                .toList();
    }

    public DepartmentDto createDepartment(SaveDepartmentRequest request) {
        Long organisationId = requireOrganisationId();

        String code = normalizeUpper(request.code());
        if (code == null) {
            throw new IllegalArgumentException("Department code is required");
        }

        if (departmentRepository.existsByCodeAndOrganisation_Id(code, organisationId)) {
            throw new IllegalArgumentException("Department already exists");
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        Department department = new Department();
        department.setCode(code);
        department.setLabel(normalize(request.label()) != null ? normalize(request.label()) : code);
        department.setOrganisation(organisation);

        return toDepartmentDto(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<ResourceTypeConfigDto> getResourceTypes() {
        Long organisationId = requireOrganisationId();

        return resourceTypeRepository.findByOrganisation_IdOrderByCodeAsc(organisationId)
                .stream()
                .map(this::toResourceTypeDto)
                .toList();
    }

    public ResourceTypeConfigDto createResourceType(SaveResourceTypeRequest request) {
        Long organisationId = requireOrganisationId();

        String code = normalizeUpper(request.code());
        if (code == null) {
            throw new IllegalArgumentException("Resource type code is required");
        }

        if (resourceTypeRepository.existsByCodeAndOrganisation_Id(code, organisationId)) {
            throw new IllegalArgumentException("Resource type already exists");
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        ResourceType type = new ResourceType();
        type.setCode(code);
        type.setLabel(normalize(request.label()) != null ? normalize(request.label()) : code);
        type.setOrganisation(organisation);
        applyResourceTypeFields(type, request, organisationId);

        return toResourceTypeDto(resourceTypeRepository.save(type));
    }

    public ResourceTypeConfigDto updateResourceType(Long id, SaveResourceTypeRequest request) {
        Long organisationId = requireOrganisationId();

        ResourceType type = resourceTypeRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found"));

        String code = normalizeUpper(request.code());
        if (code != null && !code.equals(type.getCode())) {
            if (resourceTypeRepository.existsByCodeAndOrganisation_Id(code, organisationId)) {
                throw new IllegalArgumentException("Resource type code already exists");
            }
            type.setCode(code);
        }

        if (request.label() != null) {
            type.setLabel(normalize(request.label()));
        }

        applyResourceTypeFields(type, request, organisationId);

        return toResourceTypeDto(resourceTypeRepository.save(type));
    }

    public void deleteResourceType(Long id) {
        Long organisationId = requireOrganisationId();

        ResourceType type = resourceTypeRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found"));

        type.setActive(false);
        resourceTypeRepository.save(type);
    }

    private void applyResourceTypeFields(ResourceType type, SaveResourceTypeRequest request, Long organisationId) {
        type.setColour(normalize(request.colour()));
        type.setDefaultRate(request.defaultRate());

        if (request.assignable() != null) {
            type.setAssignable(request.assignable());
        }

        if (request.active() != null) {
            type.setActive(request.active());
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganisation_Id(request.departmentId(), organisationId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            type.setDepartment(department);
        }
    }

    private DepartmentDto toDepartmentDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getCode(),
                department.getLabel()
        );
    }

    private ResourceTypeConfigDto toResourceTypeDto(ResourceType type) {
        return new ResourceTypeConfigDto(
                type.getId(),
                type.getCode(),
                type.getLabel(),
                type.getColour(),
                type.getDepartment() != null ? type.getDepartment().getId() : null,
                type.getDepartment() != null ? type.getDepartment().getCode() : null,
                type.getDefaultRate(),
                type.isAssignable(),
                type.isActive()
        );
    }

    private Long requireOrganisationId() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            throw new IllegalArgumentException("No organisation context found");
        }
        return organisationId;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}