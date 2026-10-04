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
import com.ercopac.ercopac_tracker.ticketing.domain.EquipmentType;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriority;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriorityConfig;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatusConfig;
import com.ercopac.ercopac_tracker.ticketing.repository.EquipmentTypeRepository; // 👈 AJOUT
import com.ercopac.ercopac_tracker.ticketing.repository.TicketPriorityConfigRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketStatusConfigRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionRepository rolePermissionRepository;
    
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    
    private final TicketStatusConfigRepository statusConfigRepo;
    private final TicketPriorityConfigRepository priorityConfigRepo;
    
    // 👈 AJOUT : Injection du repository pour l'équipement
    private final EquipmentTypeRepository equipmentTypeRepository;

    @Override
    public void run(String... args) {
        if (organisationRepository.count() > 0) {
            return; // Prevent reseeding if data already exists
        }

        // =========================================================
        // 1. ORGANISATION
        // =========================================================
        Organisation org = new Organisation();
        org.setName("PharmaStore GmbH");
        org.setCode("PHARMA");
        org.setActive(true);
        org.setStatus(OrganisationStatus.ACTIVE);
        org.setCountry("Germany");
        org.setDomain("pharmastore.com");
        org.setPlan("ENTERPRISE");
        org.setOrgAdminLicenceLimit(5);
        org.setProjectManagerLicenceLimit(10);
        org.setDepartmentManagerLicenceLimit(20);
        org.setEmployeeLicenceLimit(100);
        org.setSalesManagerLicenceLimit(10);
        org.setClientLicenceLimit(50);
        organisationRepository.save(org);

        // =========================================================
        // 2. DEPARTMENTS
        // =========================================================
        Department deptDesign = new Department("DES", "Design", org);
        departmentRepository.save(deptDesign);

        Department deptEng = new Department("ENG", "Engineering", org);
        departmentRepository.save(deptEng);
        ResourceType projectManagerType = resourceType(org, "PROJECT_MANAGER", "Project Manager");
        ResourceType engineeringType = resourceType(org, "SOFTWARE_ENGINEER", "Software Engineer");

        // =========================================================
        // 3. USERS
        // =========================================================
        AppUser platformOwner = seedUser("hamza@projectum.com", "Hamza Tounsi", "Hamza123!", Role.PLATFORM_OWNER, null, "Executive", null, null);
        AppUser orgAdmin = seedUser("admin@pharmastore.com", "Organisation Admin", "Hamza123!", Role.ORG_ADMIN, org, "Administration", null, null);
        
        AppUser gm = seedUser("gm@pharmastore.com", "Michael Weber", "Hamza123!", Role.PROJECT_MANAGER, org, "Management", deptDesign, projectManagerType);
        AppUser dm = seedUser("dm@pharmastore.com", "Sarah Engineering", "Hamza123!", Role.DEPARTMENT_MANAGER, org, "Engineering", deptEng, engineeringType);
        
        AppUser employee1 = seedUser("employee3@pharmastore.com", "John Developer", "Hamza123!", Role.EMPLOYEE, org, "Engineering", deptEng, engineeringType);
        AppUser employee2 = seedUser("employee2@pharmastore.com", "Emma Planner", "Hamza123!", Role.EMPLOYEE, org, "Design", deptDesign, projectManagerType);

        seedGeneralManagerPermissions(org);

        // =========================================================
        // 4. PROJECT 1
        // =========================================================
        Project project = new Project();
        project.setName("SAP Migration Program");
        project.setCode("SAP-2026");
        project.setShortName("SAP");
        project.setCountry("Germany");
        project.setCustomer("Bayer AG");
        project.setCategory("Digital Transformation");
        project.setProjectType("Enterprise");
        project.setProjectPhase("Execution");
        project.setPriority("HIGH");
        project.setRiskLevel("MEDIUM");
        project.setProjectManagerId(gm.getId());
        project.setProjectManagerName(gm.getFullName());
        project.setPlannedStart(LocalDate.now().minusDays(15));
        project.setPlannedEnd(LocalDate.now().plusMonths(6));
        project.setProgress(42);
        project.setProjectBudget(new BigDecimal("850000"));
        project.setTotalProjectBudget(new BigDecimal("1200000"));
        project.setOrganisation(org);
        projectRepository.save(project);

        // =========================================================
        // 5. TASKS
        // =========================================================
        ProjectTask summary1 = createTask(project, org, "1", "Project Initiation", 0, null, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), 100, "COMPLETED", null);
        ProjectTask summary2 = createTask(project, org, "2", "System Design", 0, null, LocalDate.now(), LocalDate.now().plusDays(30), 65, "IN_PROGRESS", null);
        ProjectTask task21 = createTask(project, org, "2.1", "Architecture Definition", 1, summary2, LocalDate.now(), LocalDate.now().plusDays(7), 100, "COMPLETED", dm);
        ProjectTask task22 = createTask(project, org, "2.2", "Database Modelling", 1, summary2, LocalDate.now().plusDays(2), LocalDate.now().plusDays(14), 75, "IN_PROGRESS", employee1);
        ProjectTask task23 = createTask(project, org, "2.3", "API Integration", 1, summary2, LocalDate.now().plusDays(10), LocalDate.now().plusDays(25), 30, "IN_PROGRESS", employee2);
        ProjectTask summary3 = createTask(project, org, "3", "Testing & Deployment", 0, null, LocalDate.now().plusDays(25), LocalDate.now().plusDays(60), 0, "NOT_STARTED", null);
        createTask(project, org, "3.1", "QA Validation", 1, summary3, LocalDate.now().plusDays(30), LocalDate.now().plusDays(40), 0, "NOT_STARTED", employee1);
        createTask(project, org, "3.2", "Production Deployment", 1, summary3, LocalDate.now().plusDays(45), LocalDate.now().plusDays(55), 0, "NOT_STARTED", gm);

        // =========================================================
        // 6. TICKET CONFIGS & EQUIPMENT
        // =========================================================
        seedTicketConfigs();
        seedEquipmentCatalogue(); // 👈 AJOUT : Appel de la méthode

        System.out.println("=======================================");
        System.out.println("✅ PROJECTUM DEMO DATA SEEDED SUCCESSFULLY");
        System.out.println("✅ Departments 'Design' and 'Engineering' created.");
        System.out.println("✅ Ticket Statuses, Priorities and Equipment seeded.");
        System.out.println("=======================================");
    }

    private AppUser seedUser(String email, String fullName, String rawPassword, Role role, Organisation organisation, String departmentCode, Department department, ResourceType resourceType) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(java.util.Set.of(role));
        user.setActive(true);
        user.setInternalUser(role.requiresResourceProfile());
        user.setDepartmentCode(departmentCode);
        user.setJobTitle(role.name());
        user.setDefaultRate(new BigDecimal("120"));
        user.setHoursPerDay(8);
        user.setOrganisation(organisation);
        
        user.setDepartment(department);
        user.setResourceType(resourceType);
        return userRepository.save(user);
    }

    private ResourceType resourceType(Organisation organisation, String code, String label) {
        return resourceTypeRepository.findByCodeAndOrganisation_Id(code, organisation.getId())
                .orElseGet(() -> resourceTypeRepository.save(new ResourceType(code, label, organisation)));
    }

    private void seedGeneralManagerPermissions(Organisation organisation) {
        List<PermissionModule> modules = List.of(
                PermissionModule.GM_DASHBOARD,
                PermissionModule.CRM,
                PermissionModule.PROJECTS,
                PermissionModule.PLANNING,
                PermissionModule.TASKS,
                PermissionModule.FINANCE,
                PermissionModule.FORECAST,
                PermissionModule.RISKS,
                PermissionModule.CHANGE_REQUESTS,
                PermissionModule.ACTIONS,
                PermissionModule.RESOURCES,
                PermissionModule.SUPPLIERS
        );

        for (PermissionModule module : modules) {
            if (rolePermissionRepository
                    .findByOrganisation_IdAndRoleAndModule(organisation.getId(), Role.PROJECT_MANAGER, module)
                    .isPresent()) {
                continue;
            }

            RolePermission permission = new RolePermission();
            permission.setOrganisation(organisation);
            permission.setRole(Role.PROJECT_MANAGER);
            permission.setModule(module);
            permission.setCanRead(true);
            permission.setCanWrite(true);
            rolePermissionRepository.save(permission);
        }
    }

    private ProjectTask createTask(Project project, Organisation organisation, String wbs, String name, int outlineLevel, ProjectTask parent, LocalDate start, LocalDate end, int progress, String status, AppUser assignedUser) {
        ProjectTask task = new ProjectTask();
        task.setProjectId(project.getId());
        task.setOrganisationId(organisation.getId());
        task.setWbsCode(wbs);
        task.setName(name);
        task.setOutlineLevel(outlineLevel);
        task.setParentId(parent != null ? parent.getId() : null);
        task.setPlannedStart(start);
        task.setPlannedEnd(end);
        task.setBaselineStart(start);
        task.setBaselineEnd(end);
        task.setDurationDays((int) (end.toEpochDay() - start.toEpochDay()) + 1);
        task.setPercentComplete(progress);
        task.setStatus(status);
        task.setActive(true);
        if (assignedUser != null) {
            task.setAssignedUser(assignedUser);
        }
        return projectTaskRepository.save(task);
    }

    private void seedTicketConfigs() {
        TicketStatusConfig open = new TicketStatusConfig();
        open.setStatus(TicketStatus.OPEN);
        open.setLabel("Open");
        open.setDescription("First response SLA starts");
        open.setDisplayOrder(1);
        open.setSlaTrigger("First response SLA starts");
        statusConfigRepo.save(open);

        TicketStatusConfig inProgress = new TicketStatusConfig();
        inProgress.setStatus(TicketStatus.IN_PROGRESS);
        inProgress.setLabel("In progress");
        inProgress.setDescription("Agent working on fix");
        inProgress.setDisplayOrder(2);
        statusConfigRepo.save(inProgress);

        TicketStatusConfig escalated = new TicketStatusConfig();
        escalated.setStatus(TicketStatus.ESCALATED);
        escalated.setLabel("Escalated L2");
        escalated.setDescription("L2 team handling");
        escalated.setDisplayOrder(3);
        statusConfigRepo.save(escalated);

        TicketStatusConfig resolved = new TicketStatusConfig();
        resolved.setStatus(TicketStatus.RESOLVED);
        resolved.setLabel("Resolved");
        resolved.setDescription("Solution delivered");
        resolved.setDisplayOrder(4);
        statusConfigRepo.save(resolved);

        TicketStatusConfig closed = new TicketStatusConfig();
        closed.setStatus(TicketStatus.CLOSED);
        closed.setLabel("Closed");
        closed.setDescription("No further action");
        closed.setDisplayOrder(5);
        statusConfigRepo.save(closed);

        TicketPriorityConfig high = new TicketPriorityConfig();
        high.setPriority(TicketPriority.HIGH);
        high.setLabel("High");
        high.setFirstResponseSlaHours(1);
        high.setResolutionSlaHours(8);
        priorityConfigRepo.save(high);

        TicketPriorityConfig medium = new TicketPriorityConfig();
        medium.setPriority(TicketPriority.MEDIUM);
        medium.setLabel("Medium");
        medium.setFirstResponseSlaHours(4);
        medium.setResolutionSlaHours(24);
        priorityConfigRepo.save(medium);

        TicketPriorityConfig low = new TicketPriorityConfig();
        low.setPriority(TicketPriority.LOW);
        low.setLabel("Low");
        low.setFirstResponseSlaHours(24);
        low.setResolutionSlaHours(72);
        priorityConfigRepo.save(low);
    }

    // 👈 AJOUT : Méthode pour insérer le catalogue d'équipements
    private void seedEquipmentCatalogue() {
        String[][] equipmentData = {
            {"Server", "eq1", "🖥️"},
            {"Network switch", "eq2", "🔌"},
            {"Firewall", "eq3", "🛡️"},
            {"NAS / Storage", "eq4", "💾"},
            {"UPS", "eq5", "⚡"},
            {"Workstation", "eq6", "💻"},
            {"Printer", "eq7", "🖨️"},
            {"Router", "eq8", "📡"},
            {"Access point", "eq9", "📶"},
            {"Camera / CCTV", "eq10", "📷"}
        };

        for (String[] data : equipmentData) {
            EquipmentType eq = new EquipmentType();
            eq.setName(data[0]);
            eq.setCode(data[1]);
            eq.setIcon(data[2]);
            eq.setActive(true);
            equipmentTypeRepository.save(eq);
        }
    }
}