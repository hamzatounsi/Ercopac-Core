package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.dto.CreateResourceRequest;
import com.ercopac.ercopac_tracker.user.dto.ResourceDetailsDto;
import com.ercopac.ercopac_tracker.user.dto.ResourceListItemDto;
import com.ercopac.ercopac_tracker.user.dto.ResourceOptionDto;
import com.ercopac.ercopac_tracker.user.dto.UpdateResourceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@Transactional
public class ResourceService {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    

    public ResourceService(UserRepository userRepository,
                           OrganisationRepository organisationRepository,
                           PasswordEncoder passwordEncoder,
                           SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.organisationRepository = organisationRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public Page<ResourceListItemDto> getResources(String search,
                                                  String departmentCode,
                                                  String role,
                                                  Boolean active,
                                                  Boolean internalUser,
                                                  Pageable pageable) {
        Long organisationId = getOrganisationIdOrNullForPlatform();
        Role parsedRole = parseRole(role);
        String searchPattern = toSearchPattern(search);

        return userRepository.searchResources(
                organisationId,
                searchPattern,
                normalize(departmentCode),
                parsedRole,
                active,
                internalUser,
                pageable
        ).map(this::toListItemDto);
    }

    @Transactional(readOnly = true)
    public ResourceDetailsDto getResourceById(Long id) {
        AppUser user = findAccessibleResource(id);
        return toDetailsDto(user);
    }

    public ResourceDetailsDto createResource(CreateResourceRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        validateCreateRequest(request, organisationId);

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(parseRequiredRole(request.role()));
        user.setOrganisation(organisation);

        user.setEmployeeCode(normalize(request.employeeCode()));
        user.setDepartmentCode(normalize(request.departmentCode()));
        user.setResourceType(normalize(request.resourceType()));
        user.setJobTitle(normalize(request.jobTitle()));
        user.setSeniority(normalize(request.seniority()));
        user.setInternalUser(request.internalUser() != null ? request.internalUser() : true);
        user.setHoursPerDay(request.hoursPerDay() != null ? request.hoursPerDay() : 8);
        user.setDaysPerWeek(request.daysPerWeek() != null ? request.daysPerWeek() : 5);
        user.setWorkdays(normalizeOrDefault(request.workdays(), "MON-FRI"));
        user.setDefaultRate(request.defaultRate());
        user.setRateType(normalize(request.rateType()));
        user.setCurrency(normalize(request.currency()));
        user.setColor(normalize(request.color()));
        user.setNotes(normalize(request.notes()));
        user.setActive(request.active() != null ? request.active() : true);

        AppUser saved = userRepository.save(user);
        return toDetailsDto(saved);
    }

    public ResourceDetailsDto updateResource(Long id, UpdateResourceRequest request) {
        Long organisationId = requireOrganisationIdForWrite();

        AppUser user = userRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        String employeeCode = normalize(request.employeeCode());
        if (employeeCode != null) {
            userRepository.findByOrganisation_IdAndEmployeeCode(organisationId, employeeCode)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Employee code already exists in this organisation");
                    });
            user.setEmployeeCode(employeeCode);
        } else {
            user.setEmployeeCode(null);
        }

        user.setDepartmentCode(normalize(request.departmentCode()));
        user.setResourceType(normalize(request.resourceType()));
        user.setJobTitle(normalize(request.jobTitle()));
        user.setSeniority(normalize(request.seniority()));
        user.setInternalUser(request.internalUser() != null ? request.internalUser() : user.isInternalUser());
        user.setHoursPerDay(request.hoursPerDay() != null ? request.hoursPerDay() : user.getHoursPerDay());
        user.setDaysPerWeek(request.daysPerWeek() != null ? request.daysPerWeek() : user.getDaysPerWeek());
        user.setWorkdays(normalizeOrDefault(request.workdays(), user.getWorkdays()));
        user.setDefaultRate(request.defaultRate());
        user.setRateType(normalize(request.rateType()));
        user.setCurrency(normalize(request.currency()));
        user.setColor(normalize(request.color()));
        user.setNotes(normalize(request.notes()));
        user.setActive(request.active() != null ? request.active() : user.isActive());

