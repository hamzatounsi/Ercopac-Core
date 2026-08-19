package com.ercopac.ercopac_tracker.org_admin.service;

import com.ercopac.ercopac_tracker.auth.passwordreset.PasswordResetRequestRepository;
import com.ercopac.ercopac_tracker.auth.passwordreset.PasswordResetStatus;
import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import com.ercopac.ercopac_tracker.user.repository.SupplierRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
public class OrganisationAdminService {

    private static final List<Role> ASSIGNABLE_ROLES = List.of(
            Role.ORG_ADMIN,
            Role.PROJECT_MANAGER,
            Role.PROJECT_MANAGER_LEAD,
            Role.MANAGER,
            Role.DEPARTMENT_MANAGER,
            Role.EMPLOYEE,
            Role.SALES_MANAGER,
            Role.CLIENT
    );

    private static final Set<String> SESSION_TIMEOUTS = Set.of(
            "1_HOUR", "4_HOURS", "8_HOURS", "12_HOURS"
    );

    private static final Set<PermissionModule> PLATFORM_MODULES = EnumSet.of(
            PermissionModule.OWNER_DASHBOARD,
            PermissionModule.ORGANISATIONS,
            PermissionModule.BILLING,
            PermissionModule.PLATFORM_ANALYTICS,
            PermissionModule.INFRASTRUCTURE,
            PermissionModule.SUPPORT,
            PermissionModule.PERMISSIONS,
            PermissionModule.PLATFORM_SETTINGS
    );

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)*[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Map<String, String> USER_SORTS = Map.of(
            "id", "id",
            "fullName", "fullName",
            "email", "email",
            "role", "role",
            "department", "department.label",
            "active", "active"
    );

    private final SecurityUtils securityUtils;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final SupplierRepository supplierRepository;
    private final ProjectTaskRepository taskRepository;
    private final PasswordResetRequestRepository passwordResetRepository;
    private final RolePermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganisationAdminService(
            SecurityUtils securityUtils,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            ResourceTypeRepository resourceTypeRepository,
            SupplierRepository supplierRepository,
            ProjectTaskRepository taskRepository,
            PasswordResetRequestRepository passwordResetRepository,
            RolePermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.securityUtils = securityUtils;
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.supplierRepository = supplierRepository;
        this.taskRepository = taskRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public OrgAdminDtos.Overview getOverview() {
        Organisation organisation = currentOrganisation();
        Long organisationId = organisation.getId();
        long totalUsers = userRepository.countByOrganisation_Id(organisationId);
        long activeUsers = userRepository.countByOrganisation_IdAndActiveTrue(organisationId);
        long departments = departmentRepository.countByOrganisation_Id(organisationId);
        long pendingResets = passwordResetRepository.countByOrganisationIdAndStatus(
                organisationId,
                PasswordResetStatus.PENDING
        );

        List<OrgAdminDtos.RoleCount> roleCounts = ASSIGNABLE_ROLES.stream()
                .map(role -> new OrgAdminDtos.RoleCount(
                        role.name(),
                        roleLabel(role),
                        userRepository.countByOrganisation_IdAndRole(organisationId, role),
                        userRepository.countByOrganisation_IdAndRoleAndActiveTrue(organisationId, role)
                ))
                .toList();

        List<String> warnings = new ArrayList<>();
        if (departments == 0) {
            warnings.add("Create at least one department to complete organisation setup.");
        }
        long usersWithoutDepartment = userRepository.countActiveUsersWithoutDepartment(organisationId);
        if (usersWithoutDepartment > 0) {
            warnings.add(usersWithoutDepartment + " active user(s) are not assigned to a department.");
        }
        if (organisation.getUserLimit() > 0 && activeUsers >= organisation.getUserLimit()) {
            warnings.add("The active user capacity has been reached.");
        }
        if (pendingResets > 0) {
            warnings.add(pendingResets + " password reset request(s) require review.");
        }

        return new OrgAdminDtos.Overview(
                toProfile(organisation),
                totalUsers,
                activeUsers,
                Math.max(0, totalUsers - activeUsers),
                departments,
                pendingResets,
                roleCounts,
                warnings
        );
    }

    @Transactional(readOnly = true)
    public OrgAdminDtos.OrganisationProfile getProfile() {
        return toProfile(currentOrganisation());
    }

    public OrgAdminDtos.OrganisationProfile updateProfile(OrgAdminDtos.UpdateProfileRequest request) {
        Organisation organisation = currentOrganisation();
        String name = request.name().trim();

        organisationRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(organisation.getId()))
                .ifPresent(existing -> {
                    throw conflict("Organisation name already exists.");
                });

        String domain = normalize(request.domain());
        if (domain != null && !DOMAIN_PATTERN.matcher(domain).matches()) {
            throw new IllegalArgumentException("Enter a valid organisation domain.");
        }

        organisation.setName(name);
        organisation.setCountry(normalize(request.country()));
        organisation.setDomain(domain == null ? null : domain.toLowerCase(Locale.ROOT));
        return toProfile(organisationRepository.save(organisation));
    }

    @Transactional(readOnly = true)
    public OrgAdminDtos.SecuritySettings getSecuritySettings() {
        return toSecuritySettings(currentOrganisation());
    }

    public OrgAdminDtos.SecuritySettings updateSecuritySettings(
            OrgAdminDtos.UpdateSecuritySettingsRequest request
    ) {
        Organisation organisation = currentOrganisation();
        String sessionTimeout = request.sessionTimeout().trim().toUpperCase(Locale.ROOT);
        if (!SESSION_TIMEOUTS.contains(sessionTimeout)) {
            throw new IllegalArgumentException("Invalid session timeout.");
        }

        organisation.setSessionTimeout(sessionTimeout);
        organisation.setMaxFailedLogins(request.maxFailedLogins());
        organisation.setPasswordMinLength(request.passwordMinLength());
        return toSecuritySettings(organisationRepository.save(organisation));
    }

    @Transactional(readOnly = true)
    public OrgAdminDtos.PageResponse<OrgAdminDtos.UserSummary> getUsers(
            String search,
            Long departmentId,
            String role,
            Boolean active,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Long organisationId = currentOrganisationId();
        Role parsedRole = role == null || role.isBlank() ? null : parseAssignableRole(role);
        String sortProperty = USER_SORTS.getOrDefault(sort, "fullName");
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page),
                Math.max(5, Math.min(100, size)),
                Sort.by(sortDirection, sortProperty)
        );

        Page<OrgAdminDtos.UserSummary> result = userRepository.searchOrganisationUsers(
                organisationId,
                searchPattern(search),
                departmentId,
                parsedRole,
                active,
                pageRequest
        ).map(this::toUserSummary);

        return new OrgAdminDtos.PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public OrgAdminDtos.UserSummary createUser(OrgAdminDtos.CreateUserRequest request) {
        Organisation organisation = currentOrganisation();
        Long organisationId = organisation.getId();
        Role role = parseAssignableRole(request.role());
        boolean active = request.active() == null || request.active();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw conflict("Email address already exists.");
        }
        validatePassword(request.password(), organisation);
        validateEmployeeCode(request.employeeCode(), organisationId, null);
        if (active) {
            enforceActiveCapacity(organisation);
            enforceRoleCapacity(organisation, role);
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setOrganisation(organisation);
        user.setInternalUser(role.requiresResourceProfile());
        user.setEmployeeCode(normalizeUpper(request.employeeCode()));
        user.setJobTitle(normalize(request.jobTitle()));
        user.setActive(active);
        applyResourceProfile(user, role, request.departmentId(), request.resourceTypeId(), organisationId);

        return toUserSummary(userRepository.save(user));
    }

    public OrgAdminDtos.UserSummary updateUser(Long id, OrgAdminDtos.UpdateUserRequest request) {
        Organisation organisation = currentOrganisation();
        Long organisationId = organisation.getId();
        AppUser user = findOrganisationUser(id, organisationId);
        Role targetRole = parseAssignableRole(request.role());
        boolean targetActive = request.active();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (user.getId().equals(securityUtils.getCurrentUserId())
                && (!email.equalsIgnoreCase(user.getEmail())
                || targetRole != user.getRole()
                || !targetActive)) {
            throw conflict("You cannot change your own email, role, or active status during an active session.");
        }

        ensureRequiredAdminRemains(user, targetRole, targetActive, organisationId);

        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw conflict("Email address already exists.");
                });
        validateEmployeeCode(request.employeeCode(), organisationId, user.getId());

        if (targetActive && (!user.isActive() || targetRole != user.getRole())) {
            if (!user.isActive()) {
                enforceActiveCapacity(organisation);
            }
            enforceRoleCapacity(organisation, targetRole);
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setRole(targetRole);
        user.setInternalUser(targetRole.requiresResourceProfile());
        user.setEmployeeCode(normalizeUpper(request.employeeCode()));
        user.setJobTitle(normalize(request.jobTitle()));
        user.setActive(targetActive);
        applyResourceProfile(user, targetRole, request.departmentId(), request.resourceTypeId(), organisationId);

        return toUserSummary(userRepository.save(user));
    }

    public OrgAdminDtos.UserSummary updateUserStatus(Long id, boolean active) {
        Organisation organisation = currentOrganisation();
        Long organisationId = organisation.getId();
        AppUser user = findOrganisationUser(id, organisationId);

        if (user.getId().equals(securityUtils.getCurrentUserId()) && !active) {
            throw conflict("You cannot deactivate your own account.");
        }
        ensureRequiredAdminRemains(user, user.getRole(), active, organisationId);
        if (active && !user.isActive()) {
            enforceActiveCapacity(organisation);
            enforceRoleCapacity(organisation, user.getRole());
        }

        user.setActive(active);
        return toUserSummary(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<OrgAdminDtos.DepartmentSummary> getDepartments() {
        Long organisationId = currentOrganisationId();
        return departmentRepository.findByOrganisation_IdOrderByCodeAsc(organisationId)
                .stream()
                .map(department -> toDepartmentSummary(department, organisationId))
                .toList();
    }

    public OrgAdminDtos.DepartmentSummary createDepartment(OrgAdminDtos.SaveDepartmentRequest request) {
        Organisation organisation = currentOrganisation();
        Long organisationId = organisation.getId();
        String code = normalizeUpper(request.code());
        String name = request.name().trim();
        validateUniqueDepartment(code, name, organisationId, null);

        Department department = new Department();
        department.setOrganisation(organisation);
        department.setCode(code);
        department.setLabel(name);
        department = departmentRepository.save(department);
        assignManager(department, request.managerId(), organisationId);

        return toDepartmentSummary(departmentRepository.save(department), organisationId);
    }

    public OrgAdminDtos.DepartmentSummary updateDepartment(
            Long id,
            OrgAdminDtos.SaveDepartmentRequest request
    ) {
        Long organisationId = currentOrganisationId();
        Department department = findDepartment(id, organisationId);
        String oldCode = department.getCode();
        String code = normalizeUpper(request.code());
        String name = request.name().trim();
        validateUniqueDepartment(code, name, organisationId, id);

        department.setCode(code);
        department.setLabel(name);
        assignManager(department, request.managerId(), organisationId);
        department = departmentRepository.save(department);

        if (!oldCode.equalsIgnoreCase(code)) {
            propagateDepartmentCode(department, oldCode, organisationId);
        }

        return toDepartmentSummary(department, organisationId);
    }

    public void deleteDepartment(Long id) {
        Long organisationId = currentOrganisationId();
        Department department = findDepartment(id, organisationId);
        long userReferences = userRepository.countOrganisationUsersInDepartment(
                organisationId,
                id,
                department.getCode()
        );
        long taskReferences = Math.max(
                taskRepository.countByDepartment_IdAndOrganisationId(id, organisationId),
                taskRepository.countByDepartmentCodeAndOrganisationId(department.getCode(), organisationId)
        );

        if (userReferences > 0 || taskReferences > 0) {
            throw conflict("Department cannot be deleted while users or project tasks still reference it.");
        }

        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public List<OrgAdminDtos.SupplierSummary> getSuppliers() {
        return supplierRepository.findByOrganisation_IdOrderByNameAsc(currentOrganisationId())
                .stream()
                .map(this::toSupplierSummary)
                .toList();
    }

    public OrgAdminDtos.SupplierSummary createSupplier(OrgAdminDtos.SaveSupplierRequest request) {
        Organisation organisation = currentOrganisation();
        String code = normalizeUpper(request.code());
        if (supplierRepository.countByOrganisationAndCodeOrLegacyShortCode(organisation.getId(), code) > 0) {
            throw conflict("Supplier code already exists.");
        }

        Supplier supplier = new Supplier();
        supplier.setOrganisation(organisation);
        applySupplier(supplier, request, code, organisation.getId());
        return toSupplierSummary(supplierRepository.save(supplier));
    }

    public OrgAdminDtos.SupplierSummary updateSupplier(Long id, OrgAdminDtos.SaveSupplierRequest request) {
        Long organisationId = currentOrganisationId();
        Supplier supplier = supplierRepository.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> notFound("Supplier not found."));
        String code = normalizeUpper(request.code());
        if (supplierRepository.countByOrganisationAndCodeOrLegacyShortCodeExcludingId(organisationId, code, id) > 0) {
            throw conflict("Supplier code already exists.");
        }

        applySupplier(supplier, request, code, organisationId);
        return toSupplierSummary(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findByIdAndOrganisation_Id(id, currentOrganisationId())
                .orElseThrow(() -> notFound("Supplier not found."));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public List<OrgAdminDtos.RoleSummary> getRoles() {
        Long organisationId = currentOrganisationId();
        return ASSIGNABLE_ROLES.stream()
                .map(role -> new OrgAdminDtos.RoleSummary(
                        role.name(),
                        roleLabel(role),
                        roleDescription(role),
                        userRepository.countByOrganisation_IdAndRole(organisationId, role),
                        userRepository.countByOrganisation_IdAndRoleAndActiveTrue(organisationId, role),
                        permissionViews(organisationId, role)
                ))
                .toList();
    }

    private List<OrgAdminDtos.PermissionView> permissionViews(Long organisationId, Role role) {
        if (role == Role.ORG_ADMIN) {
            return List.of(new OrgAdminDtos.PermissionView(
                    "ORGANISATION_ADMIN",
                    "Organisation administration",
                    true,
                    true
            ));
        }

        return permissionRepository.findByOrganisation_IdAndRole(organisationId, role)
                .stream()
                .filter(permission -> !PLATFORM_MODULES.contains(permission.getModule()))
                .filter(permission -> permission.isCanRead() || permission.isCanWrite())
                .sorted(java.util.Comparator.comparing(permission -> permission.getModule().name()))
                .map(this::toPermissionView)
                .toList();
    }

    private OrgAdminDtos.PermissionView toPermissionView(RolePermission permission) {
        return new OrgAdminDtos.PermissionView(
                permission.getModule().name(),
                titleCase(permission.getModule().name()),
                permission.isCanRead(),
                permission.isCanWrite()
        );
    }

    private void validatePassword(String password, Organisation organisation) {
        int minimumLength = Math.max(8, organisation.getPasswordMinLength());
        if (password == null || password.length() < minimumLength) {
            throw new IllegalArgumentException(
                    "Temporary password must contain at least " + minimumLength + " characters."
            );
        }
    }

    private void validateEmployeeCode(String value, Long organisationId, Long currentUserId) {
        String employeeCode = normalizeUpper(value);
        if (employeeCode == null) {
            return;
        }
        userRepository.findByOrganisation_IdAndEmployeeCodeIgnoreCase(organisationId, employeeCode)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw conflict("Employee code already exists in this organisation.");
                });
    }

    private void applyResourceProfile(
            AppUser user,
            Role role,
            Long departmentId,
            Long resourceTypeId,
            Long organisationId
    ) {
        if (!role.requiresResourceProfile()) {
            user.setDepartment(null);
            user.setDepartmentCode(null);
            user.setResourceType(null);
            return;
        }

        if (departmentId == null) {
            throw new IllegalArgumentException("Department is required for " + roleLabel(role) + ".");
        }
        Department department = findDepartment(departmentId, organisationId);
        if (resourceTypeId == null) {
            throw new IllegalArgumentException("Resource type is required for " + roleLabel(role) + ".");
        }
        ResourceType resourceType = resourceTypeRepository.findByIdAndOrganisation_Id(resourceTypeId, organisationId)
                .orElseThrow(() -> notFound("Resource type not found."));
        if (!resourceType.isActive() || !resourceType.isAssignable()) {
            throw new IllegalArgumentException("Resource type is not available for assignment.");
        }
        user.setDepartment(department);
        user.setDepartmentCode(department.getCode());
        user.setResourceType(resourceType);
    }

    private void assignManager(Department department, Long managerId, Long organisationId) {
        if (managerId == null) {
            department.setManager(null);
            return;
        }

        AppUser manager = findOrganisationUser(managerId, organisationId);
        if (!manager.isActive() || manager.getRole() != Role.DEPARTMENT_MANAGER) {
            throw new IllegalArgumentException("Department manager must be an active Department Manager.");
        }

        department.setManager(manager);
        manager.setDepartment(department);
        manager.setDepartmentCode(department.getCode());
        userRepository.save(manager);
    }

    private void propagateDepartmentCode(Department department, String oldCode, Long organisationId) {
        List<AppUser> users = userRepository.findByOrganisation_IdAndDepartmentCodeOrderByFullNameAsc(
                organisationId,
                oldCode
        );
        users.forEach(user -> {
            user.setDepartment(department);
            user.setDepartmentCode(department.getCode());
        });
        userRepository.saveAll(users);

        List<ProjectTask> tasks = taskRepository.findByDepartmentCodeAndOrganisationIdOrderByDisplayOrderAscIdAsc(
                oldCode,
                organisationId
        );
        tasks.forEach(task -> {
            task.setDepartment(department);
            task.setDepartmentCode(department.getCode());
        });
        taskRepository.saveAll(tasks);
    }

    private void validateUniqueDepartment(
            String code,
            String name,
            Long organisationId,
            Long currentDepartmentId
    ) {
        boolean duplicateCode = currentDepartmentId == null
                ? departmentRepository.existsByOrganisation_IdAndCodeIgnoreCase(organisationId, code)
                : departmentRepository.existsByOrganisation_IdAndCodeIgnoreCaseAndIdNot(
                        organisationId,
                        code,
                        currentDepartmentId
                );
        if (duplicateCode) {
            throw conflict("Department code already exists.");
        }

        boolean duplicateName = currentDepartmentId == null
                ? departmentRepository.existsByOrganisation_IdAndLabelIgnoreCase(organisationId, name)
                : departmentRepository.existsByOrganisation_IdAndLabelIgnoreCaseAndIdNot(
                        organisationId,
                        name,
                        currentDepartmentId
                );
        if (duplicateName) {
            throw conflict("Department name already exists.");
        }
    }

    private void ensureRequiredAdminRemains(
            AppUser user,
            Role targetRole,
            boolean targetActive,
            Long organisationId
    ) {
        boolean removesActiveAdmin = user.getRole() == Role.ORG_ADMIN
                && user.isActive()
                && (targetRole != Role.ORG_ADMIN || !targetActive);

        if (removesActiveAdmin
                && userRepository.countByOrganisation_IdAndRoleAndActiveTrue(
                organisationId,
                Role.ORG_ADMIN
        ) <= 1) {
            throw conflict("At least one active Organisation Admin is required.");
        }
    }

    private void enforceActiveCapacity(Organisation organisation) {
        if (userRepository.countByOrganisation_IdAndActiveTrue(organisation.getId())
                >= organisation.getUserLimit()) {
            throw conflict("The organisation active user limit has been reached.");
        }
    }

    private void enforceRoleCapacity(Organisation organisation, Role role) {
        int limit = switch (role) {
            case ORG_ADMIN -> organisation.getOrgAdminLicenceLimit();
            case PROJECT_MANAGER, PROJECT_MANAGER_LEAD -> organisation.getProjectManagerLicenceLimit();
            case DEPARTMENT_MANAGER -> organisation.getDepartmentManagerLicenceLimit();
            case EMPLOYEE -> organisation.getEmployeeLicenceLimit();
            case MANAGER -> Integer.MAX_VALUE;
            case PLATFORM_OWNER -> 0;
            case SALES_MANAGER -> organisation.getSalesManagerLicenceLimit();
            case CLIENT -> organisation.getClientLicenceLimit();
        };

        long used = role.isProjectManagerRole()
                ? userRepository.countByOrganisation_IdAndRoleInAndActiveTrue(
                        organisation.getId(), List.of(Role.PROJECT_MANAGER, Role.PROJECT_MANAGER_LEAD))
                : userRepository.countByOrganisation_IdAndRoleAndActiveTrue(organisation.getId(), role);
        if (used >= limit) {
            throw conflict("No active " + roleLabel(role) + " licence is available.");
        }
    }

    private Role parseAssignableRole(String value) {
        try {
            Role role = Role.valueOf(value.trim().toUpperCase(Locale.ROOT).replaceFirst("^ROLE_", ""));
            if (!ASSIGNABLE_ROLES.contains(role)) {
                throw new IllegalArgumentException("Role cannot be assigned by an Organisation Admin.");
            }
            return role;
        } catch (NullPointerException | IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("cannot be assigned")) {
                throw exception;
            }
            throw new IllegalArgumentException("Invalid organisation role.");
        }
    }

    private AppUser findOrganisationUser(Long userId, Long organisationId) {
        return userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> notFound("User not found."));
    }

    private Department findDepartment(Long departmentId, Long organisationId) {
        return departmentRepository.findByIdAndOrganisation_Id(departmentId, organisationId)
                .orElseThrow(() -> notFound("Department not found."));
    }

    private Organisation currentOrganisation() {
        Long organisationId = currentOrganisationId();
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> notFound("Organisation not found."));
    }

    private Long currentOrganisationId() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        if (organisationId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Organisation context is required.");
        }
        return organisationId;
    }

    private OrgAdminDtos.OrganisationProfile toProfile(Organisation organisation) {
        return new OrgAdminDtos.OrganisationProfile(
                organisation.getName(),
                organisation.getCode(),
                organisation.getCountry(),
                organisation.getDomain(),
                organisation.getStatus().name(),
                organisation.getPlan(),
                organisation.getUserLimit(),
                organisation.getOrgAdminLicenceLimit(),
                organisation.getProjectManagerLicenceLimit(),
                organisation.getDepartmentManagerLicenceLimit(),
                organisation.getEmployeeLicenceLimit(),
                organisation.getSalesManagerLicenceLimit(),
                organisation.getClientLicenceLimit(),
                organisation.getCreatedAt()
        );
    }

    private OrgAdminDtos.SecuritySettings toSecuritySettings(Organisation organisation) {
        return new OrgAdminDtos.SecuritySettings(
                normaliseSessionTimeout(organisation.getSessionTimeout()),
                Math.max(3, organisation.getMaxFailedLogins()),
                Math.max(8, organisation.getPasswordMinLength())
        );
    }

    private OrgAdminDtos.UserSummary toUserSummary(AppUser user) {
        Department department = user.getDepartment();
        ResourceType resourceType = user.getResourceType();
        return new OrgAdminDtos.UserSummary(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                department == null ? null : department.getId(),
                department == null ? user.getDepartmentCode() : department.getCode(),
                department == null ? user.getDepartmentCode() : department.getLabel(),
                resourceType == null ? null : resourceType.getId(),
                resourceType == null ? null : resourceType.getCode(),
                resourceType == null ? null : resourceType.getLabel(),
                user.getEmployeeCode(),
                user.getJobTitle(),
                user.isActive()
        );
    }

    private OrgAdminDtos.DepartmentSummary toDepartmentSummary(
            Department department,
            Long organisationId
    ) {
        return new OrgAdminDtos.DepartmentSummary(
                department.getId(),
                department.getCode(),
                department.getLabel(),
                department.getManager() == null ? null : department.getManager().getId(),
                department.getManager() == null ? null : department.getManager().getFullName(),
                userRepository.countOrganisationUsersInDepartment(
                        organisationId,
                        department.getId(),
                        department.getCode()
                ),
                department.getCreatedAt()
        );
    }

    private void applySupplier(
            Supplier supplier,
            OrgAdminDtos.SaveSupplierRequest request,
            String code,
            Long organisationId
    ) {
        supplier.setName(normalize(request.name()));
        supplier.setCode(code);
        // Keep the legacy resource-management fields in sync while the
        // organisation configuration uses the canonical supplier fields.
        supplier.setShortCode(code);
        supplier.setContactPerson(normalize(request.contactPerson()));
        supplier.setContact(normalize(request.contactPerson()));
        supplier.setEmail(normalize(request.email()));
        supplier.setPhone(normalize(request.phone()));
        supplier.setAddress(normalize(request.address()));
        supplier.setNotes(normalize(request.notes()));
        Set<Long> requestedIds = request.resourceTypeIds() == null
                ? Set.of()
                : request.resourceTypeIds().stream().filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        List<ResourceType> resourceTypes = requestedIds.isEmpty()
                ? List.of()
                : resourceTypeRepository.findAllById(requestedIds).stream()
                        .filter(resourceType -> resourceType.getOrganisation() != null
                                && organisationId.equals(resourceType.getOrganisation().getId()))
                        .toList();
        if (resourceTypes.size() != requestedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Every resource type must belong to the supplier organisation.");
        }
        supplier.setResourceTypes(new java.util.LinkedHashSet<>(resourceTypes));
        supplier.setActive(Boolean.TRUE.equals(request.active()));
    }

    private OrgAdminDtos.SupplierSummary toSupplierSummary(Supplier supplier) {
        return new OrgAdminDtos.SupplierSummary(
                supplier.getId(),
                supplier.getName(),
                supplier.getCode() == null ? supplier.getShortCode() : supplier.getCode(),
                supplier.getContactPerson() == null ? supplier.getContact() : supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getNotes(),
                supplier.getResourceTypes().stream()
                        .map(resourceType -> new OrgAdminDtos.SupplierResourceTypeSummary(
                                resourceType.getId(), resourceType.getCode(), resourceType.getLabel()))
                        .toList(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    private String normaliseSessionTimeout(String value) {
        String normalised = value == null ? "4_HOURS" : value.trim().toUpperCase(Locale.ROOT);
        return SESSION_TIMEOUTS.contains(normalised) ? normalised : "4_HOURS";
    }

    private String roleLabel(Role role) {
        return switch (role) {
            case ORG_ADMIN -> "Organisation Admin";
            case PROJECT_MANAGER -> "Project Manager";
            case PROJECT_MANAGER_LEAD -> "Project Manager Lead";
            case MANAGER -> "Manager";
            case DEPARTMENT_MANAGER -> "Department Manager";
            case EMPLOYEE -> "Employee";
            case PLATFORM_OWNER -> "Platform Owner";
            case SALES_MANAGER -> "Sales Manager";
            case CLIENT -> "Client";
        };
    }

    private String roleDescription(Role role) {
        return switch (role) {
            case ORG_ADMIN -> "Manages this organisation's profile, users, departments, and security configuration.";
            case PROJECT_MANAGER -> "Manages the organisation portfolio and operational project data.";
            case PROJECT_MANAGER_LEAD -> "Manages Project Managers and the full organisation project portfolio.";
            case MANAGER -> "Read-only executive visibility of company performance.";
            case DEPARTMENT_MANAGER -> "Manages delivery, resources, and workload for one department.";
            case EMPLOYEE -> "Works with personal assignments and tasks.";
            case PLATFORM_OWNER -> "Manages the SaaS platform.";
            case SALES_MANAGER -> "Manages customer support tickets and client communication.";
            case CLIENT -> "Submits and follows support tickets for their organisation.";
        };
    }

    private String titleCase(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(value);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String searchPattern(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : "%" + normalized.toLowerCase(Locale.ROOT) + "%";
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
