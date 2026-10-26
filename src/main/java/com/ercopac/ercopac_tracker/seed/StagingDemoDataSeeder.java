package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Creates fictional, tenant-scoped data only for an explicitly enabled staging boot. */
@Component
@Profile("staging & !production & !prod")
@ConditionalOnProperty(name = "app.staging-seed.enabled", havingValue = "true")
public class StagingDemoDataSeeder implements CommandLineRunner {

    static final String DEMO_ORGANISATION_CODE = "STAGING_DEMO";
    static final String DEMO_ORGANISATION_NAME = "Projectum Staging Demo";
    static final String EMAIL_DOMAIN = "projectum.example";

    private static final Logger log = LoggerFactory.getLogger(StagingDemoDataSeeder.class);
    private static final Set<String> FORBIDDEN_PROFILES = Set.of("production", "prod");
    private static final List<Role> PROJECT_MANAGER_ROLES =
            List.of(Role.PROJECT_MANAGER, Role.PROJECT_MANAGER_LEAD);
    private static final List<Role> CRM_ROLES =
            List.of(Role.SALES_MANAGER_LEAD, Role.SALES_MANAGER, Role.SYSTEM_ENGINEER);

    private final Environment environment;
    private final String demoPassword;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final Set<String> departmentsCreatedThisRun = new HashSet<>();

    public StagingDemoDataSeeder(
            Environment environment,
            @Value("${app.staging-seed.password}") String demoPassword,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            ResourceTypeRepository resourceTypeRepository,
            ProjectRepository projectRepository,
            ProjectTaskRepository projectTaskRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.environment = environment;
        this.demoPassword = demoPassword;
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        requireSafeStagingProfile();
        validatePassword(demoPassword);
        departmentsCreatedThisRun.clear();

        Organisation organisation = getOrCreateDemoOrganisation();
        Department engineering = getOrCreateDepartment(organisation, "ENG", "Engineering");
        Department delivery = getOrCreateDepartment(organisation, "DEL", "Project Delivery");
        Department sales = getOrCreateDepartment(organisation, "SAL", "Sales and Customer Success");
        ResourceType engineerType = getOrCreateResourceType(
                organisation, engineering, "SOFTWARE_ENGINEER", "Software Engineer", "85.00");
        ResourceType managerType = getOrCreateResourceType(
                organisation, delivery, "PROJECT_MANAGER", "Project Manager", "120.00");

        getOrCreateUser("platform.owner", "Staging Platform Owner",
                Role.PLATFORM_OWNER, null, null, null, false);
        getOrCreateUser("org.admin", "Staging Organisation Admin",
                Role.ORG_ADMIN, organisation, null, null, false);
        AppUser projectManager = getOrCreateUser("project.manager", "Morgan Project Manager",
                Role.PROJECT_MANAGER, organisation, delivery, managerType, true);
        AppUser projectLead = getOrCreateUser("project.lead", "Alex Project Lead",
                Role.PROJECT_MANAGER_LEAD, organisation, delivery, managerType, true);
        AppUser departmentManager = getOrCreateUser("department.manager", "Jordan Engineering Manager",
                Role.DEPARTMENT_MANAGER, organisation, engineering, engineerType, true);
        AppUser employee = getOrCreateUser("employee", "Taylor Demo Engineer",
                Role.EMPLOYEE, organisation, engineering, engineerType, true);
        AppUser salesManager = getOrCreateUser("sales.manager", "Casey Demo Sales Manager",
                Role.SALES_MANAGER, organisation, sales, null, false);
        AppUser client = getOrCreateUser("client", "Riley Example Client",
                Role.CLIENT, organisation, null, null, false);

        setManagerOnlyWhenUnassigned(engineering, departmentManager);
        setManagerOnlyWhenUnassigned(delivery, projectLead);
        seedPermissions(organisation);

        Project rollout = getOrCreateProject(organisation, "STG-DEMO-001",
                "Fictional Customer Portal Rollout", projectManager, projectLead,
                salesManager, "Example Industries", 38, "640000", "MEDIUM");
        Project optimisation = getOrCreateProject(organisation, "STG-DEMO-002",
                "Fictional Operations Optimisation", projectLead, projectLead,
                salesManager, "Contoso Example Services", 12, "280000", "LOW");
        seedRolloutTasks(rollout, organisation, engineering, engineerType,
                projectManager, departmentManager, employee, client);
        seedOptimisationTasks(optimisation, organisation, delivery, managerType, projectLead, employee);

        log.info("Staging demo data is ready for organisation code {} (accounts={}, projects={}).",
                DEMO_ORGANISATION_CODE, 8, 2);
    }

