package com.ercopac.ercopac_tracker.security;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The single rule that limits what a Department Manager (Department Chief) may see and change.
 *
 * <p>A Department Manager is allowed the <b>resource types of their department</b>: every active
 * {@link ResourceType} whose {@code department} is the manager's department, plus the manager's own
 * resource type. When no resource type is linked to the department yet, the historical department
 * code map ({@link #LEGACY_DEPARTMENT_RESOURCE_TYPES}) is used instead, so existing data keeps working.
 *
 * <p>Suppliers are in scope when at least one of their resource types is allowed. Resources (users)
 * are in scope when their resource type is allowed or they belong to the manager's department.
 *
 * <p>To restrict a manager to their single own resource type instead, change only
 * {@link #resolveAllowedCodes}: every service (resources, suppliers, task assignment, My Department)
 * reads the scope from here.
 *
 * <p>Users who also hold a broader role (organisation admin, platform owner, project manager, project
 * manager lead or manager) are not restricted.
 */
@Component
public class DepartmentScope {

    /** Department code → resource type codes, used when no resource type is linked to the department. */
    static final Map<String, List<String>> LEGACY_DEPARTMENT_RESOURCE_TYPES = Map.ofEntries(
            Map.entry("SW", List.of("PC", "PLC")),
            Map.entry("MFC", List.of("MFC.M", "MFC.E")),
            Map.entry("INST", List.of("MEC", "ELECT", "INST")),
            Map.entry("PM", List.of("PM")),
            Map.entry("ME", List.of("ME")),
            Map.entry("CE", List.of("CE")),
            Map.entry("PRC", List.of("PRC")),
            Map.entry("QA", List.of("QA")),
            Map.entry("HSE", List.of("HSE")),
            Map.entry("FIN", List.of("FIN")),
            Map.entry("CS", List.of("CS"))
    );

    /** Roles that see the whole organisation even when the user is also a Department Manager. */
    private static final Set<Role> UNRESTRICTED_ROLES = Set.of(
            Role.PLATFORM_OWNER,
            Role.ORG_ADMIN,
            Role.PROJECT_MANAGER,
            Role.PROJECT_MANAGER_LEAD,
            Role.MANAGER
    );

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public DepartmentScope(SecurityUtils securityUtils,
                           UserRepository userRepository,
                           ResourceTypeRepository resourceTypeRepository) {
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    /**
     * The scope of the authenticated user. Unrestricted unless the user is a Department Manager
     * without any broader role.
     */
    public Scope current() {
        if (!securityUtils.hasAnyRole(Role.DEPARTMENT_MANAGER.name())) {
            return Scope.unrestricted();
        }
        String[] broader = UNRESTRICTED_ROLES.stream().map(Role::name).toArray(String[]::new);
        if (securityUtils.hasAnyRole(broader)) {
            return Scope.unrestricted();
        }
        Long userId = securityUtils.getCurrentUserId();
        Long organisationId = securityUtils.getCurrentOrganisationId();
        AppUser manager = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Authenticated user is outside the current organisation."));
        return forManager(manager, organisationId);
    }

    /** The restricted scope of a given Department Manager (used when another user views their department). */
    public Scope forManager(AppUser manager, Long organisationId) {
        return forDepartment(organisationId, manager.getDepartmentCode(), manager.getResourceType());
    }

    /** The restricted scope of a department, plus an optional extra resource type (the manager's own). */
    public Scope forDepartment(Long organisationId, String departmentCode, ResourceType ownResourceType) {
        String department = clean(departmentCode);
        String ownCode = ownResourceType == null ? null : ownResourceType.getCode();
        return new Scope(true, department, resolveAllowedCodes(organisationId, department, ownCode));
    }

    /** Throws 403 unless the current user may access the supplier. */
    public void assertSupplierAccess(Supplier supplier) {
        if (!current().allowsSupplier(supplier)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This supplier is outside the resource types of your department.");
        }
    }

    Set<String> resolveAllowedCodes(Long organisationId, String departmentCode, String ownResourceTypeCode) {
        Set<String> codes = new TreeSet<>();
        if (departmentCode != null && organisationId != null) {
            resourceTypeRepository.findActiveByOrganisationAndDepartmentCode(organisationId, departmentCode)
                    .stream()
                    .map(ResourceType::getCode)
                    .map(DepartmentScope::code)
                    .filter(Objects::nonNull)
                    .forEach(codes::add);
            if (codes.isEmpty()) {
                codes.addAll(LEGACY_DEPARTMENT_RESOURCE_TYPES.getOrDefault(
                        departmentCode.toUpperCase(Locale.ROOT), List.of(departmentCode.toUpperCase(Locale.ROOT))));
            }
        }
        String own = code(ownResourceTypeCode);
        if (own != null) {
            codes.add(own);
        }
        return Set.copyOf(codes);
    }

    /** Resource type codes of the historical department code map (empty when the department is unknown). */
    public static List<String> legacyResourceTypes(String departmentCode) {
        String department = code(departmentCode);
        return department == null ? List.of() : LEGACY_DEPARTMENT_RESOURCE_TYPES.getOrDefault(department, List.of());
    }

    static String code(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toUpperCase(Locale.ROOT);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * What a user may access. {@code restricted == false} means everything in the organisation.
     * Resource type codes are uppercase.
     */
    public record Scope(boolean restricted, String departmentCode, Set<String> resourceTypeCodes) {

        public Scope {
            resourceTypeCodes = resourceTypeCodes == null ? Set.of() : Set.copyOf(resourceTypeCodes);
        }

        public static Scope unrestricted() {
            return new Scope(false, null, Set.of());
        }

        /** Allowed codes in alphabetical order (stable output for lists and generic rows). */
        public List<String> sortedResourceTypeCodes() {
            return new TreeSet<>(resourceTypeCodes).stream().toList();
        }

        public boolean allowsResourceType(String resourceTypeCode) {
            if (!restricted) {
                return true;
            }
            String normalized = code(resourceTypeCode);
            return normalized != null && resourceTypeCodes.contains(normalized);
        }

        public boolean allowsAllResourceTypes(Collection<String> codes) {
            return codes.stream().allMatch(this::allowsResourceType);
        }

        public boolean allowsSupplier(Supplier supplier) {
            if (!restricted) {
                return true;
            }
            return supplier != null && supplier.getResourceTypes() != null
                    && supplier.getResourceTypes().stream().map(ResourceType::getCode).anyMatch(this::allowsResourceType);
        }

        public boolean allowsResource(AppUser user) {
            if (!restricted) {
                return true;
            }
            if (user == null) {
                return false;
            }
            if (user.getResourceType() != null && allowsResourceType(user.getResourceType().getCode())) {
                return true;
            }
            return departmentCode != null && departmentCode.equalsIgnoreCase(user.getDepartmentCode());
        }

        /** The supplier's resource types that are in scope (all of them when unrestricted). */
        public List<String> visibleResourceTypes(Supplier supplier) {
            return supplier.getResourceTypes().stream()
                    .map(ResourceType::getCode)
                    .filter(this::allowsResourceType)
                    .sorted()
                    .toList();
        }
    }
}
