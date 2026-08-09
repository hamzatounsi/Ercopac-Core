package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.domain.DepartmentHoliday;
import com.ercopac.ercopac_tracker.department.repository.DepartmentHolidayRepository;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.domain.RolePermission;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.domain.ProjectApplicationType;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequest;
import com.ercopac.ercopac_tracker.projectum.change_requests.repository.ChangeRequestRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.ticketing.domain.Ticket;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketActivity;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketActivityType;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCategory;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketMessage;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketOrigin;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriority;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketReadState;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketActivityRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketMessageRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketReadStateRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Opt-in, idempotent local data only.  It intentionally does not run in a
 * deployed environment unless projectum.seed.local=true is explicitly set.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "projectum.seed.local", havingValue = "true")
public class DevelopmentTicketingDataSeeder implements CommandLineRunner {

    private static final String DEVELOPMENT_PASSWORD = "Test1234!";

    private final OrganisationRepository organisations;
    private final DepartmentRepository departments;
    private final UserRepository users;
    private final ResourceTypeRepository resourceTypes;
    private final TicketRepository tickets;
    private final TicketMessageRepository messages;
    private final TicketActivityRepository activities;
    private final TicketReadStateRepository readStates;
    private final ProjectRepository projects;
    private final ProjectTaskRepository tasks;
    private final DepartmentHolidayRepository holidays;
    private final RolePermissionRepository permissions;
    private final FinanceEntryRepository financeEntries;
    private final ForecastEntryRepository forecasts;
    private final RiskItemRepository risks;
    private final ChangeRequestRepository changeRequests;
    private final ActionItemRepository actions;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentTicketingDataSeeder(
            OrganisationRepository organisations,
            DepartmentRepository departments,
            UserRepository users,
            ResourceTypeRepository resourceTypes,
            TicketRepository tickets,
            TicketMessageRepository messages,
            TicketActivityRepository activities,
            TicketReadStateRepository readStates,
            ProjectRepository projects,
            ProjectTaskRepository tasks,
            DepartmentHolidayRepository holidays,
            RolePermissionRepository permissions,
            FinanceEntryRepository financeEntries,
            ForecastEntryRepository forecasts,
            RiskItemRepository risks,
            ChangeRequestRepository changeRequests,
            ActionItemRepository actions,
            PasswordEncoder passwordEncoder
    ) {
        this.organisations = organisations;
        this.departments = departments;
        this.users = users;
        this.resourceTypes = resourceTypes;
        this.tickets = tickets;
        this.messages = messages;
        this.activities = activities;
        this.readStates = readStates;
        this.projects = projects;
        this.tasks = tasks;
        this.holidays = holidays;
        this.permissions = permissions;
        this.financeEntries = financeEntries;
        this.forecasts = forecasts;
        this.risks = risks;
        this.changeRequests = changeRequests;
        this.actions = actions;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Organisation ercopac = organisation("ERCOPAC_DEMO", "ERCOPAC Demo", "Germany");
        Organisation testIndustries = organisation("PROJECTUM_TEST", "Projectum Test Industries", "Netherlands");
        Department engineering = department(ercopac, "ENGINEERING", "Engineering");
        Department production = department(ercopac, "PRODUCTION", "Production");
        Department finance = department(ercopac, "FINANCE", "Finance");
        Department sales = department(ercopac, "SALES", "Sales");
        Department quality = department(ercopac, "QUALITY", "Quality");
        Department operations = department(ercopac, "OPERATIONS", "Operations");
        Department testEngineering = department(testIndustries, "ENGINEERING", "Engineering");
        Department testProduction = department(testIndustries, "PRODUCTION", "Production");

        AppUser owner = user("owner@projectum.local", "Platform Owner", Role.PLATFORM_OWNER, null, null, true);
        user("org.admin@projectum.local", "ERCOPAC Organisation Admin", Role.ORG_ADMIN, ercopac, null, false);
        AppUser gm = user("gm@projectum.local", "ERCOPAC Project Manager", Role.PROJECT_MANAGER, ercopac, operations, true);
        AppUser departmentManager = user("department.manager@projectum.local", "Engineering Department Manager", Role.DEPARTMENT_MANAGER, ercopac, engineering, true);
        AppUser engineer = user("employee@projectum.local", "Engineering Specialist", Role.EMPLOYEE, ercopac, engineering, true);
        AppUser productionEmployee = user("production.employee@projectum.local", "Production Planner", Role.EMPLOYEE, ercopac, production, true);
        AppUser financeEmployee = user("finance.employee@projectum.local", "Financial Controller", Role.EMPLOYEE, ercopac, finance, true);
        AppUser qualityEmployee = user("quality.employee@projectum.local", "Quality Analyst", Role.EMPLOYEE, ercopac, quality, true);
        AppUser ercopacSales = user("sales.manager@projectum.local", "ERCOPAC Sales Manager", Role.SALES_MANAGER, ercopac, sales, true);
        AppUser ercopacClient = user("client@projectum.local", "ERCOPAC Client Contact", Role.CLIENT, ercopac, null, false);

        user("org.admin@testindustries.local", "Test Industries Organisation Admin", Role.ORG_ADMIN, testIndustries, null, false);
        AppUser testGm = user("gm@testindustries.local", "Test Industries Project Manager", Role.PROJECT_MANAGER, testIndustries, testProduction, true);
        AppUser testDm = user("department.manager@testindustries.local", "Test Industries Engineering Manager", Role.DEPARTMENT_MANAGER, testIndustries, testEngineering, true);
        AppUser testEmployee = user("employee@testindustries.local", "Test Industries Engineer", Role.EMPLOYEE, testIndustries, testEngineering, true);
        AppUser testSales = user("sales.manager@testindustries.local", "Test Industries Sales Manager", Role.SALES_MANAGER, testIndustries, testProduction, true);
        AppUser testClient = user("client@testindustries.local", "Test Industries Client", Role.CLIENT, testIndustries, null, false);

        engineering.setManager(departmentManager);
        testEngineering.setManager(testDm);
        departments.save(engineering);
        departments.save(testEngineering);
        seedPermissions(ercopac);
        seedPermissions(testIndustries);
        holiday(ercopac, engineering, engineer, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), "Planned leave", departmentManager);
        holiday(ercopac, production, productionEmployee, LocalDate.now().minusDays(2), LocalDate.now().plusDays(1), "Production shutdown coverage", gm);
        Project active = project(ercopac, gm, "ERCO-ERP-2026", "ERP Modernisation", "IN_PROGRESS", 48, LocalDate.now().minusDays(45), LocalDate.now().plusDays(90), new BigDecimal("850000"), "MEDIUM");
        Project atRisk = project(ercopac, gm, "ERCO-QA-2026", "Quality Traceability Upgrade", "AT_RISK", 30, LocalDate.now().minusDays(20), LocalDate.now().plusDays(35), new BigDecimal("420000"), "HIGH");
        Project completed = project(ercopac, gm, "ERCO-OPS-2025", "Operations Reporting Rollout", "COMPLETED", 100, LocalDate.now().minusMonths(6), LocalDate.now().minusDays(10), new BigDecimal("180000"), "LOW");
        Project tenantTwoProject = project(testIndustries, testGm, "PTI-MES-2026", "Manufacturing Execution Pilot", "IN_PROGRESS", 62, LocalDate.now().minusDays(30), LocalDate.now().plusDays(60), new BigDecimal("560000"), "MEDIUM");
        seedProjectTasks(active, engineering, production, engineer, productionEmployee);
        seedProjectTasks(atRisk, engineering, quality, engineer, qualityEmployee);
        seedProjectTasks(completed, operations, finance, financeEmployee, financeEmployee);
        seedProjectTasks(tenantTwoProject, testEngineering, testProduction, testEmployee, testEmployee);
        seedOperationalData(active, engineering, gm, engineer);
        seedOperationalData(atRisk, quality, gm, qualityEmployee);