    private void requireSafeStagingProfile() {
        Set<String> activeProfiles = Arrays.stream(environment.getActiveProfiles())
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (!activeProfiles.contains("staging") || activeProfiles.stream().anyMatch(FORBIDDEN_PROFILES::contains)) {
            throw new IllegalStateException(
                    "Staging demo seeding requires the staging profile and refuses production/prod profiles.");
        }
    }

    static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("The staging demo password secret is missing or empty.");
        }
        if (password.length() < 20 || password.length() > 128
                || password.chars().anyMatch(Character::isWhitespace)
                || password.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException(
                    "The staging demo password secret must be 20-128 characters without whitespace or control characters.");
        }
    }

    private Organisation getOrCreateDemoOrganisation() {
        return organisationRepository.findByCode(DEMO_ORGANISATION_CODE)
                .map(existing -> {
                    if (!DEMO_ORGANISATION_NAME.equals(existing.getName())) {
                        throw new IllegalStateException("Organisation code " + DEMO_ORGANISATION_CODE
                                + " belongs to a different organisation; refusing to modify it.");
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    organisationRepository.findByNameIgnoreCase(DEMO_ORGANISATION_NAME).ifPresent(existing -> {
                        throw new IllegalStateException("The staging demo organisation name uses another code.");
                    });
                    Organisation organisation = new Organisation();
                    organisation.setName(DEMO_ORGANISATION_NAME);
                    organisation.setCode(DEMO_ORGANISATION_CODE);
                    organisation.setCountry("Germany");
                    organisation.setDomain("projectum.example");
                    organisation.setStatus(OrganisationStatus.ACTIVE);
                    organisation.setActive(true);
                    organisation.setPlan("STAGING_DEMO");
                    organisation.setUserLimit(20);
                    organisation.setOrgAdminLicenceLimit(2);
                    organisation.setProjectManagerLicenceLimit(4);
                    organisation.setDepartmentManagerLicenceLimit(3);
                    organisation.setEmployeeLicenceLimit(10);
                    organisation.setSalesManagerLicenceLimit(3);
                    organisation.setClientLicenceLimit(5);
                    organisation.setBillingEmail("billing@projectum.example");
                    organisation.setInternalNotes("Fictional staging-only tenant. Never use customer data.");
                    return organisationRepository.save(organisation);
                });
    }

    private Department getOrCreateDepartment(Organisation organisation, String code, String label) {
        return departmentRepository.findByCodeAndOrganisation_Id(code, organisation.getId())
                .orElseGet(() -> {
                    departmentsCreatedThisRun.add(code);
                    return departmentRepository.save(new Department(code, label, organisation));
                });
    }

    private ResourceType getOrCreateResourceType(
            Organisation organisation, Department department, String code, String label, String rate) {
        return resourceTypeRepository.findByCodeAndOrganisation_Id(code, organisation.getId())
                .orElseGet(() -> {
                    ResourceType type = new ResourceType(code, label, organisation);
                    type.setDepartment(department);
                    type.setDefaultRate(new BigDecimal(rate));
                    type.setColour("PROJECT_MANAGER".equals(code) ? "#7C3AED" : "#2563EB");
                    return resourceTypeRepository.save(type);
                });
    }

    private AppUser getOrCreateUser(
            String localPart, String fullName, Role role, Organisation organisation,
            Department department, ResourceType resourceType, boolean internalUser) {
        String email = localPart + "@" + EMAIL_DOMAIN;
        return userRepository.findByEmailIgnoreCase(email)
                .map(existing -> validateExistingDemoUser(existing, role, organisation, email))
                .orElseGet(() -> {
                    if (organisation != null) enforceLicenceCapacity(organisation, role);
                    AppUser user = new AppUser();
                    user.setEmail(email);
                    user.setFullName(fullName);
                    user.setPasswordHash(passwordEncoder.encode(demoPassword));
                    user.setRoles(Set.of(role));
                    user.setActive(true);
                    user.setOrganisation(organisation);
                    user.setDepartment(department);
                    user.setResourceType(resourceType);
                    user.setInternalUser(internalUser);
                    user.setEmployeeCode(organisation == null ? null : "DEMO-" + role.name());
                    user.setJobTitle(fullName);
                    user.setSeniority(role.requiresResourceProfile() ? "SENIOR" : null);
                    user.setDefaultRate(resourceType == null ? null : resourceType.getDefaultRate());
                    user.setRateType(resourceType == null ? null : "HOURLY");
                    user.setCurrency(resourceType == null ? null : "EUR");
                    user.setEmailNotificationsEnabled(false);
                    user.setNotes("Fictional staging-only account; external email delivery is disabled.");
                    return userRepository.save(user);
                });
    }

    private AppUser validateExistingDemoUser(
            AppUser existing, Role expectedRole, Organisation expectedOrganisation, String email) {
        Long actualOrganisationId = existing.getOrganisation() == null ? null : existing.getOrganisation().getId();
        Long expectedOrganisationId = expectedOrganisation == null ? null : expectedOrganisation.getId();
        if (!Objects.equals(actualOrganisationId, expectedOrganisationId)) {
            throw new IllegalStateException("Demo email " + email
                    + " belongs to another organisation; refusing to modify it.");
        }
        if (!existing.isActive() || !existing.hasRole(expectedRole)) {
            throw new IllegalStateException("Existing demo account " + email
                    + " lacks its required active role; refusing to reset it.");
        }
        if (expectedRole.requiresResourceProfile()
                && (existing.getDepartment() == null || existing.getResourceType() == null)) {
            throw new IllegalStateException("Existing demo account " + email
                    + " lacks its required department/resource profile; refusing to reset it.");
        }
        return existing;
    }

    private void enforceLicenceCapacity(Organisation organisation, Role role) {
        if (userRepository.countByOrganisation_IdAndActiveTrue(organisation.getId()) >= organisation.getUserLimit()) {
            throw new IllegalStateException("The staging demo organisation user limit has been reached.");
        }
        int limit = switch (role) {
            case ORG_ADMIN -> organisation.getOrgAdminLicenceLimit();
            case PROJECT_MANAGER, PROJECT_MANAGER_LEAD -> organisation.getProjectManagerLicenceLimit();
            case DEPARTMENT_MANAGER -> organisation.getDepartmentManagerLicenceLimit();
            case EMPLOYEE -> organisation.getEmployeeLicenceLimit();
            case SALES_MANAGER_LEAD, SALES_MANAGER, SYSTEM_ENGINEER -> organisation.getSalesManagerLicenceLimit();
            case CLIENT -> organisation.getClientLicenceLimit();
            case MANAGER, H24, H24_LEAD -> Integer.MAX_VALUE;
            case PLATFORM_OWNER -> throw new IllegalArgumentException("Platform owners are not organisation members.");
        };
        long used = role.isProjectManagerRole()
                ? userRepository.countByOrganisation_IdAndRoleInAndActiveTrue(
                        organisation.getId(), PROJECT_MANAGER_ROLES)
                : role.isCrmRole()
                ? userRepository.countByOrganisation_IdAndRoleInAndActiveTrue(organisation.getId(), CRM_ROLES)
                : userRepository.countByOrganisation_IdAndRoleAndActiveTrue(organisation.getId(), role);
        if (used >= limit) {
            throw new IllegalStateException("No staging demo licence remains for role " + role.name() + ".");
        }
    }

    private void setManagerOnlyWhenUnassigned(Department department, AppUser manager) {
        if (departmentsCreatedThisRun.contains(department.getCode()) && department.getManager() == null) {
            department.setManager(manager);
            departmentRepository.save(department);
        }
    }

    private void seedPermissions(Organisation organisation) {
        seedPermissions(organisation, Role.ORG_ADMIN, true, EnumSet.of(
                PermissionModule.GM_DASHBOARD, PermissionModule.CRM, PermissionModule.PROJECTS,
                PermissionModule.PLANNING, PermissionModule.TASKS, PermissionModule.FINANCE,
                PermissionModule.FORECAST, PermissionModule.RISKS, PermissionModule.CHANGE_REQUESTS,
                PermissionModule.ACTIONS, PermissionModule.RESOURCES, PermissionModule.SUPPLIERS,
                PermissionModule.DEPARTMENT_DASHBOARD, PermissionModule.EMPLOYEE_DASHBOARD));
        seedPermissions(organisation, Role.PROJECT_MANAGER, true, EnumSet.of(
                PermissionModule.GM_DASHBOARD, PermissionModule.PROJECTS, PermissionModule.PLANNING,
                PermissionModule.TASKS, PermissionModule.FINANCE, PermissionModule.FORECAST,
                PermissionModule.RISKS, PermissionModule.CHANGE_REQUESTS, PermissionModule.ACTIONS,
                PermissionModule.RESOURCES, PermissionModule.SUPPLIERS));
        seedPermissions(organisation, Role.DEPARTMENT_MANAGER, true, EnumSet.of(
                PermissionModule.DEPARTMENT_DASHBOARD, PermissionModule.PROJECTS,
                PermissionModule.PLANNING, PermissionModule.TASKS, PermissionModule.RESOURCES));
        seedPermissions(organisation, Role.EMPLOYEE, false, EnumSet.of(
                PermissionModule.EMPLOYEE_DASHBOARD, PermissionModule.PROJECTS,
                PermissionModule.PLANNING, PermissionModule.TASKS));
        seedPermissions(organisation, Role.SALES_MANAGER, true,
                EnumSet.of(PermissionModule.CRM, PermissionModule.PROJECTS));
        seedPermissions(organisation, Role.CLIENT, false,
                EnumSet.of(PermissionModule.PROJECTS, PermissionModule.PLANNING, PermissionModule.TASKS));
    }

    private void seedPermissions(
            Organisation organisation, Role role, boolean canWrite, Set<PermissionModule> modules) {
        for (PermissionModule module : modules) {
            if (rolePermissionRepository
                    .findByOrganisation_IdAndRoleAndModule(organisation.getId(), role, module)
                    .isPresent()) continue;
            RolePermission permission = new RolePermission();
            permission.setOrganisation(organisation);
            permission.setRole(role);
            permission.setModule(module);
            permission.setCanRead(true);
            permission.setCanWrite(canWrite);
            rolePermissionRepository.save(permission);
        }
    }

    private Project getOrCreateProject(
            Organisation organisation, String code, String name, AppUser projectManager,
            AppUser programmeManager, AppUser salesManager, String customer,
            int progress, String budget, String risk) {
        return projectRepository.findByCode(code)
                .map(existing -> {
                    if (!organisation.getId().equals(existing.getOrganisation().getId())) {
                        throw new IllegalStateException("Demo project code " + code
                                + " belongs to another organisation; refusing to modify it.");
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Project project = new Project();
                    project.setCode(code);
                    project.setName(name);
                    project.setShortName(code.substring(code.length() - 3));
                    project.setOrganisation(organisation);
                    project.setPortfolio("Staging Demonstrations");
                    project.setOrgAssignment("Digital Delivery");
                    project.setCountry("Germany");
                    project.setProjectType("Customer Delivery");
                    project.setProjectPhase("EXECUTION");
                    project.setPriority("HIGH");
                    project.setRiskLevel(risk);
                    project.setCustomer(customer);
                    project.setPlannedStart(LocalDate.now().minusDays(21));
                    project.setPlannedEnd(LocalDate.now().plusMonths(4));
                    project.setProgress(progress);
                    project.setProjectBudget(new BigDecimal(budget));
                    project.setTotalProjectBudget(new BigDecimal(budget));
                    project.setEstimatedCost(new BigDecimal(budget).multiply(new BigDecimal("0.72")));
                    project.setProjectManagerId(projectManager.getId());
                    project.setProjectManagerName(projectManager.getFullName());
                    project.setProgramManagerName(programmeManager.getFullName());
                    project.setSalesManagerName(salesManager.getFullName());
                    project.setComment("Fictional staging-only project data.");
                    return projectRepository.save(project);
                });
    }

    private void seedRolloutTasks(
            Project project, Organisation organisation, Department department, ResourceType type,
            AppUser manager, AppUser departmentManager, AppUser employee, AppUser client) {
        ProjectTask discovery = getOrCreateTask(project, organisation, "1", "Discovery and planning",
                null, 0, "SUMMARY", 100, null, department, type, 1);
        getOrCreateTask(project, organisation, "1.1", "Stakeholder workshop",
                discovery, 1, "TASK", 100, client, department, type, 2);
        ProjectTask implementation = getOrCreateTask(project, organisation, "2", "Portal implementation",
                null, 0, "SUMMARY", 45, null, department, type, 3);
        getOrCreateTask(project, organisation, "2.1", "Integration design",
                implementation, 1, "TASK", 70, departmentManager, department, type, 4);
        getOrCreateTask(project, organisation, "2.2", "Implement tenant dashboard",
                implementation, 1, "TASK", 35, employee, department, type, 5);
        getOrCreateTask(project, organisation, "3", "Acceptance and handover",
                null, 0, "SUMMARY", 0, manager, department, type, 6);
    }

    private void seedOptimisationTasks(
            Project project, Organisation organisation, Department department, ResourceType type,
            AppUser projectLead, AppUser employee) {
        ProjectTask assessment = getOrCreateTask(project, organisation, "1", "Current-state assessment",
                null, 0, "SUMMARY", 30, projectLead, department, type, 1);
        getOrCreateTask(project, organisation, "1.1", "Process metrics review",
                assessment, 1, "TASK", 25, employee, department, type, 2);
        getOrCreateTask(project, organisation, "2", "Improvement roadmap",
                null, 0, "SUMMARY", 0, projectLead, department, type, 3);
    }

    private ProjectTask getOrCreateTask(
            Project project, Organisation organisation, String wbs, String name, ProjectTask parent,
            int outlineLevel, String taskType, int completion, AppUser assignee,
            Department department, ResourceType resourceType, int displayOrder) {
        return projectTaskRepository.findByProjectIdAndWbsCode(project.getId(), wbs)
                .orElseGet(() -> {
                    LocalDate start = LocalDate.now().plusDays((displayOrder - 2L) * 7L);
                    LocalDate end = start.plusDays("SUMMARY".equals(taskType) ? 20 : 8);
                    ProjectTask task = new ProjectTask();
                    task.setProjectId(project.getId());
                    task.setOrganisationId(organisation.getId());
                    task.setWbsCode(wbs);
                    task.setName(name);
                    task.setDescription("Fictional staging demonstration task.");
                    task.setParentId(parent == null ? null : parent.getId());
                    task.setOutlineLevel(outlineLevel);
                    task.setTaskType(taskType);
                    task.setDisplayOrder(displayOrder);
                    task.setPlannedStart(start);
                    task.setPlannedEnd(end);
                    task.setBaselineStart(start);
                    task.setBaselineEnd(end);
                    task.setDurationDays((int) (end.toEpochDay() - start.toEpochDay()) + 1);
                    task.setPercentComplete(completion);
                    task.setStatus(completion == 100 ? "COMPLETED" : completion > 0 ? "IN_PROGRESS" : "NOT_STARTED");
                    task.setPlannedHours(new BigDecimal("64.00"));
                    task.setAllocationPercent(100);
                    task.setActive(true);
                    task.setAssignedUser(assignee);
                    task.setDepartment(department);
                    task.setResourceType(resourceType);
                    return projectTaskRepository.save(task);
                });
    }
}
