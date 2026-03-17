package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final ProjectPlanningRepository projectPlanningRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo,
                      ProjectRepository projectRepo,
                      ProjectPlanningRepository projectPlanningRepo,
                      PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.projectPlanningRepo = projectPlanningRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createIfMissingUser("gm@ercopac.com", "Test1234", Role.GENERAL_MANAGER);
        createIfMissingUser("dm@ercopac.com", "Test1234", Role.DEPARTMENT_MANAGER);
        createIfMissingUser("emp@ercopac.com", "Test1234", Role.EMPLOYEE);

        Long gmId = userRepo.findByEmail("gm@ercopac.com")
                .map(AppUser::getId)
                .orElse(null);

        seedProjectIfMissing(
                "PRJ-001",
                "Packaging Line Upgrade",
                "PLU",
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(60),
                LocalDate.now().minusDays(25),
                LocalDate.now().plusDays(55),
                gmId,
                "Packaging",
                "Tunisia",
                "Industrial",
                "Execution",
                "P1",
                new BigDecimal("250000.00"),
                new BigDecimal("300000.00"),
                90
        );

        seedProjectIfMissing(
                "PRJ-002",
                "Factory Energy Optimization",
                "FEO",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(15),
                LocalDate.now().minusDays(8),
                LocalDate.now().plusDays(20),
                gmId,
                "Operations",
                "Tunisia",
                "Transformation",
                "Execution",
                "P2",
                new BigDecimal("120000.00"),
                new BigDecimal("150000.00"),
                80
        );

        seedProjectIfMissing(
                "PRJ-003",
                "New Storage Area Construction",
                "NSAC",
                LocalDate.now().minusDays(90),
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(85),
                LocalDate.now().minusDays(3),
                gmId,
                "Production",
                "Germany",
                "Infrastructure",
                "Closure",
                "P3",
                new BigDecimal("500000.00"),
                new BigDecimal("620000.00"),
                100
        );
    }

    private void createIfMissingUser(String email, String rawPassword, Role role) {
        userRepo.findByEmail(email).orElseGet(() -> {
            AppUser user = new AppUser(email, encoder.encode(rawPassword), role);
            return userRepo.save(user);
        });
    }

    private void seedProjectIfMissing(String code,
                                      String name,
                                      String shortName,
                                      LocalDate plannedStart,
                                      LocalDate plannedEnd,
                                      LocalDate expectedStart,
                                      LocalDate expectedEnd,
                                      Long managerId,
                                      String portfolio,
                                      String country,
                                      String projectType,
                                      String projectPhase,
                                      String priority,
                                      BigDecimal projectBudget,
                                      BigDecimal totalProjectBudget,
                                      Integer probability) {

        if (projectRepo.findByCode(code).isPresent()) {
            return;
        }

        Project project = new Project();
        project.setCode(code);
        project.setName(name);
        project.setShortName(shortName);
        project.setPortfolio(portfolio);
        project.setOrgAssignment("General Management");
        project.setCountry(country);
        project.setProjectType(projectType);
        project.setProjectPhase(projectPhase);
        project.setPriority(priority);
        project.setPlannedStart(plannedStart);
        project.setPlannedEnd(plannedEnd);
        project.setProjectBudget(projectBudget);
        project.setTotalProjectBudget(totalProjectBudget);
        project.setProjectManagerId(managerId);
        project.setComment("Demo seeded project for ERCOPAC monitoring platform.");

        Project savedProject = projectRepo.save(project);

        ProjectPlanning planning = new ProjectPlanning();
        planning.setProjectId(savedProject.getId());
        planning.setExpectedStart(expectedStart);
        planning.setExpectedEnd(expectedEnd);
        planning.setProjectCalendar("Standard Calendar");
        planning.setProbability(probability);
        planning.setKeywords("demo, seeded, project");
        planning.setSubcontractors("N/A");

        projectPlanningRepo.save(planning);
    }
}