        ticket("TKT-2026-100001", ercopac, ercopacClient, ercopacClient, null,
                "Unable to activate new user account", TicketCategory.ACCESS, TicketPriority.HIGH, TicketStatus.OPEN,
                "Berlin HQ", "A new employee cannot access the planning workspace.", false);
        ticket("TKT-2026-100002", ercopac, ercopacClient, ercopacClient, ercopacSales,
                "Integration timeout on purchase orders", TicketCategory.INTEGRATION, TicketPriority.CRITICAL, TicketStatus.IN_PROGRESS,
                "Berlin HQ", "The ERP integration times out during peak processing.", true);
        ticket("TKT-2026-100003", ercopac, ercopacSales, ercopacClient, ercopacSales,
                "Slow dashboard load for engineering", TicketCategory.PERFORMANCE, TicketPriority.MEDIUM, TicketStatus.ESCALATED,
                "Munich Site", "Engineering dashboard needs more than 20 seconds to load.", true);
        ticket("TKT-2026-100004", ercopac, ercopacClient, ercopacClient, ercopacSales,
                "Clarify invoice export fields", TicketCategory.BILLING, TicketPriority.LOW, TicketStatus.RESOLVED,
                "Berlin HQ", "The client requested clarification of the invoice export columns.", true);
        ticket("TKT-2026-100005", ercopac, ercopacSales, ercopacClient, ercopacSales,
                "Feature request: saved ticket filters", TicketCategory.FEATURE_REQUEST, TicketPriority.LOW, TicketStatus.CLOSED,
                "Berlin HQ", "Saved ticket filters have been delivered and accepted.", true);
        ticket("TKT-2026-100006", ercopac, ercopacClient, ercopacClient, ercopacSales,
                "Client reopened a resolved access request", TicketCategory.ACCESS, TicketPriority.MEDIUM, TicketStatus.REOPENED,
                "Berlin HQ", "The original workaround did not resolve the access issue.", true);
        ticket("TKT-2026-200001", testIndustries, testClient, testClient, testSales,
                "Demo tenant data import question", TicketCategory.DATA, TicketPriority.MEDIUM, TicketStatus.OPEN,
                "Hamburg Site", "Imported project dates require confirmation.", true);

