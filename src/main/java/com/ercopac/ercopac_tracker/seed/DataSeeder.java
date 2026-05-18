package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (organisationRepository.count() > 0) {
            return;
        }

        // =========================================================
        // ORGANISATION
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
        org.setGeneralManagerLicenceLimit(10);
        org.setDepartmentManagerLicenceLimit(20);
        org.setEmployeeLicenceLimit(100);

        organisationRepository.save(org);

        // =========================================================
        // USERS
        // =========================================================

        AppUser platformOwner = seedUser(
                "hamza@projectum.com",
                "Hamza Tounsi",
                "Hamza123!",
                Role.PLATFORM_OWNER,
                null,
                "Executive"
        );

        AppUser orgAdmin = seedUser(
                "admin@pharmastore.com",
                "Organisation Admin",
                "Hamza123!",
                Role.ORG_ADMIN,
                org,
                "Administration"
        );

        AppUser gm = seedUser(
                "gm@pharmastore.com",
                "Michael Weber",
                "Hamza1",
                Role.GENERAL_MANAGER,
                org,
                "Management"
        );

        AppUser dm = seedUser(
                "dm@pharmastore.com",
                "Sarah Engineering",
                "Hamza123!",
                Role.DEPARTMENT_MANAGER,
                org,
                "Engineering"
        );

        AppUser employee1 = seedUser(
                "emp23!loyee1@pharmastore.com",
                "John Developer",
                "Hamza123!",
                Role.EMPLOYEE,
                org,
                "Engineering"
        );

        AppUser employee2 = seedUser(
                "employee2@pharmastore.com",
                "Emma Planner",
                "Hamza123!",
                Role.EMPLOYEE,
                org,
                "Planning"
        );

        // =========================================================
        // PROJECT 1
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
        // TASKS
        // =========================================================

        ProjectTask summary1 = createTask(
                project,
                org,
                "1",
                "Project Initiation",
                0,
                null,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(10),
                100,
                "COMPLETED",
                null
        );

        ProjectTask summary2 = createTask(
                project,
                org,
                "2",
                "System Design",
                0,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                65,
                "IN_PROGRESS",
                null
        );

        ProjectTask task21 = createTask(
                project,
                org,
                "2.1",
                "Architecture Definition",
                1,
                summary2,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                100,
                "COMPLETED",
                dm
        );

        ProjectTask task22 = createTask(
                project,
                org,
                "2.2",
                "Database Modelling",
                1,
                summary2,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(14),
                75,
                "IN_PROGRESS",
                employee1
        );

        ProjectTask task23 = createTask(
                project,
                org,
                "2.3",
                "API Integration",
                1,
                summary2,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(25),
                30,
                "IN_PROGRESS",
                employee2
        );

        ProjectTask summary3 = createTask(
                project,
                org,
                "3",
                "Testing & Deployment",
                0,
                null,
                LocalDate.now().plusDays(25),
                LocalDate.now().plusDays(60),
                0,
                "NOT_STARTED",
                null
        );

        createTask(
                project,
                org,
                "3.1",
                "QA Validation",
                1,
                summary3,
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(40),
                0,
                "NOT_STARTED",
                employee1
        );

        createTask(
                project,
                org,
                "3.2",
                "Production Deployment",
                1,
                summary3,
                LocalDate.now().plusDays(45),
                LocalDate.now().plusDays(55),
                0,
                "NOT_STARTED",
                gm
        );

        System.out.println("=======================================");
        System.out.println("PROJECTUM DEMO DATA SEEDED");
        System.out.println("=======================================");
        System.out.println("PLATFORM OWNER:");
        System.out.println("hamza@projectum.com / Hamza123!");
        System.out.println();
        System.out.println("ORG ADMIN:");
        System.out.println("admin@pharmastore.com / Hamza123!");
        System.out.println();
        System.out.println("GENERAL MANAGER:");
        System.out.println("gm@pharmastore.com / Hamza123!");
        System.out.println();
        System.out.println("DEPARTMENT MANAGER:");
        System.out.println("dm@pharmastore.com / Hamza123!");
        System.out.println();
        System.out.println("EMPLOYEES:");
        System.out.println("employee1@pharmastore.com / Hamza123!");
        System.out.println("employee2@pharmastore.com / Hamza123!");
        System.out.println("=======================================");
    }

    private AppUser seedUser(
            String email,
            String fullName,
            String rawPassword,
            Role role,
            Organisation organisation,
            String department
    ) {

        AppUser user = new AppUser();

        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        user.setRole(role);
        user.setActive(true);
        user.setInternalUser(true);

        user.setDepartmentCode(department);

        user.setJobTitle(role.name());
        user.setDefaultRate(new BigDecimal("120"));
        user.setHoursPerDay(8);

        user.setOrganisation(organisation);

        return userRepository.save(user);
    }

    private ProjectTask createTask(
            Project project,
            Organisation organisation,
            String wbs,
            String name,
            int outlineLevel,
            ProjectTask parent,
            LocalDate start,
            LocalDate end,
            int progress,
            String status,
            AppUser assignedUser
    ) {

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
}