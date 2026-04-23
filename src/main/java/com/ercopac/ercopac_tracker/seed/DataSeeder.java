package com.ercopac.ercopac_tracker.seed;


import com.ercopac.ercopac_tracker.department.domain.DepartmentHoliday;
import com.ercopac.ercopac_tracker.department.repository.DepartmentHolidayRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final ProjectPlanningRepository planningRepo;
    private final OrganisationRepository organisationRepo;
    private final ProjectTaskRepository taskRepo;
    private final TaskDependencyRepository dependencyRepo;
    private final DepartmentHolidayRepository holidayRepo;
    private final FinanceEntryRepository financeRepo;
    private final ForecastEntryRepository forecastRepo;
    private final RiskItemRepository riskRepo;
    private final ActionItemRepository actionRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo,
                      ProjectRepository projectRepo,
                      ProjectPlanningRepository planningRepo,
                      OrganisationRepository organisationRepo,
                      ProjectTaskRepository taskRepo,
                      TaskDependencyRepository dependencyRepo,
                      DepartmentHolidayRepository holidayRepo,
                      FinanceEntryRepository financeRepo,
                      ForecastEntryRepository forecastRepo,
                      RiskItemRepository riskRepo,
                      ActionItemRepository actionRepo,
                      PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.planningRepo = planningRepo;
        this.organisationRepo = organisationRepo;
        this.taskRepo = taskRepo;
        this.dependencyRepo = dependencyRepo;
        this.holidayRepo = holidayRepo;
        this.financeRepo = financeRepo;
        this.forecastRepo = forecastRepo;
        this.riskRepo = riskRepo;
        this.actionRepo = actionRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();

        // ---------------------------------------------------------------------
        // ORGANISATIONS
        // ---------------------------------------------------------------------
        Organisation ercopac = createOrUpdateOrganisation("ERCOPAC", "ERCOPAC");
        Organisation demo = createOrUpdateOrganisation("Demo Company", "DEMO");

        // ---------------------------------------------------------------------
        // PLATFORM USERS
        // ---------------------------------------------------------------------
        createOrUpdatePlatformUser(
                "Platform Owner",
                "owner@ercopac.com",
                "Admin123!",
                Role.PLATFORM_OWNER,
                "#111827"
        );

        createOrUpdatePlatformUser(
                "Platform Admin",
                "platformadmin@ercopac.com",
                "Admin123!",
                Role.PLATFORM_ADMIN,
                "#334155"
        );

        // ---------------------------------------------------------------------
        // ERCOPAC USERS
        // ---------------------------------------------------------------------
        AppUser gm = createOrUpdateOrgUser(
                "Hamza General Manager",
                "gm@ercopac.com",
                "Test1234",
                Role.GENERAL_MANAGER,
                ercopac,
                "EXEC",
                "General Manager",
                "MANAGEMENT",
                "EXECUTIVE",
                8,
                5,
                "MON-FRI",
                "#2563eb",
                true
        );

        AppUser dmPm = createOrUpdateOrgUser(
                "Amina PM Manager",
                "dm.pm@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "PM",
                "Project Management Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#1d4ed8",
                true
        );

        AppUser dmMe = createOrUpdateOrgUser(
                "Karim Mechanical Manager",
                "dm.me@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "ME",
                "Mechanical Department Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#0891b2",
                true
        );

        AppUser dmCe = createOrUpdateOrgUser(
                "Salma Electrical Manager",
                "dm.ce@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "CE",
                "Electrical Department Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#7c3aed",
                true
        );

        AppUser dmSw = createOrUpdateOrgUser(
                "Youssef Software Manager",
                "dm.sw@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "SW",
                "Software Department Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#0f766e",
                true
        );

        AppUser dmPrc = createOrUpdateOrgUser(
                "Meriem Procurement Manager",
                "dm.prc@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "PRC",
                "Procurement Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#ea580c",
                true
        );

        AppUser dmMfc = createOrUpdateOrgUser(
                "Nader Manufacturing Manager",
                "dm.mfc@ercopac.com",
                "Test1234",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "MFC",
                "Manufacturing Manager",
                "MANAGEMENT",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#dc2626",
                true
        );

        AppUser pmLead = createOrUpdateOrgUser(
                "Sonia PM Lead",
                "pm.lead@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "PM",
                "Project Manager",
                "PM",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#2563eb",
                true
        );

        AppUser pmCoordinator = createOrUpdateOrgUser(
                "Walid PM Coordinator",
                "pm.coord@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "PM",
                "Project Coordinator",
                "PM",
                "MID",
                8,
                5,
                "MON-FRI",
                "#3b82f6",
                true
        );

        AppUser me1 = createOrUpdateOrgUser(
                "Hedi Mechanical Engineer",
                "me1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "ME",
                "Mechanical Engineer",
                "ME",
                "MID",
                8,
                5,
                "MON-FRI",
                "#06b6d4",
                true
        );

        AppUser me2 = createOrUpdateOrgUser(
                "Nour Mechanical Designer",
                "me2@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "ME",
                "Mechanical Designer",
                "ME",
                "MID",
                8,
                5,
                "MON-FRI",
                "#22d3ee",
                true
        );

        AppUser ce1 = createOrUpdateOrgUser(
                "Rim Electrical Engineer",
                "ce1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "CE",
                "Electrical Engineer",
                "CE",
                "MID",
                8,
                5,
                "MON-FRI",
                "#8b5cf6",
                true
        );

        AppUser ce2 = createOrUpdateOrgUser(
                "Bilel Automation Engineer",
                "ce2@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "CE",
                "Automation Engineer",
                "CE",
                "MID",
                8,
                5,
                "MON-FRI",
                "#a855f7",
                true
        );

        AppUser sw1 = createOrUpdateOrgUser(
                "Omar Software Engineer",
                "sw1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "SW",
                "Software Engineer",
                "SW",
                "MID",
                8,
                5,
                "MON-FRI",
                "#10b981",
                true
        );

        AppUser sw2 = createOrUpdateOrgUser(
                "Ines QA Engineer",
                "sw2@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "SW",
                "QA Engineer",
                "SW",
                "MID",
                8,
                5,
                "MON-FRI",
                "#14b8a6",
                true
        );

        AppUser prc1 = createOrUpdateOrgUser(
                "Ahmed Buyer",
                "prc1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "PRC",
                "Buyer",
                "PRC",
                "MID",
                8,
                5,
                "MON-FRI",
                "#f97316",
                true
        );

        AppUser prc2 = createOrUpdateOrgUser(
                "Dorra Supply Coordinator",
                "prc2@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "PRC",
                "Supply Coordinator",
                "PRC",
                "MID",
                8,
                5,
                "MON-FRI",
                "#fb923c",
                true
        );

        AppUser mfc1 = createOrUpdateOrgUser(
                "Maher Manufacturing Engineer",
                "mfc1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "MFC",
                "Manufacturing Engineer",
                "MFC",
                "MID",
                8,
                5,
                "MON-FRI",
                "#ef4444",
                true
        );

        AppUser mfc2 = createOrUpdateOrgUser(
                "Sarra Site Supervisor",
                "mfc2@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "MFC",
                "Site Supervisor",
                "MFC",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#f43f5e",
                true
        );

        AppUser meSupplier1 = createOrUpdateOrgUser(
        "Marco Mechanical Supplier",
        "supplier.me1@ercopac.com",
        "Test1234",
        Role.EMPLOYEE,
        ercopac,
        "ME",
        "External Mechanical Designer",
        "ME",
        "SENIOR",
        8,
        5,
        "MON-FRI",
        "#f59e0b",
        false
        );

        AppUser ceSupplier1 = createOrUpdateOrgUser(
                "Elena Electrical Supplier",
                "supplier.ce1@ercopac.com",
                "Test1234",
                "EMPLOYEE".equals(Role.EMPLOYEE.name()) ? Role.EMPLOYEE : Role.EMPLOYEE,
                ercopac,
                "CE",
                "External Electrical Engineer",
                "CE",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#f97316",
                false
        );

        AppUser swSupplier1 = createOrUpdateOrgUser(
                "Lucas Software Supplier",
                "supplier.sw1@ercopac.com",
                "Test1234",
                Role.EMPLOYEE,
                ercopac,
                "SW",
                "External Software Consultant",
                "SW",
                "SENIOR",
                8,
                5,
                "MON-FRI",
                "#fb923c",
                false
        );

        // ---------------------------------------------------------------------
        // DEMO USERS
        // ---------------------------------------------------------------------
        AppUser demoGm = createOrUpdateOrgUser(
                "Demo GM",
                "gm@demo.com",
                "Test1234",
                Role.GENERAL_MANAGER,
                demo,
                "EXEC",
                "General Manager",
                "MANAGEMENT",
                "EXECUTIVE",
                8,
                5,
                "MON-FRI",
                "#6366f1",
                true
        );

        createOrUpdateOrgUser(
                "Demo Employee",
                "employee@demo.com",
                "Test1234",
                Role.EMPLOYEE,
                demo,
                "OPS",
                "Operations Analyst",
                "OPS",
                "MID",
                8,
                5,
                "MON-FRI",
                "#22c55e",
                true
        );

        // ---------------------------------------------------------------------
        // PROJECTS
        // ---------------------------------------------------------------------
        Project ferrero = createOrUpdateProject(
                "PRJ-26003",
                "Ferrero Line Expansion",
                "Ferrero",
                ercopac,
                gm.getId(),
                "Packaging",
                "Tunisia",
                "Industrial",
                "Execution",
                "P1",
                today.minusDays(40),
                today.plusDays(130),
                new BigDecimal("950000"),
                new BigDecimal("1200000")
        );

        Project digitalFactory = createOrUpdateProject(
                "PRJ-24011",
                "Digital Factory Rollout",
                "DFR",
                ercopac,
                gm.getId(),
                "Transformation",
                "Germany",
                "Digital",
                "Execution",
                "P1",
                today.minusDays(15),
                today.plusDays(75),
                new BigDecimal("420000"),
                new BigDecimal("510000")
        );

        Project hrProject = createOrUpdateProject(
                "PRJ-24020",
                "HR Process Digitalization",
                "HRD",
                ercopac,
                gm.getId(),
                "Human Resources",
                "France",
                "Internal",
                "Execution",
                "P2",
                today.minusDays(10),
                today.plusDays(60),
                new BigDecimal("110000"),
                new BigDecimal("145000")
        );

        Project demoProject = createOrUpdateProject(
                "PRJ-DEMO-001",
                "Demo Warehouse Digitalization",
                "DWD",
                demo,
                demoGm.getId(),
                "Logistics",
                "France",
                "Digital",
                "Execution",
                "P2",
                today.minusDays(12),
                today.plusDays(55),
                new BigDecimal("85000"),
                new BigDecimal("98000")
        );

        // ---------------------------------------------------------------------
        // PLANNING
        // ---------------------------------------------------------------------
        createOrUpdatePlanning(
                ferrero.getId(),
                today.minusDays(36),
                today.plusDays(138),
                85,
                "Ferrero, packaging, commissioning, FAT, CS handover"
        );

        createOrUpdatePlanning(
                digitalFactory.getId(),
                today.minusDays(12),
                today.plusDays(82),
                75,
                "MES, ERP, dashboard, integration"
        );

        createOrUpdatePlanning(
                hrProject.getId(),
                today.minusDays(8),
                today.plusDays(65),
                78,
                "workflow, hr, onboarding, policies"
        );

        createOrUpdatePlanning(
                demoProject.getId(),
                today.minusDays(10),
                today.plusDays(60),
                72,
                "demo, warehouse, digitalization"
        );

        // ---------------------------------------------------------------------
        // RICH SCHEDULE - FERRERO MAIN PROJECT
        // ---------------------------------------------------------------------
        seedTask(
                ferrero.getId(), "1.0", "Project Management", "SUMMARY",
                today.minusDays(35), today.plusDays(110),
                today.minusDays(33), today.plusDays(108),
                today.minusDays(35), today.plusDays(112),
                55, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 10, "PM", false, null, pmLead
        );

        seedTask(
                ferrero.getId(), "1.1", "PO Purchase Order", "MILESTONE",
                today.minusDays(26), today.minusDays(26),
                today.minusDays(26), today.minusDays(26),
                today.minusDays(26), today.minusDays(26),
                100, 1, "MANUAL", "DONE", "#7c3aed",
                true, 11, "PM", true, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "1.2", "Internal Kick-off", "ACTIVITY",
                today.minusDays(25), today.minusDays(24),
                today.minusDays(25), today.minusDays(24),
                today.minusDays(25), today.minusDays(24),
                100, 1, "AUTO", "DONE", "#16a34a",
                true, 12, "PM", false, pmCoordinator, pmCoordinator
        );

        seedTask(
                ferrero.getId(), "1.3", "Customer Design Approval", "MILESTONE",
                today.minusDays(24), today.minusDays(24),
                today.minusDays(22), today.minusDays(22),
                today.minusDays(23), today.minusDays(23),
                100, 1, "MANUAL", "DONE", "#f59e0b",
                true, 13, "PM", true, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "1.4", "FAT", "MILESTONE",
                today.plusDays(45), today.plusDays(45),
                today.plusDays(49), today.plusDays(49),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 14, "PM", false, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "1.5", "Shipment", "MILESTONE",
                today.plusDays(52), today.plusDays(52),
                today.plusDays(56), today.plusDays(56),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 15, "PM", false, prc1, prc1
        );

        seedTask(
                ferrero.getId(), "1.6", "Ready to Run", "MILESTONE",
                today.plusDays(88), today.plusDays(88),
                today.plusDays(92), today.plusDays(92),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 16, "PM", false, mfc2, mfc2
        );

        seedTask(
                ferrero.getId(), "1.7", "Final Acceptance", "MILESTONE",
                today.plusDays(102), today.plusDays(102),
                today.plusDays(106), today.plusDays(106),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#f59e0b",
                true, 17, "PM", true, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "1.8", "CS Handover", "MILESTONE",
                today.plusDays(108), today.plusDays(108),
                today.plusDays(112), today.plusDays(112),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 18, "CS", false, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "2.0", "Customer Activity", "SUMMARY",
                today.minusDays(20), today.plusDays(20),
                today.minusDays(18), today.plusDays(24),
                today.minusDays(20), today.plusDays(22),
                35, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 20, "PM", false, null, pmCoordinator
        );

        seedTask(
                ferrero.getId(), "2.1", "Site Ready", "ACTIVITY",
                today.minusDays(20), today.minusDays(13),
                today.minusDays(18), today.minusDays(11),
                today.minusDays(20), today.minusDays(14),
                100, 1, "AUTO", "DONE", "#0ea5e9",
                true, 21, "PM", false, opsUserFallback(mfc2), pmCoordinator
        );

        seedTask(
                ferrero.getId(), "2.2", "Utilities — Power", "ACTIVITY",
                today.minusDays(9), today.minusDays(3),
                today.minusDays(6), today.plusDays(1),
                today.minusDays(9), today.minusDays(2),
                65, 1, "AUTO", "IN_PROGRESS", "#0ea5e9",
                true, 22, "CE", false, ce1, ce1
        );

        seedTask(
                ferrero.getId(), "2.3", "Utilities — Network", "ACTIVITY",
                today.minusDays(9), today.minusDays(3),
                today.minusDays(6), today.plusDays(1),
                today.minusDays(9), today.minusDays(2),
                60, 1, "AUTO", "IN_PROGRESS", "#0ea5e9",
                true, 23, "SW", false, sw1, sw1
        );

        seedTask(
                ferrero.getId(), "2.4", "Ready to Test", "ACTIVITY",
                today.plusDays(2), today.plusDays(7),
                today.plusDays(6), today.plusDays(11),
                null, null,
                12, 2, "AUTO", "PLANNED", "#0ea5e9",
                true, 24, "PM", false, sw2, pmCoordinator
        );

        seedTask(
                ferrero.getId(), "2.5", "Customer Item 5", "ACTIVITY",
                today.plusDays(16), today.plusDays(21),
                today.plusDays(20), today.plusDays(25),
                null, null,
                0, 2, "AUTO", "PLANNED", "#0ea5e9",
                true, 25, "PM", false, pmCoordinator, pmCoordinator
        );

        seedTask(
                ferrero.getId(), "2.6", "Customer Item 6", "ACTIVITY",
                today.plusDays(22), today.plusDays(27),
                today.plusDays(26), today.plusDays(31),
                null, null,
                0, 2, "AUTO", "PLANNED", "#0ea5e9",
                true, 26, "PM", false, pmCoordinator, pmCoordinator
        );

        seedTask(
        ferrero.getId(), "2.7", "Supplier Electrical Validation", "ACTIVITY",
        today.plusDays(8), today.plusDays(12),
        today.plusDays(10), today.plusDays(14),
        null, null,
        0, 1, "AUTO", "PLANNED", "#f97316",
        true, 27, "CE", false, ceSupplier1, ceSupplier1
        );

        seedTask(
                ferrero.getId(), "3.0", "Design", "SUMMARY",
                today.minusDays(30), today.plusDays(12),
                today.minusDays(28), today.plusDays(16),
                today.minusDays(30), today.plusDays(14),
                42, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 30, "ME", false, null, dmMe
        );

        seedTask(
                ferrero.getId(), "3.1", "ME Design", "SUMMARY",
                today.minusDays(30), today.minusDays(2),
                today.minusDays(28), today.plusDays(1),
                today.minusDays(30), today.minusDays(5),
                72, 1, "AUTO", "IN_PROGRESS", "#0891b2",
                true, 31, "ME", false, null, dmMe
        );

        seedTask(
                ferrero.getId(), "3.1.1", "F10Z0001", "SUMMARY",
                today.minusDays(30), today.minusDays(12),
                today.minusDays(28), today.minusDays(10),
                today.minusDays(30), today.minusDays(14),
                85, 1, "AUTO", "IN_PROGRESS", "#0891b2",
                true, 32, "ME", false, null, me1
        );

        seedTask(
                ferrero.getId(), "3.1.1.1", "Machine 1 Design", "ACTIVITY",
                today.minusDays(30), today.minusDays(21),
                today.minusDays(28), today.minusDays(18),
                today.minusDays(30), today.minusDays(22),
                100, 1, "AUTO", "DONE", "#06b6d4",
                true, 33, "ME", false, me1, me1
        );

        seedTask(
                ferrero.getId(), "3.1.1.2", "BOM", "ACTIVITY",
                today.minusDays(20), today.minusDays(15),
                today.minusDays(18), today.minusDays(12),
                today.minusDays(20), today.minusDays(16),
                80, 1, "AUTO", "IN_PROGRESS", "#06b6d4",
                true, 34, "ME", false, me2, me2
        );

        seedTask(
                ferrero.getId(), "3.1.2", "F10Z0002", "SUMMARY",
                today.minusDays(16), today.minusDays(10),
                today.minusDays(14), today.minusDays(7),
                today.minusDays(16), today.minusDays(11),
                60, 1, "AUTO", "IN_PROGRESS", "#0891b2",
                true, 35, "ME", false, null, me1
        );

        seedTask(
                ferrero.getId(), "3.1.2.1", "Machine 2 Design", "ACTIVITY",
                today.minusDays(16), today.minusDays(13),
                today.minusDays(14), today.minusDays(10),
                today.minusDays(16), today.minusDays(13),
                100, 1, "AUTO", "DONE", "#06b6d4",
                true, 36, "ME", false, me1, me1
        );

        seedTask(
                ferrero.getId(), "3.1.2.2", "BOM", "ACTIVITY",
                today.minusDays(12), today.minusDays(10),
                today.minusDays(10), today.minusDays(8),
                today.minusDays(12), today.minusDays(10),
                50, 1, "AUTO", "IN_PROGRESS", "#06b6d4",
                true, 37, "ME", false, me2, me2
        );

        seedTask(
                ferrero.getId(), "3.1.3", "F10Z0003", "SUMMARY",
                today.minusDays(9), today.minusDays(5),
                today.minusDays(7), today.minusDays(1),
                today.minusDays(9), today.minusDays(4),
                30, 1, "AUTO", "IN_PROGRESS", "#0891b2",
                true, 38, "ME", false, null, me1
        );

        seedTask(
                ferrero.getId(), "3.1.3.1", "Design", "ACTIVITY",
                today.minusDays(9), today.minusDays(7),
                today.minusDays(7), today.minusDays(5),
                today.minusDays(9), today.minusDays(7),
                40, 1, "AUTO", "IN_PROGRESS", "#06b6d4",
                true, 39, "ME", false, me1, me1
        );

        seedTask(
                ferrero.getId(), "3.1.3.2", "BOM", "ACTIVITY",
                today.minusDays(6), today.minusDays(5),
                today.minusDays(4), today.minusDays(2),
                null, null,
                0, 1, "AUTO", "PLANNED", "#06b6d4",
                true, 40, "ME", false, me2, me2
        );

        seedTask(
        ferrero.getId(), "3.1.3.3", "Supplier Mechanical Review", "ACTIVITY",
        today.minusDays(5), today.minusDays(3),
        today.minusDays(3), today.minusDays(1),
        null, null,
        0, 1, "AUTO", "PLANNED", "#f59e0b",
        true, 41, "ME", false, meSupplier1, meSupplier1
        );

        seedTask(
                ferrero.getId(), "4.0", "Procurement", "SUMMARY",
                today.plusDays(1), today.plusDays(36),
                today.plusDays(5), today.plusDays(40),
                null, null,
                25, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 50, "PRC", false, null, dmPrc
        );

        seedTask(
                ferrero.getId(), "4.1", "Long Lead Items", "ACTIVITY",
                today.plusDays(1), today.plusDays(16),
                today.plusDays(5), today.plusDays(20),
                null, null,
                30, 1, "AUTO", "IN_PROGRESS", "#f97316",
                true, 51, "PRC", false, prc1, prc1
        );

        seedTask(
                ferrero.getId(), "4.2", "Standard Components", "ACTIVITY",
                today.plusDays(3), today.plusDays(26),
                today.plusDays(7), today.plusDays(30),
                null, null,
                20, 1, "AUTO", "IN_PROGRESS", "#f97316",
                true, 52, "PRC", false, prc2, prc2
        );

        seedTask(
                ferrero.getId(), "5.0", "Manufacturing", "SUMMARY",
                today.plusDays(26), today.plusDays(54),
                today.plusDays(30), today.plusDays(58),
                null, null,
                10, 1, "AUTO", "PLANNED", "#6b7280",
                true, 60, "MFC", false, null, dmMfc
        );

        seedTask(
                ferrero.getId(), "5.1", "Mechanical Assembly", "ACTIVITY",
                today.plusDays(26), today.plusDays(39),
                today.plusDays(30), today.plusDays(43),
                null, null,
                15, 1, "AUTO", "PLANNED", "#ef4444",
                true, 61, "MFC", false, mfc1, mfc1
        );

        seedTask(
                ferrero.getId(), "5.2", "Electrical Assembly", "ACTIVITY",
                today.plusDays(33), today.plusDays(47),
                today.plusDays(37), today.plusDays(51),
                null, null,
                8, 1, "AUTO", "PLANNED", "#ef4444",
                true, 62, "MFC", false, ce1, ce1
        );

        seedTask(
                ferrero.getId(), "5.3", "Internal Testing", "ACTIVITY",
                today.plusDays(48), today.plusDays(53),
                today.plusDays(52), today.plusDays(57),
                null, null,
                0, 1, "AUTO", "PLANNED", "#ef4444",
                true, 63, "SW", false, sw2, sw2
        );

        seedTask(
        ferrero.getId(), "7.4", "Supplier Software Support", "ACTIVITY",
        today.plusDays(88), today.plusDays(94),
        today.plusDays(92), today.plusDays(98),
        null, null,
        0, 1, "AUTO", "PLANNED", "#fb923c",
        true, 84, "SW", false, swSupplier1, swSupplier1
        );

        seedTask(
                ferrero.getId(), "6.0", "FAT & Delivery", "SUMMARY",
                today.plusDays(55), today.plusDays(72),
                today.plusDays(59), today.plusDays(76),
                null, null,
                0, 1, "AUTO", "PLANNED", "#6b7280",
                true, 70, "PM", false, null, pmLead
        );

        seedTask(
                ferrero.getId(), "6.1", "Factory Acceptance Test", "MILESTONE",
                today.plusDays(59), today.plusDays(59),
                today.plusDays(63), today.plusDays(63),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#f59e0b",
                true, 71, "PM", true, pmLead, pmLead
        );

        seedTask(
                ferrero.getId(), "6.2", "Packaging & Shipment", "ACTIVITY",
                today.plusDays(60), today.plusDays(68),
                today.plusDays(64), today.plusDays(72),
                null, null,
                0, 1, "AUTO", "PLANNED", "#f97316",
                true, 72, "MFC", false, prc2, prc2
        );

        seedTask(
                ferrero.getId(), "7.0", "Site Installation", "SUMMARY",
                today.plusDays(73), today.plusDays(94),
                today.plusDays(77), today.plusDays(98),
                null, null,
                0, 1, "AUTO", "PLANNED", "#6b7280",
                true, 80, "MFC", false, null, dmMfc
        );

        seedTask(
                ferrero.getId(), "7.1", "Mechanical Installation", "ACTIVITY",
                today.plusDays(73), today.plusDays(81),
                today.plusDays(77), today.plusDays(85),
                null, null,
                0, 1, "AUTO", "PLANNED", "#ef4444",
                true, 81, "MFC", false, mfc2, mfc2
        );

        seedTask(
                ferrero.getId(), "7.2", "Electrical Installation", "ACTIVITY",
                today.plusDays(77), today.plusDays(85),
                today.plusDays(81), today.plusDays(89),
                null, null,
                0, 1, "AUTO", "PLANNED", "#8b5cf6",
                true, 82, "CE", false, ce2, ce2
        );

        seedTask(
                ferrero.getId(), "7.3", "Commissioning", "ACTIVITY",
                today.plusDays(86), today.plusDays(92),
                today.plusDays(90), today.plusDays(96),
                null, null,
                0, 1, "AUTO", "PLANNED", "#10b981",
                true, 83, "SW", false, sw1, sw1
        );

        seedTask(
                ferrero.getId(), "8.0", "Go Live", "MILESTONE",
                today.plusDays(98), today.plusDays(98),
                today.plusDays(102), today.plusDays(102),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 90, "PM", false, pmLead, pmLead
        );

        // ---------------------------------------------------------------------
        // DEPENDENCIES - FERRERO
        // ---------------------------------------------------------------------
        seedDependency(ferrero.getId(), "1.1", "1.2", "FS", 0);
        seedDependency(ferrero.getId(), "1.2", "1.3", "FS", 0);
        seedDependency(ferrero.getId(), "1.3", "3.0", "FS", 0);

        seedDependency(ferrero.getId(), "2.1", "2.2", "FS", 0);
        seedDependency(ferrero.getId(), "2.1", "2.3", "FS", 0);
        seedDependency(ferrero.getId(), "2.2", "2.4", "FS", 0);
        seedDependency(ferrero.getId(), "2.3", "2.4", "FS", 0);
        seedDependency(ferrero.getId(), "2.4", "2.5", "FS", 0);
        seedDependency(ferrero.getId(), "2.5", "2.6", "FS", 0);

        seedDependency(ferrero.getId(), "3.1.1.1", "3.1.1.2", "FS", 0);
        seedDependency(ferrero.getId(), "3.1.1.2", "3.1.2.1", "FS", 0);
        seedDependency(ferrero.getId(), "3.1.2.1", "3.1.2.2", "FS", 0);
        seedDependency(ferrero.getId(), "3.1.2.2", "3.1.3.1", "FS", 0);
        seedDependency(ferrero.getId(), "3.1.3.1", "3.1.3.2", "FS", 0);

        seedDependency(ferrero.getId(), "3.1.3.2", "4.1", "FS", 0);
        seedDependency(ferrero.getId(), "3.1.3.2", "4.2", "FS", 0);

        seedDependency(ferrero.getId(), "4.1", "5.1", "FS", 0);
        seedDependency(ferrero.getId(), "4.2", "5.2", "FS", 0);
        seedDependency(ferrero.getId(), "5.1", "5.2", "FS", 0);
        seedDependency(ferrero.getId(), "5.2", "5.3", "FS", 0);

        seedDependency(ferrero.getId(), "5.3", "6.1", "FS", 0);
        seedDependency(ferrero.getId(), "6.1", "6.2", "FS", 0);
        seedDependency(ferrero.getId(), "6.2", "7.1", "FS", 0);
        seedDependency(ferrero.getId(), "7.1", "7.2", "SS", 0);
        seedDependency(ferrero.getId(), "7.2", "7.3", "FS", 0);
        seedDependency(ferrero.getId(), "7.3", "1.6", "FS", 0);
        seedDependency(ferrero.getId(), "1.6", "8.0", "FS", 0);
        seedDependency(ferrero.getId(), "8.0", "1.7", "FS", 0);
        seedDependency(ferrero.getId(), "1.7", "1.8", "FS", 0);

        // ---------------------------------------------------------------------
        // SMALLER SCHEDULES FOR OTHER PROJECTS
        // ---------------------------------------------------------------------
        seedTask(
                digitalFactory.getId(), "1.0", "Digital Factory Rollout", "SUMMARY",
                today.minusDays(12), today.plusDays(60),
                today.minusDays(10), today.plusDays(65),
                today.minusDays(12), today.plusDays(62),
                48, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 1, "SW", false, null, sw1
        );

        seedTask(
                digitalFactory.getId(), "1.1", "MES Integration", "ACTIVITY",
                today.minusDays(12), today.plusDays(8),
                today.minusDays(10), today.plusDays(10),
                today.minusDays(12), today.plusDays(6),
                65, 1, "AUTO", "IN_PROGRESS", "#10b981",
                true, 2, "SW", false, sw1, sw1
        );

        seedTask(
                digitalFactory.getId(), "1.2", "Dashboard Deployment", "ACTIVITY",
                today.plusDays(5), today.plusDays(18),
                today.plusDays(9), today.plusDays(22),
                null, null,
                20, 2, "AUTO", "PLANNED", "#0ea5e9",
                true, 3, "SW", false, sw2, sw2
        );

        seedTask(
                digitalFactory.getId(), "1.3", "Go Live", "MILESTONE",
                today.plusDays(45), today.plusDays(45),
                today.plusDays(50), today.plusDays(50),
                null, null,
                0, 1, "MANUAL", "PLANNED", "#7c3aed",
                true, 4, "PM", false, pmLead, pmLead
        );

        seedDependency(digitalFactory.getId(), "1.1", "1.2", "FS", 0);
        seedDependency(digitalFactory.getId(), "1.2", "1.3", "FS", 0);

        seedTask(
                hrProject.getId(), "1.0", "HR Digitalization", "SUMMARY",
                today.minusDays(8), today.plusDays(48),
                today.minusDays(6), today.plusDays(52),
                today.minusDays(8), today.plusDays(50),
                38, 1, "AUTO", "IN_PROGRESS", "#6b7280",
                true, 1, "HR", false, null, gm
        );

        seedTask(
                hrProject.getId(), "1.1", "Workflow Mapping", "ACTIVITY",
                today.minusDays(8), today.plusDays(4),
                today.minusDays(6), today.plusDays(6),
                today.minusDays(8), today.plusDays(3),
                70, 1, "AUTO", "IN_PROGRESS", "#0ea5e9",
                true, 2, "HR", false, null, gm
        );

        seedTask(
                hrProject.getId(), "1.2", "Policy Digitization", "ACTIVITY",
                today.plusDays(5), today.plusDays(20),
                today.plusDays(8), today.plusDays(24),
                null, null,
                0, 1, "AUTO", "PLANNED", "#0ea5e9",
                true, 3, "HR", false, null, gm
        );

        seedDependency(hrProject.getId(), "1.1", "1.2", "FS", 0);

        // ---------------------------------------------------------------------
        // FINANCE
        // ---------------------------------------------------------------------
        seedFinance(ercopac, ferrero, "1.0", "Project Management", 1,
                new BigDecimal("250000"), new BigDecimal("220000"), new BigDecimal("140000"),
                new BigDecimal("110000"), new BigDecimal("245000"));

        seedFinance(ercopac, ferrero, "3.0", "Design", 1,
                new BigDecimal("180000"), new BigDecimal("155000"), new BigDecimal("89000"),
                new BigDecimal("72000"), new BigDecimal("171000"));

        seedFinance(ercopac, ferrero, "4.0", "Procurement", 1,
                new BigDecimal("220000"), new BigDecimal("210000"), new BigDecimal("98000"),
                new BigDecimal("93000"), new BigDecimal("216000"));

        seedFinance(ercopac, ferrero, "5.0", "Manufacturing", 1,
                new BigDecimal("260000"), new BigDecimal("245000"), new BigDecimal("75000"),
                new BigDecimal("61000"), new BigDecimal("252000"));

        seedFinance(ercopac, ferrero, "7.0", "Site Installation", 1,
                new BigDecimal("160000"), new BigDecimal("145000"), new BigDecimal("30000"),
                new BigDecimal("18000"), new BigDecimal("152000"));

        seedFinance(ercopac, digitalFactory, "1.0", "Digital Factory Rollout", 1,
                new BigDecimal("420000"), new BigDecimal("390000"), new BigDecimal("145000"),
                new BigDecimal("121000"), new BigDecimal("402000"));

        seedFinance(ercopac, hrProject, "1.0", "HR Digitalization", 1,
                new BigDecimal("110000"), new BigDecimal("98000"), new BigDecimal("42000"),
                new BigDecimal("28000"), new BigDecimal("103000"));

        // ---------------------------------------------------------------------
        // FORECAST
        // ---------------------------------------------------------------------
        seedForecast(ercopac, ferrero, "3.0", period(today.minusMonths(1)), new BigDecimal("35000"));
        seedForecast(ercopac, ferrero, "3.0", period(today), new BigDecimal("52000"));
        seedForecast(ercopac, ferrero, "4.0", period(today), new BigDecimal("78000"));
        seedForecast(ercopac, ferrero, "4.0", period(today.plusMonths(1)), new BigDecimal("92000"));
        seedForecast(ercopac, ferrero, "5.0", period(today.plusMonths(1)), new BigDecimal("110000"));
        seedForecast(ercopac, ferrero, "5.0", period(today.plusMonths(2)), new BigDecimal("98000"));
        seedForecast(ercopac, ferrero, "7.0", period(today.plusMonths(2)), new BigDecimal("73000"));
        seedForecast(ercopac, ferrero, "7.0", period(today.plusMonths(3)), new BigDecimal("52000"));

        seedForecast(ercopac, digitalFactory, "1.0", period(today), new BigDecimal("65000"));
        seedForecast(ercopac, digitalFactory, "1.0", period(today.plusMonths(1)), new BigDecimal("80000"));

        seedForecast(ercopac, hrProject, "1.0", period(today), new BigDecimal("22000"));
        seedForecast(ercopac, hrProject, "1.0", period(today.plusMonths(1)), new BigDecimal("36000"));

        // ---------------------------------------------------------------------
        // RISKS
        // ---------------------------------------------------------------------
        seedRisk(
                ercopac, ferrero,
                "risk", "managing",
                "Long lead components may arrive later than baseline.",
                today.minusDays(7), today.plusDays(18),
                "Expedite supplier follow-up and identify alternative source.",
                "PRC", "Meriem Procurement Manager", "4.1",
                4, 4, "open", null, null,
                "Weekly supplier escalation in place."
        );

        seedRisk(
                ercopac, ferrero,
                "risk", "new",
                "Customer utility readiness could delay test campaign.",
                today.minusDays(3), today.plusDays(12),
                "Run readiness checklist and close open utility actions before FAT.",
                "PM", "Sonia PM Lead", "2.0",
                5, 3, "open", null, null,
                "Impacts test window and commissioning sequence."
        );

        seedRisk(
                ercopac, ferrero,
                "opportunity", "managing",
                "Earlier internal testing may compress site installation duration.",
                today.minusDays(2), today.plusDays(25),
                "Advance software validation in factory before shipment.",
                "SW", "Omar Software Engineer", "5.3",
                3, 4, "approved", "Hamza General Manager", today.minusDays(1),
                "Can improve overall on-time delivery probability."
        );

        seedRisk(
                ercopac, digitalFactory,
                "risk", "managing",
                "ERP integration scope may increase after workshop outputs.",
                today.minusDays(6), today.plusDays(16),
                "Freeze integration scope and validate interfaces early.",
                "SW", "Youssef Software Manager", "1.1",
                4, 3, "open", null, null,
                "Potential impact on schedule and budget."
        );

        // ---------------------------------------------------------------------
        // ACTIONS
        // ---------------------------------------------------------------------
        seedAction(
                ercopac, ferrero,
                "Finalize utility readiness checklist",
                "Complete power and network prerequisites before ready-to-test milestone.",
                "action", "PM", "high", "doing",
                false, today.minusDays(2), today.plusDays(5)
        );

        seedAction(
                ercopac, ferrero,
                "Follow up long lead supplier",
                "Daily follow-up on high criticality purchase orders.",
                "issue", "PRC", "high", "blocked",
                false, today.minusDays(1), today.plusDays(6)
        );

        seedAction(
                ercopac, ferrero,
                "Prepare FAT protocol",
                "Build FAT checklist, sign-off sheet, and defect capture flow.",
                "action", "PM", "medium", "todo",
                true, today.plusDays(12), today.plusDays(30)
        );

        seedAction(
                ercopac, digitalFactory,
                "Validate MES API mapping",
                "Confirm message contracts between MES and ERP.",
                "action", "SW", "high", "review",
                false, today.minusDays(4), today.plusDays(3)
        );

        // ---------------------------------------------------------------------
        // HOLIDAYS
        // ---------------------------------------------------------------------
        seedHoliday(ercopac.getId(), me2, today.plusDays(4), today.plusDays(6), "Annual leave", dmMe.getId());
        seedHoliday(ercopac.getId(), ce2, today.plusDays(18), today.plusDays(19), "Training", dmCe.getId());
        seedHoliday(ercopac.getId(), sw2, today.plusDays(10), today.plusDays(12), "Medical leave", dmSw.getId());
        seedHoliday(ercopac.getId(), prc2, today.plusDays(7), today.plusDays(8), "Supplier visit", dmPrc.getId());
        seedHoliday(ercopac.getId(), mfc1, today.plusDays(22), today.plusDays(24), "Site support", dmMfc.getId());
    }

    // =====================================================================
    // ORGANISATION HELPERS
    // =====================================================================

    private Organisation createOrUpdateOrganisation(String name, String code) {
        Organisation organisation = organisationRepo.findByCode(code).orElseGet(Organisation::new);
        organisation.setName(name);
        organisation.setCode(code);
        organisation.setActive(true);
        return organisationRepo.save(organisation);
    }

    // =====================================================================
    // USER HELPERS
    // =====================================================================

    private AppUser createOrUpdatePlatformUser(String fullName,
                                               String email,
                                               String rawPassword,
                                               Role role,
                                               String color) {
        AppUser user = userRepo.findByEmail(email).orElseGet(AppUser::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        user.setOrganisation(null);
        user.setDepartmentCode(null);
        user.setJobTitle(role.name());
        user.setResourceType("PLATFORM");
        user.setSeniority("EXECUTIVE");
        user.setHoursPerDay(8);
        user.setDaysPerWeek(5);
        user.setWorkdays("MON-FRI");
        user.setColor(color);
        user.setInternalUser(true);
        user.setActive(true);
        return userRepo.save(user);
    }

    private AppUser createOrUpdateOrgUser(String fullName,
                                          String email,
                                          String rawPassword,
                                          Role role,
                                          Organisation organisation,
                                          String departmentCode,
                                          String jobTitle,
                                          String resourceType,
                                          String seniority,
                                          Integer hoursPerDay,
                                          Integer daysPerWeek,
                                          String workdays,
                                          String color,
                                          boolean internalUser) {
        AppUser user = userRepo.findByEmail(email).orElseGet(AppUser::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        user.setOrganisation(organisation);
        user.setDepartmentCode(departmentCode);
        user.setJobTitle(jobTitle);
        user.setResourceType(resourceType);
        user.setSeniority(seniority);
        user.setHoursPerDay(hoursPerDay);
        user.setDaysPerWeek(daysPerWeek);
        user.setWorkdays(workdays);
        user.setColor(color);
        user.setInternalUser(internalUser);
        user.setActive(true);
        return userRepo.save(user);
    }

    // =====================================================================
    // PROJECT HELPERS
    // =====================================================================

    private Project createOrUpdateProject(String code,
                                          String name,
                                          String shortName,
                                          Organisation organisation,
                                          Long managerId,
                                          String portfolio,
                                          String country,
                                          String projectType,
                                          String projectPhase,
                                          String priority,
                                          LocalDate plannedStart,
                                          LocalDate plannedEnd,
                                          BigDecimal projectBudget,
                                          BigDecimal totalProjectBudget) {

        Project project = projectRepo.findByCode(code).orElseGet(Project::new);
        project.setCode(code);
        project.setName(name);
        project.setShortName(shortName);
        project.setOrganisation(organisation);
        project.setProjectManagerId(managerId);
        project.setPortfolio(portfolio);
        project.setCountry(country);
        project.setProjectType(projectType);
        project.setProjectPhase(projectPhase);
        project.setPriority(priority);
        project.setPlannedStart(plannedStart);
        project.setPlannedEnd(plannedEnd);
        project.setProjectBudget(projectBudget);
        project.setTotalProjectBudget(totalProjectBudget);
        project.setOrgAssignment("General Management");
        project.setComment("Rich seeded project for dashboards, schedule, finance, forecast, risks, and actions.");
        return projectRepo.save(project);
    }

    private void createOrUpdatePlanning(Long projectId,
                                        LocalDate expectedStart,
                                        LocalDate expectedEnd,
                                        Integer probability,
                                        String keywords) {
        ProjectPlanning planning = planningRepo.findByProjectId(projectId).orElseGet(ProjectPlanning::new);
        planning.setProjectId(projectId);
        planning.setExpectedStart(expectedStart);
        planning.setExpectedEnd(expectedEnd);
        planning.setProbability(probability);
        planning.setProjectCalendar("Standard Calendar");
        planning.setKeywords(keywords);
        planning.setSubcontractors("N/A");
        planningRepo.save(planning);
    }

    // =====================================================================
    // TASK HELPERS
    // =====================================================================

    private void seedTask(Long projectId,
                          String wbsCode,
                          String name,
                          String taskType,
                          LocalDate baselineStart,
                          LocalDate baselineEnd,
                          LocalDate plannedStart,
                          LocalDate plannedEnd,
                          LocalDate actualStart,
                          LocalDate actualEnd,
                          Integer percentComplete,
                          Integer priority,
                          String scheduleMode,
                          String status,
                          String color,
                          Boolean active,
                          Integer displayOrder,
                          String departmentCode,
                          Boolean customerMilestone,
                          AppUser assignedUser,
                          AppUser fallbackAssignedUser) {

        ProjectTask task = findTask(projectId, wbsCode).orElseGet(ProjectTask::new);

        task.setProjectId(projectId);
        task.setWbsCode(wbsCode);
        task.setName(name);
        task.setDescription(name + " - seeded task");
        task.setTaskType(taskType);
        task.setBaselineStart(baselineStart);
        task.setBaselineEnd(baselineEnd);
        task.setPlannedStart(plannedStart);
        task.setPlannedEnd(plannedEnd);
        task.setActualStart(actualStart);
        task.setActualEnd(actualEnd);
        task.setDurationDays(calculateDuration(plannedStart, plannedEnd));
        task.setPercentComplete(percentComplete);
        task.setPriority(priority);
        task.setScheduleMode(scheduleMode);
        task.setStatus(status);
        task.setColor(color);
        task.setActive(active);
        task.setDisplayOrder(displayOrder);
        task.setDepartmentCode(departmentCode);
        task.setCustomerMilestone(customerMilestone);
        task.setAllocationPercent(100);
        task.setPlannedHours(BigDecimal.valueOf((long) (task.getDurationDays() == null ? 0 : task.getDurationDays()) * 8));
        task.setActualHours(BigDecimal.valueOf((long) Math.max(0, (percentComplete == null ? 0 : percentComplete)) * 2));

        if (assignedUser != null) {
            task.setAssignedUser(assignedUser);
        } else if (fallbackAssignedUser != null) {
            task.setAssignedUser(fallbackAssignedUser);
        }

        taskRepo.save(task);
    }

    private void seedDependency(Long projectId,
                                String predecessorWbs,
                                String successorWbs,
                                String dependencyType,
                                Integer lagDays) {

        Optional<ProjectTask> predecessorOpt = findTask(projectId, predecessorWbs);
        Optional<ProjectTask> successorOpt = findTask(projectId, successorWbs);

        if (predecessorOpt.isEmpty() || successorOpt.isEmpty()) {
            return;
        }

        Long predecessorId = predecessorOpt.get().getId();
        Long successorId = successorOpt.get().getId();

        boolean exists = dependencyRepo.findByProjectId(projectId).stream()
                .anyMatch(d ->
                        projectId.equals(d.getProjectId()) &&
                        predecessorId.equals(d.getPredecessorTaskId()) &&
                        successorId.equals(d.getSuccessorTaskId()) &&
                        Objects.equals(dependencyType, d.getDependencyType())
                );

        if (exists) {
            return;
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setProjectId(projectId);
        dependency.setPredecessorTaskId(predecessorId);
        dependency.setSuccessorTaskId(successorId);
        dependency.setDependencyType(dependencyType);
        dependency.setLagDays(lagDays);

        dependencyRepo.save(dependency);
    }

    private Optional<ProjectTask> findTask(Long projectId, String wbsCode) {
        return taskRepo.findAll()
                .stream()
                .filter(t -> projectId.equals(t.getProjectId()) && wbsCode.equalsIgnoreCase(t.getWbsCode()))
                .findFirst();
    }

    private Integer calculateDuration(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }

    private AppUser opsUserFallback(AppUser user) {
        return user;
    }

    // =====================================================================
    // FINANCE HELPERS
    // =====================================================================

    private void seedFinance(Organisation organisation,
                             Project project,
                             String wbsCode,
                             String description,
                             Integer level,
                             BigDecimal sales,
                             BigDecimal budget,
                             BigDecimal commitment,
                             BigDecimal actualCost,
                             BigDecimal forecast) {

        FinanceEntry entry = financeRepo.findAll()
                .stream()
                .filter(e -> e.getProject() != null
                        && e.getProject().getId().equals(project.getId())
                        && wbsCode.equalsIgnoreCase(e.getWbsCode()))
                .findFirst()
                .orElseGet(FinanceEntry::new);

        entry.setOrganisation(organisation);
        entry.setProject(project);
        entry.setWbsCode(wbsCode);
        entry.setDescription(description);
        entry.setLevel(level);
        entry.setSales(sales);
        entry.setBudget(budget);
        entry.setCommitment(commitment);
        entry.setActualCost(actualCost);
        entry.setForecast(forecast);

        financeRepo.save(entry);
    }

    // =====================================================================
    // FORECAST HELPERS
    // =====================================================================

    private void seedForecast(Organisation organisation,
                              Project project,
                              String wbsCode,
                              String periodKey,
                              BigDecimal amount) {

        ForecastEntry entry = forecastRepo.findAll()
                .stream()
                .filter(e -> e.getProject() != null
                        && e.getProject().getId().equals(project.getId())
                        && wbsCode.equalsIgnoreCase(e.getWbsCode())
                        && periodKey.equalsIgnoreCase(e.getPeriodKey()))
                .findFirst()
                .orElseGet(ForecastEntry::new);

        entry.setOrganisation(organisation);
        entry.setProject(project);
        entry.setWbsCode(wbsCode);
        entry.setPeriodKey(periodKey);
        entry.setAmount(amount);

        forecastRepo.save(entry);
    }

    // =====================================================================
    // RISK HELPERS
    // =====================================================================

    private void seedRisk(Organisation organisation,
                          Project project,
                          String riskType,
                          String state,
                          String description,
                          LocalDate inputDate,
                          LocalDate dueDate,
                          String mitigation,
                          String ownerDept,
                          String owner,
                          String wbsCode,
                          Integer impact,
                          Integer probability,
                          String varianceStatus,
                          String approvedBy,
                          LocalDate approvedAt,
                          String notes) {

        RiskItem item = riskRepo.findAll()
                .stream()
                .filter(r -> r.getProject() != null
                        && r.getProject().getId().equals(project.getId())
                        && description.equalsIgnoreCase(r.getDescription()))
                .findFirst()
                .orElseGet(RiskItem::new);

        item.setOrganisation(organisation);
        item.setProject(project);
        item.setRiskType(riskType);
        item.setState(state);
        item.setDescription(description);
        item.setInputDate(inputDate);
        item.setDueDate(dueDate);
        item.setMitigation(mitigation);
        item.setOwnerDept(ownerDept);
        item.setOwner(owner);
        item.setWbsCode(wbsCode);
        item.setImpact(impact);
        item.setProbability(probability);
        item.setVarianceStatus(varianceStatus);
        item.setApprovedBy(approvedBy);
        item.setApprovedAt(approvedAt);
        item.setNotes(notes);

        riskRepo.save(item);
    }

    // =====================================================================
    // ACTION HELPERS
    // =====================================================================

    private void seedAction(Organisation organisation,
                            Project project,
                            String title,
                            String description,
                            String actionType,
                            String departmentCode,
                            String priority,
                            String status,
                            Boolean customerVisible,
                            LocalDate insertedDate,
                            LocalDate dueDate) {

        ActionItem item = actionRepo.findAll()
                .stream()
                .filter(a -> a.getProject() != null
                        && a.getProject().getId().equals(project.getId())
                        && title.equalsIgnoreCase(a.getTitle()))
                .findFirst()
                .orElseGet(ActionItem::new);

        item.setOrganisation(organisation);
        item.setProject(project);
        item.setTitle(title);
        item.setDescription(description);
        item.setActionType(actionType);
        item.setDepartmentCode(departmentCode);
        item.setPriority(priority);
        item.setStatus(status);
        item.setCustomerVisible(customerVisible);
        item.setInsertedDate(insertedDate);
        item.setDueDate(dueDate);

        actionRepo.save(item);
    }

    // =====================================================================
    // HOLIDAY HELPERS
    // =====================================================================

    private void seedHoliday(Long organisationId,
                             AppUser member,
                             LocalDate fromDate,
                             LocalDate toDate,
                             String note,
                             Long createdBy) {

        boolean exists = holidayRepo.existsByOrganisationIdAndMember_IdAndFromDateAndToDate(
                organisationId,
                member.getId(),
                fromDate,
                toDate
        );

        if (exists) {
            return;
        }

        DepartmentHoliday holiday = new DepartmentHoliday();
        holiday.setOrganisationId(organisationId);
        holiday.setMember(member);
        holiday.setFromDate(fromDate);
        holiday.setToDate(toDate);
        holiday.setNote(note);
        holiday.setCreatedBy(createdBy);

        holidayRepo.save(holiday);
    }

    // =====================================================================
    // MISC
    // =====================================================================

    private String period(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }
}