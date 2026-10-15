package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StagingDemoDataSeederTest {

    private static final String VALID_PASSWORD = "Demo-Only-Password-42!";

    private OrganisationRepository organisations;
    private UserRepository users;
    private DepartmentRepository departments;
    private ResourceTypeRepository resourceTypes;
    private ProjectRepository projects;
    private ProjectTaskRepository tasks;
    private RolePermissionRepository permissions;
    private PasswordEncoder passwordEncoder;

    private final Map<String, Organisation> organisationsByCode = new LinkedHashMap<>();
    private final Map<String, AppUser> usersByEmail = new LinkedHashMap<>();
    private final Map<String, Department> departmentsByKey = new LinkedHashMap<>();
    private final Map<String, ResourceType> resourceTypesByKey = new LinkedHashMap<>();
    private final Map<String, Project> projectsByCode = new LinkedHashMap<>();
    private final Map<String, ProjectTask> tasksByKey = new LinkedHashMap<>();
    private final Map<String, RolePermission> permissionsByKey = new LinkedHashMap<>();
    private final AtomicLong projectIds = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        organisations = mock(OrganisationRepository.class);
        users = mock(UserRepository.class);
        departments = mock(DepartmentRepository.class);
        resourceTypes = mock(ResourceTypeRepository.class);
        projects = mock(ProjectRepository.class);
        tasks = mock(ProjectTaskRepository.class);
        permissions = mock(RolePermissionRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        when(organisations.findByCode(any())).thenAnswer(invocation ->
                Optional.ofNullable(organisationsByCode.get(invocation.getArgument(0))));
        when(organisations.findByNameIgnoreCase(any())).thenAnswer(invocation ->
                organisationsByCode.values().stream()
                        .filter(org -> org.getName().equalsIgnoreCase(invocation.getArgument(0)))
                        .findFirst());
        when(organisations.save(any())).thenAnswer(invocation -> {
            Organisation organisation = invocation.getArgument(0);
            if (organisation.getId() == null) organisation.setId(10L + organisationsByCode.size());
            organisationsByCode.put(organisation.getCode(), organisation);
            return organisation;
        });

        when(users.findByEmailIgnoreCase(any())).thenAnswer(invocation ->
                Optional.ofNullable(usersByEmail.get(((String) invocation.getArgument(0)).toLowerCase())));
        when(users.save(any())).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            usersByEmail.put(user.getEmail().toLowerCase(), user);
            return user;
        });
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));

        when(departments.findByCodeAndOrganisation_Id(any(), any())).thenAnswer(invocation ->
                Optional.ofNullable(departmentsByKey.get(key(invocation.getArgument(1), invocation.getArgument(0)))));
        when(departments.save(any())).thenAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            departmentsByKey.put(key(department.getOrganisation().getId(), department.getCode()), department);
            return department;
        });

        when(resourceTypes.findByCodeAndOrganisation_Id(any(), any())).thenAnswer(invocation ->
                Optional.ofNullable(resourceTypesByKey.get(key(invocation.getArgument(1), invocation.getArgument(0)))));
        when(resourceTypes.save(any())).thenAnswer(invocation -> {
            ResourceType type = invocation.getArgument(0);
            resourceTypesByKey.put(key(type.getOrganisation().getId(), type.getCode()), type);
            return type;
        });

        when(projects.findByCode(any())).thenAnswer(invocation ->
                Optional.ofNullable(projectsByCode.get(invocation.getArgument(0))));
        when(projects.save(any())).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            if (project.getId() == null) project.setId(projectIds.incrementAndGet());
            projectsByCode.put(project.getCode(), project);
            return project;
        });

        when(tasks.findByProjectIdAndWbsCode(any(), any())).thenAnswer(invocation ->
                Optional.ofNullable(tasksByKey.get(key(invocation.getArgument(0), invocation.getArgument(1)))));
        when(tasks.save(any())).thenAnswer(invocation -> {
            ProjectTask task = invocation.getArgument(0);
            tasksByKey.put(key(task.getProjectId(), task.getWbsCode()), task);
            return task;
        });

        when(permissions.findByOrganisation_IdAndRoleAndModule(any(), any(), any())).thenAnswer(invocation ->
                Optional.ofNullable(permissionsByKey.get(invocation.getArgument(0) + ":"
                        + invocation.getArgument(1) + ":" + invocation.getArgument(2))));
        when(permissions.save(any())).thenAnswer(invocation -> {
            RolePermission permission = invocation.getArgument(0);
            permissionsByKey.put(permission.getOrganisation().getId() + ":"
                    + permission.getRole() + ":" + permission.getModule(), permission);
            return permission;
        });
    }

    @Test
    void refusesProductionProfileBeforeAnyDatabaseWrite() {
        StagingDemoDataSeeder seeder = seeder(new MockEnvironment().withProperty("spring.profiles.active", "production"),
                VALID_PASSWORD);

        assertThatThrownBy(seeder::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refuses production/prod profiles");
        verify(organisations, never()).save(any());
        verify(users, never()).save(any());
    }

    @Test
    void productionContextDoesNotCreateSeederEvenWhenSeedPropertyIsTrue() {
        new ApplicationContextRunner()
                .withUserConfiguration(StagingDemoDataSeeder.class)
                .withPropertyValues(
                        "spring.profiles.active=production",
                        "app.staging-seed.enabled=true",
                        "app.staging-seed.password=" + VALID_PASSWORD)
                .run(context -> assertThat(context).doesNotHaveBean(StagingDemoDataSeeder.class));
    }

    @Test
    void refusesMissingPasswordBeforeAccountCreation() {
        MockEnvironment staging = new MockEnvironment().withProperty("spring.profiles.active", "staging");

        assertThatThrownBy(seeder(staging, "   ")::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing or empty");
        verify(organisations, never()).save(any());
        verify(users, never()).save(any());
    }

    @Test
    void isIdempotentAndKeepsDemoTenantIsolated() throws Exception {
        Organisation outsideOrganisation = new Organisation();
        outsideOrganisation.setId(99L);
        outsideOrganisation.setCode("OUTSIDE");
        outsideOrganisation.setName("Existing Outside Organisation");
        organisationsByCode.put(outsideOrganisation.getCode(), outsideOrganisation);
        AppUser outsideUser = new AppUser("Outside User", "outside@example.org", "unchanged", Role.EMPLOYEE);
        outsideUser.setOrganisation(outsideOrganisation);
        usersByEmail.put(outsideUser.getEmail(), outsideUser);

        StagingDemoDataSeeder seeder = seeder(
                new MockEnvironment().withProperty("spring.profiles.active", "staging"), VALID_PASSWORD);
        seeder.run();

        AppUser orgAdmin = usersByEmail.get("org.admin@projectum.example");
        orgAdmin.setPasswordHash("manually-changed-hash");
        orgAdmin.setRoles(Set.of(Role.ORG_ADMIN, Role.EMPLOYEE));
        seeder.run();

        Organisation demo = organisationsByCode.get(StagingDemoDataSeeder.DEMO_ORGANISATION_CODE);
        assertThat(demo.getName()).isEqualTo(StagingDemoDataSeeder.DEMO_ORGANISATION_NAME);
        assertThat(organisationsByCode).hasSize(2);
        assertThat(usersByEmail).hasSize(9);
        assertThat(projectsByCode).hasSize(2);
        assertThat(tasksByKey).hasSize(9);

        assertDemoUser("platform.owner", Role.PLATFORM_OWNER, null, false);
        assertDemoUser("org.admin", Role.ORG_ADMIN, demo, false);
        assertDemoUser("project.manager", Role.PROJECT_MANAGER, demo, true);
        assertDemoUser("project.lead", Role.PROJECT_MANAGER_LEAD, demo, true);
        assertDemoUser("department.manager", Role.DEPARTMENT_MANAGER, demo, true);
        assertDemoUser("employee", Role.EMPLOYEE, demo, true);
        assertDemoUser("sales.manager", Role.SALES_MANAGER, demo, false);
        assertDemoUser("client", Role.CLIENT, demo, false);

        assertThat(orgAdmin.getPasswordHash()).isEqualTo("manually-changed-hash");
        assertThat(orgAdmin.getRoles()).containsExactlyInAnyOrder(Role.ORG_ADMIN, Role.EMPLOYEE);
        assertThat(outsideUser.getPasswordHash()).isEqualTo("unchanged");
        assertThat(outsideUser.getOrganisation()).isSameAs(outsideOrganisation);
        assertPermission(demo, Role.ORG_ADMIN, PermissionModule.FINANCE, true);
        assertPermission(demo, Role.PROJECT_MANAGER, PermissionModule.PROJECTS, true);
        assertPermission(demo, Role.DEPARTMENT_MANAGER, PermissionModule.DEPARTMENT_DASHBOARD, true);
        assertPermission(demo, Role.EMPLOYEE, PermissionModule.TASKS, false);
        assertPermission(demo, Role.SALES_MANAGER, PermissionModule.CRM, true);
        assertPermission(demo, Role.CLIENT, PermissionModule.PROJECTS, false);
        verify(users, times(8)).save(any());
        verify(passwordEncoder, times(8)).encode(VALID_PASSWORD);
    }

    private void assertPermission(
            Organisation organisation, Role role, PermissionModule module, boolean canWrite) {
        RolePermission permission = permissionsByKey.get(
                organisation.getId() + ":" + role + ":" + module);
        assertThat(permission).isNotNull();
        assertThat(permission.isCanRead()).isTrue();
        assertThat(permission.isCanWrite()).isEqualTo(canWrite);
    }

    private void assertDemoUser(String localPart, Role role, Organisation organisation, boolean resourceProfile) {
        AppUser user = usersByEmail.get(localPart + "@projectum.example");
        assertThat(user).isNotNull();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).contains(role);
        assertThat(user.getOrganisation()).isSameAs(organisation);
        assertThat(user.getEmailNotificationsEnabled()).isFalse();
        if (resourceProfile) {
            assertThat(user.getDepartment()).isNotNull();
            assertThat(user.getResourceType()).isNotNull();
        }
    }

    private StagingDemoDataSeeder seeder(MockEnvironment environment, String password) {
        return new StagingDemoDataSeeder(environment, password, organisations, users, departments,
                resourceTypes, projects, tasks, permissions, passwordEncoder);
    }

    private static String key(Object first, Object second) {
        return first + ":" + second;
    }
}