        printCredentials();

        // Keep a platform-level actor in the object graph for testing platform visibility.
        if (owner.getId() == null) {
            throw new IllegalStateException("Development platform owner was not persisted");
        }
    }

    private void printCredentials() {
        System.out.println("=== DEVELOPMENT TEST ACCOUNTS (password: " + DEVELOPMENT_PASSWORD + ") ===");
        System.out.println("PLATFORM_OWNER owner@projectum.local | Platform");
        System.out.println("ORG_ADMIN org.admin@projectum.local | ERCOPAC Demo / Operations");
        System.out.println("PROJECT_MANAGER gm@projectum.local | ERCOPAC Demo / Operations");
        System.out.println("DEPARTMENT_MANAGER department.manager@projectum.local | ERCOPAC Demo / Engineering");
        System.out.println("EMPLOYEE employee@projectum.local | ERCOPAC Demo / Engineering");
        System.out.println("SALES_MANAGER sales.manager@projectum.local | ERCOPAC Demo / Sales");
        System.out.println("CLIENT client@projectum.local | ERCOPAC Demo");
        System.out.println("Second tenant: Projectum Test Industries (*.testindustries.local)");
    }

    private Organisation organisation(String code, String name, String country) {
        return organisations.findByCode(code).orElseGet(() -> {
            Organisation organisation = new Organisation();
            organisation.setCode(code);
            organisation.setName(name);
            organisation.setCountry(country);
            organisation.setDomain(code.toLowerCase() + ".local");
            organisation.setStatus(OrganisationStatus.ACTIVE);
            organisation.setActive(true);
            organisation.setPlan("ENTERPRISE");
            organisation.setUserLimit(100);
            organisation.setOrgAdminLicenceLimit(10);
            organisation.setProjectManagerLicenceLimit(10);
            organisation.setDepartmentManagerLicenceLimit(20);
            organisation.setEmployeeLicenceLimit(100);
            organisation.setSalesManagerLicenceLimit(20);
            organisation.setClientLicenceLimit(100);
            return organisations.save(organisation);
        });
    }

    private Department department(Organisation organisation, String code, String label) {
        return departments.findByCodeAndOrganisation_Id(code, organisation.getId()).orElseGet(() ->
                departments.save(new Department(code, label, organisation)));
    }

    private AppUser user(String email, String name, Role role, Organisation organisation, Department department, boolean internal) {
        return users.findByEmailIgnoreCase(email).map(existing -> {
            existing.setFullName(name);
            existing.setRole(role);
            existing.setOrganisation(organisation);
            existing.setDepartment(department);
            existing.setDepartmentCode(department == null ? null : department.getCode());
            existing.setResourceType(resourceTypeFor(role, organisation, department));
            existing.setInternalUser(role.requiresResourceProfile());
            existing.setActive(true);
            existing.setJobTitle(role.name());
            existing.setDefaultRate(new BigDecimal("120"));
            existing.setSeniority(role == Role.EMPLOYEE ? "MID" : "SENIOR");
            existing.setHoursPerDay(8);
            existing.setDaysPerWeek(5);
            existing.setWorkdays("MON-FRI");
            existing.setColor(role == Role.CLIENT ? "#7C3AED" : "#2563EB");
            if (!passwordEncoder.matches(DEVELOPMENT_PASSWORD, existing.getPasswordHash())) {
                existing.setPasswordHash(passwordEncoder.encode(DEVELOPMENT_PASSWORD));
            }
            return users.save(existing);
        }).orElseGet(() -> {
            AppUser created = new AppUser(name, email, passwordEncoder.encode(DEVELOPMENT_PASSWORD), role);
            created.setOrganisation(organisation);
            created.setDepartment(department);
            created.setDepartmentCode(department == null ? null : department.getCode());
            created.setResourceType(resourceTypeFor(role, organisation, department));
            created.setInternalUser(role.requiresResourceProfile());
            created.setJobTitle(role.name());
            created.setDefaultRate(new BigDecimal("120"));
            created.setSeniority(role == Role.EMPLOYEE ? "MID" : "SENIOR");
            created.setHoursPerDay(8);
            created.setDaysPerWeek(5);
            created.setWorkdays("MON-FRI");
            created.setColor(role == Role.CLIENT ? "#7C3AED" : "#2563EB");
            return users.save(created);
        });
    }

    private ResourceType resourceTypeFor(Role role, Organisation organisation, Department department) {
        if (!role.requiresResourceProfile()) {
            return null;
        }
        String code = switch (role) {
            case PROJECT_MANAGER -> "PROJECT_MANAGER";
            case SALES_MANAGER -> "SALES_REPRESENTATIVE";
            case DEPARTMENT_MANAGER -> "DEPARTMENT_MANAGER";
            case EMPLOYEE -> department.getCode() + "_SPECIALIST";
            default -> throw new IllegalStateException("Unexpected resource role");
        };
        String label = switch (role) {
            case PROJECT_MANAGER -> "Project Manager";
            case SALES_MANAGER -> "Sales Representative";
            case DEPARTMENT_MANAGER -> "Department Manager";
            case EMPLOYEE -> department.getLabel() + " Specialist";
            default -> throw new IllegalStateException("Unexpected resource role");
        };
        return resourceTypes.findByCodeAndOrganisation_Id(code, organisation.getId())
                .orElseGet(() -> resourceTypes.save(new ResourceType(code, label, organisation)));
    }

    private void seedPermissions(Organisation organisation) {
        grant(organisation, Role.PROJECT_MANAGER, List.of(
                PermissionModule.GM_DASHBOARD, PermissionModule.CRM, PermissionModule.PROJECTS,
                PermissionModule.PLANNING, PermissionModule.TASKS, PermissionModule.FINANCE,
                PermissionModule.FORECAST, PermissionModule.RISKS, PermissionModule.CHANGE_REQUESTS,
                PermissionModule.ACTIONS, PermissionModule.RESOURCES, PermissionModule.SUPPLIERS
        ), true);
        grant(organisation, Role.DEPARTMENT_MANAGER, List.of(
                PermissionModule.DEPARTMENT_DASHBOARD, PermissionModule.PROJECTS,
                PermissionModule.PLANNING, PermissionModule.TASKS, PermissionModule.RESOURCES
        ), true);
        grant(organisation, Role.EMPLOYEE, List.of(PermissionModule.EMPLOYEE_DASHBOARD, PermissionModule.TASKS), false);
        grant(organisation, Role.SALES_MANAGER, List.of(PermissionModule.CRM), true);
    }

    private void grant(Organisation organisation, Role role, List<PermissionModule> modules, boolean canWrite) {
        for (PermissionModule module : modules) {
            if (permissions.findByOrganisation_IdAndRoleAndModule(organisation.getId(), role, module).isPresent()) {
                continue;
            }
            RolePermission permission = new RolePermission();
            permission.setOrganisation(organisation);
            permission.setRole(role);
            permission.setModule(module);
            permission.setCanRead(true);
            permission.setCanWrite(canWrite);
            permissions.save(permission);
        }
    }

    private void holiday(Organisation organisation, Department department, AppUser member,
                         LocalDate from, LocalDate to, String note, AppUser createdBy) {
        if (holidays.existsByOrganisationIdAndMember_IdAndFromDateAndToDate(organisation.getId(), member.getId(), from, to)) {
            return;
        }
        DepartmentHoliday holiday = new DepartmentHoliday();
        holiday.setOrganisationId(organisation.getId());
        holiday.setDepartment(department);
        holiday.setMember(member);
        holiday.setFromDate(from);
        holiday.setToDate(to);
        holiday.setNote(note);
        holiday.setCreatedBy(createdBy.getId());
        holidays.save(holiday);
    }

    private Project project(Organisation organisation, AppUser manager, String code, String name, String phase,
                            int progress, LocalDate start, LocalDate end, BigDecimal budget, String riskLevel) {
        return projects.findByCode(code).orElseGet(() -> {
            Project project = new Project();
            project.setCode(code);
            project.setName(name);
            project.setShortName(code);
            project.setOrganisation(organisation);
            project.setApplicationType(ProjectApplicationType.PROJECTUM);
            project.setPortfolio("Development Portfolio");
            project.setOrgAssignment(organisation.getCode());
            project.setCountry(organisation.getCountry());
            project.setProjectType("Digital Transformation");
            project.setProjectPhase(phase);
            project.setPriority("HIGH");
            project.setPlannedStart(start);
            project.setPlannedEnd(end);
            project.setProjectBudget(budget);
            project.setTotalProjectBudget(budget);
            project.setEstimatedCost(budget.multiply(new BigDecimal("0.92")));
            project.setProgress(progress);
            project.setCustomer(organisation.getName() + " Customer");
            project.setCategory("Enterprise");
            project.setRiskLevel(riskLevel);
            project.setProjectManagerId(manager.getId());
            project.setProjectManagerName(manager.getFullName());
            project.setComment("Development seed project for portfolio and schedule testing.");
            return projects.save(project);
        });
    }

    private void seedProjectTasks(Project project, Department primaryDepartment, Department secondaryDepartment,
                                  AppUser primaryAssignee, AppUser secondaryAssignee) {
        task(project, "1", "Initiation", "SUMMARY", 0, null, primaryDepartment, null, 100,
                project.getPlannedStart(), project.getPlannedStart().plusDays(10), 0);
        ProjectTask delivery = task(project, "2", "Design and delivery", "SUMMARY", 0, null, primaryDepartment, null, 55,
                project.getPlannedStart().plusDays(11), project.getPlannedEnd().minusDays(15), 1);
        task(project, "2.1", "Architecture definition", "ACTIVITY", 1, delivery.getId(), primaryDepartment, primaryAssignee, 100,
                project.getPlannedStart().plusDays(11), project.getPlannedStart().plusDays(25), 2);
        task(project, "2.2", "Cross-functional implementation", "ACTIVITY", 1, delivery.getId(), secondaryDepartment, secondaryAssignee, 45,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(20), 3);
        task(project, "2.3", "Customer acceptance milestone", "MILESTONE", 1, delivery.getId(), primaryDepartment, primaryAssignee, 0,
                project.getPlannedEnd().minusDays(15), project.getPlannedEnd().minusDays(15), 4);
    }

    private ProjectTask task(Project project, String wbs, String name, String type, int outlineLevel,
                             Long parentId, Department department, AppUser assignee, int progress,
                             LocalDate start, LocalDate end, int displayOrder) {
        return tasks.findByProjectIdAndWbsCode(project.getId(), wbs).orElseGet(() -> {
            ProjectTask task = new ProjectTask();
            task.setProjectId(project.getId());
            task.setOrganisationId(project.getOrganisation().getId());
            task.setWbsCode(wbs);
            task.setName(name);
            task.setTaskType(type);
            task.setOutlineLevel(outlineLevel);
            task.setParentId(parentId);
            task.setDepartment(department);
            task.setAssignedUser(assignee);
            task.setPlannedStart(start);
            task.setPlannedEnd(end);
            task.setBaselineStart(start.minusDays(2));
            task.setBaselineEnd(end.minusDays(2));
            task.setDurationDays((int) (end.toEpochDay() - start.toEpochDay()) + 1);
            task.setPercentComplete(progress);
            task.setAllocationPercent(assignee == null ? 0 : (wbs.endsWith("2") ? 130 : 80));
            task.setPlannedHours(new BigDecimal("80"));
            task.setActualHours(new BigDecimal("48"));
            task.setPriority(2);
            task.setScheduleMode("AUTO");
            task.setStatus(progress == 100 ? "COMPLETED" : progress == 0 ? "NOT_STARTED" : "IN_PROGRESS");
            task.setColor(type.equals("MILESTONE") ? "#DC2626" : "#2563EB");
            task.setActive(true);
            task.setDisplayOrder(displayOrder);
            return tasks.save(task);
        });
    }

    private void seedOperationalData(Project project, Department department, AppUser manager, AppUser owner) {
        FinanceEntry finance = financeEntries.findAllByProjectIdOrderByWbsCodeAsc(project.getId()).stream()
                .filter(entry -> entry.getWbsCode().equals("2.2"))
                .findFirst()
                .orElseGet(() -> {
                    FinanceEntry entry = new FinanceEntry();
                    entry.setOrganisation(project.getOrganisation());
                    entry.setProject(project);
                    entry.setOwner(manager);
                    entry.setOwnerName(manager.getFullName());
                    entry.setWbsCode("2.2");
                    entry.setDescription("Implementation work package");
                    entry.setLevel(1);
                    entry.setSales(new BigDecimal("250000"));
                    entry.setBudget(new BigDecimal("180000"));
                    entry.setCommitment(new BigDecimal("150000"));
                    entry.setActualCost(new BigDecimal("175000"));
                    entry.setForecast(new BigDecimal("220000"));
                    entry.setCostReserve(new BigDecimal("15000"));
                    entry.setUpdatedBudget(new BigDecimal("195000"));
                    entry.setIsSummary(false);
                    return financeEntries.save(entry);
                });
        String period = LocalDate.now().plusMonths(1).toString().substring(0, 7);
        if (!forecasts.existsByProjectIdAndWbsCodeAndPeriodKey(project.getId(), "2.2", period)) {
            ForecastEntry forecast = new ForecastEntry();
            forecast.setOrganisation(project.getOrganisation());
            forecast.setProject(project);
            forecast.setFinanceEntry(finance);
            forecast.setWbsCode("2.2");
            forecast.setPeriodKey(period);
            forecast.setAmount(new BigDecimal("65000"));
            forecasts.save(forecast);
        }
        String riskDescription = "Supplier capacity may delay the implementation milestone";
        RiskItem risk = risks.findAllByProjectIdOrderByIdAsc(project.getId()).stream()
                .filter(item -> riskDescription.equals(item.getDescription()))
                .findFirst()
                .orElseGet(() -> {
                    RiskItem item = new RiskItem();
                    item.setOrganisation(project.getOrganisation());
                    item.setProject(project);
                    item.setRiskType("risk");
                    item.setState("managing");
                    item.setDescription(riskDescription);
                    item.setInputDate(LocalDate.now().minusDays(7));
                    item.setDueDate(LocalDate.now().plusDays(14));
                    item.setMitigation("Secure an alternate supplier and review capacity weekly.");
                    item.setOwnerUser(owner);
                    item.setWbsCode("2.2");
                    item.setImpact("5");
                    item.setProbability(70);
                    item.setVarianceStatus("open");
                    return risks.save(item);
                });
        ChangeRequest changeRequest = changeRequests.findAllByProjectIdOrderByIdAsc(project.getId()).stream()
                .filter(request -> "Approve additional implementation capacity".equals(request.getTitle()))
                .findFirst()
                .orElseGet(() -> {
                    ChangeRequest request = new ChangeRequest();
                    request.setOrganisation(project.getOrganisation());
                    request.setProject(project);
                    request.setTitle("Approve additional implementation capacity");
                    request.setStatus("submitted");
                    request.setRequestDate(LocalDate.now().minusDays(2));
                    request.setValueAmount(BigDecimal.ZERO);
                    request.setCostAmount(new BigDecimal("40000"));
                    request.setOwner(manager.getFullName());
                    request.setRequester(owner);
                    request.setApprover(manager);
                    request.setNote("Additional capacity protects the customer acceptance milestone.");
                    return changeRequests.save(request);
                });
        if (!actions.existsByProjectIdAndTitle(project.getId(), "Confirm supplier recovery plan")) {
            ActionItem action = new ActionItem();
            action.setOrganisation(project.getOrganisation());
            action.setProject(project);
            action.setTitle("Confirm supplier recovery plan");
            action.setDescription("Overdue corrective action linked to the seeded high risk and change request.");
            action.setActionType("action");
            action.setDepartment(department);
            action.setDepartmentCode(department.getCode());
            action.setPriority("high");
            action.setStatus("blocked");
            action.setCustomerVisible(false);
            action.setInsertedDate(LocalDate.now().minusDays(10));
            action.setDueDate(LocalDate.now().minusDays(1));
            action.setOwner(owner);
            action.setRisk(risk);
            action.setChangeRequest(changeRequest);
            actions.save(action);
        }
    }

    private void ticket(
            String number,
            Organisation organisation,
            AppUser creator,
            AppUser client,
            AppUser assignee,
            String subject,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            String site,
            String description,
            boolean includeInternalNote
    ) {
        if (tickets.findByTicketNumber(number).isPresent()) {
            return;
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(number);
        ticket.setOrganisation(organisation);
        ticket.setCreatedByUser(creator);
        ticket.setClientUser(client);
        ticket.setAssignedSalesManager(assignee);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setOrigin(TicketOrigin.WEB);
        ticket.setSite(site);
        ticket.setEscalationLevel(status == TicketStatus.ESCALATED ? 1 : 0);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(Instant.now().minusSeconds(86_400));
        }
        if (status == TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now().minusSeconds(3_600));
        }
        ticket = tickets.save(ticket);

        activity(ticket, creator, TicketActivityType.CREATED, null, status.name(), "Ticket created");
        if (assignee != null) {
            activity(ticket, assignee, TicketActivityType.ASSIGNED, null, assignee.getFullName(), "Assigned to sales manager");
        }
        if (status != TicketStatus.OPEN) {
            activity(ticket, assignee == null ? creator : assignee, activityFor(status), TicketStatus.OPEN.name(), status.name(), "Ticket lifecycle updated");
        }

        message(ticket, client, "We need help with: " + subject, false);
        if (assignee != null) {
            message(ticket, assignee, "Thanks, we are reviewing this request and will update you shortly.", false);
        }
        if (includeInternalNote && assignee != null) {
            message(ticket, assignee, "Internal note: verify the tenant configuration before replying.", true);
        }

        TicketReadState clientReadState = new TicketReadState();
        clientReadState.setTicket(ticket);
        clientReadState.setUser(client);
        clientReadState.setLastReadAt(Instant.now().minusSeconds(3_600));
        readStates.save(clientReadState);
    }

    private TicketActivityType activityFor(TicketStatus status) {
        return switch (status) {
            case IN_PROGRESS -> TicketActivityType.STATUS_CHANGED;
            case ESCALATED -> TicketActivityType.ESCALATED;
            case RESOLVED -> TicketActivityType.RESOLVED;
            case CLOSED -> TicketActivityType.CLOSED;
            case REOPENED -> TicketActivityType.REOPENED;
            default -> TicketActivityType.UPDATED;
        };
    }

    private void activity(Ticket ticket, AppUser actor, TicketActivityType type, String previous, String next, String description) {
        TicketActivity activity = new TicketActivity();
        activity.setTicket(ticket);
        activity.setActor(actor);
        activity.setActivityType(type);
        activity.setPreviousValue(previous);
        activity.setNewValue(next);
        activity.setDescription(description);
        activities.save(activity);
    }

    private void message(Ticket ticket, AppUser sender, String body, boolean internalNote) {
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(body);
        message.setInternalNote(internalNote);
        message.setMessageType(internalNote ? "INTERNAL_NOTE" : "MESSAGE");
        messages.save(message);
    }
}