        AppUser saved = userRepository.save(user);
        return toDetailsDto(saved);
    }

    public void updateResourceStatus(Long id, boolean active) {
        Long organisationId = requireOrganisationIdForWrite();

        AppUser user = userRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        user.setActive(active);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<ResourceOptionDto> getResourceOptions(String departmentCode, String role) {
        Long organisationId = getOrganisationIdOrNullForPlatform();
        String normalizedDepartmentCode = normalize(departmentCode);

        if (organisationId == null) {
            return userRepository.findAll().stream()
                    .filter(AppUser::isActive)
                    .filter(u -> u.getOrganisation() != null)
                    .filter(u -> normalizedDepartmentCode == null || normalizedDepartmentCode.equals(u.getDepartmentCode()))
                    .filter(u -> role == null || role.isBlank() || (u.getRole() != null && u.getRole().name().equalsIgnoreCase(role)))
                    .map(this::toOptionDto)
                    .toList();
        }

        if (role != null && !role.isBlank()) {
            Role parsedRole = parseRole(role);
            return userRepository.findByOrganisation_IdAndRoleOrderByFullNameAsc(organisationId, parsedRole)
                    .stream()
                    .filter(AppUser::isActive)
                    .filter(u -> normalizedDepartmentCode == null || normalizedDepartmentCode.equals(u.getDepartmentCode()))
                    .map(this::toOptionDto)
                    .toList();
        }

        return userRepository.findActiveResourcesByOrganisationId(organisationId, normalizedDepartmentCode)
                .stream()
                .map(this::toOptionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getDepartmentCodes() {
        Long organisationId = getOrganisationIdOrNullForPlatform();
        if (organisationId == null) {
            return userRepository.findAll().stream()
                    .map(AppUser::getDepartmentCode)
                    .filter(v -> v != null && !v.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        }
        return userRepository.findDistinctDepartmentCodesByOrganisationId(organisationId);
    }

    @Transactional(readOnly = true)
    public List<String> getResourceTypes() {
        Long organisationId = getOrganisationIdOrNullForPlatform();
        if (organisationId == null) {
            return userRepository.findAll().stream()
                    .map(AppUser::getResourceType)
                    .filter(v -> v != null && !v.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        }
        return userRepository.findDistinctResourceTypesByOrganisationId(organisationId);
    }

    @Transactional(readOnly = true)
    public List<String> getSeniorities() {
        Long organisationId = getOrganisationIdOrNullForPlatform();
        if (organisationId == null) {
            return userRepository.findAll().stream()
                    .map(AppUser::getSeniority)
                    .filter(v -> v != null && !v.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        }
        return userRepository.findDistinctSenioritiesByOrganisationId(organisationId);
    }

    private void validateCreateRequest(CreateResourceRequest request, Long organisationId) {
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.role() == null || request.role().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String employeeCode = normalize(request.employeeCode());
        if (employeeCode != null) {
            userRepository.findByOrganisation_IdAndEmployeeCode(organisationId, employeeCode)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Employee code already exists in this organisation");
                    });
        }
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private Role parseRequiredRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized != null ? normalized : defaultValue;
    }

    private ResourceListItemDto toListItemDto(AppUser user) {
        return new ResourceListItemDto(
                user.getId(),
                user.getFullName(),
                user.getEmployeeCode(),
                user.getDepartmentCode(),
                user.getResourceType(),
                user.getJobTitle(),
                user.getEmail(),
                user.getSeniority(),
                user.isInternalUser(),
                user.getHoursPerDay(),
                user.getDaysPerWeek(),
                user.getWorkdays(),
                user.getDefaultRate(),
                user.getRateType(),
                user.getCurrency(),
                user.getColor(),
                user.isActive()
        );
    }

    private ResourceDetailsDto toDetailsDto(AppUser user) {
        return new ResourceDetailsDto(
                user.getId(),
                user.getFullName(),
                user.getEmployeeCode(),
                user.getDepartmentCode(),
                user.getResourceType(),
                user.getJobTitle(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getSeniority(),
                user.isInternalUser(),
                user.getHoursPerDay(),
                user.getDaysPerWeek(),
                user.getWorkdays(),
                user.getDefaultRate(),
                user.getRateType(),
                user.getCurrency(),
                user.getColor(),
                user.getNotes(),
                user.isActive()
        );
    }

    private ResourceOptionDto toOptionDto(AppUser user) {
        return new ResourceOptionDto(
                user.getId(),
                user.getFullName(),
                user.getDepartmentCode()
        );
    }

    private Long requireOrganisationIdForWrite() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            throw new IllegalArgumentException("No organisation context found for current user.");
        }
        return organisationId;
    }

    private String toSearchPattern(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : "%" + normalized.toLowerCase() + "%";
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

    private AppUser findAccessibleResource(Long id) {
        Long organisationId = getOrganisationIdOrNullForPlatform();

        if (organisationId == null) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        }

        return userRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    @Transactional(readOnly = true)
    public List<ResourceOptionDto> getResourceOptionsForProject(Long projectId) {
        return getResourceOptions(null, null);
    }
}