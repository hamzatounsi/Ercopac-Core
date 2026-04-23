package com.ercopac.ercopac_tracker.platform_dashboard.service;

import com.ercopac.ercopac_tracker.kpi.domain.HealthStatus;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.OrganisationSummaryDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformAlertDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformKpiDto;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformProjectRowDto;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlatformDashboardService {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public PlatformDashboardService(
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public PlatformKpiDto getPlatformKpis() {
        LocalDate today = LocalDate.now();
        List<Project> allProjects = projectRepository.findAll();

        long totalOrganisations = organisationRepository.count();
        long totalUsers = userRepository.count();
        long totalProjects = allProjects.size();

        long activeProjects = allProjects.stream()
                .filter(this::isActiveProject)
                .count();

        long completedProjects = allProjects.stream()
                .filter(this::isCompletedProject)
                .count();

        long delayedProjects = allProjects.stream()
                .filter(project -> isDelayedProject(project, today))
                .count();

        long criticalProjects = allProjects.stream()
                .filter(project -> computeHealth(project, today) == HealthStatus.RED)
                .count();

        return new PlatformKpiDto(
                totalOrganisations,
                totalUsers,
                totalProjects,
                activeProjects,
                completedProjects,
                delayedProjects,
                criticalProjects
        );
    }

    public List<OrganisationSummaryDto> getOrganisationSummaries() {
        LocalDate today = LocalDate.now();
        List<Organisation> organisations = organisationRepository.findAll();

        return organisations.stream()
                .map(org -> {
                    List<Project> organisationProjects = projectRepository.findAllByOrganisationId(org.getId());

                    long usersCount = userRepository.countByOrganisation_Id(org.getId());
                    long projectsCount = organisationProjects.size();

                    long activeProjects = organisationProjects.stream()
                            .filter(this::isActiveProject)
                            .count();

                    long delayedProjects = organisationProjects.stream()
                            .filter(project -> isDelayedProject(project, today))
                            .count();

                    long criticalProjects = organisationProjects.stream()
                            .filter(project -> computeHealth(project, today) == HealthStatus.RED)
                            .count();

                    String overallHealth = computeOrganisationHealth(organisationProjects, today).name();

                    return new OrganisationSummaryDto(
                            org.getId(),
                            org.getName(),
                            usersCount,
                            projectsCount,
                            activeProjects,
                            delayedProjects,
                            criticalProjects,
                            overallHealth
                    );
                })
                .sorted(Comparator.comparing(OrganisationSummaryDto::organisationName))
                .toList();
    }

    public List<PlatformProjectRowDto> getGlobalProjects() {
        LocalDate today = LocalDate.now();

        return projectRepository.findAll()
                .stream()
                .map(project -> new PlatformProjectRowDto(
                        project.getId(),
                        project.getCode(),
                        project.getName(),
                        project.getOrganisation() != null ? project.getOrganisation().getName() : "N/A",
                        project.getPlannedStart(),
                        project.getPlannedEnd(),
                        computeProgress(project, today),
                        computeStatus(project, today),
                        computeHealth(project, today).name()
                ))
                .sorted(Comparator.comparing(PlatformProjectRowDto::projectName))
                .toList();
    }

    public List<PlatformAlertDto> getPlatformAlerts() {
        LocalDate today = LocalDate.now();
        List<Project> projects = projectRepository.findAll();
        List<Organisation> organisations = organisationRepository.findAll();

        long delayedProjects = projects.stream()
                .filter(project -> isDelayedProject(project, today))
                .count();

        long criticalProjects = projects.stream()
                .filter(project -> computeHealth(project, today) == HealthStatus.RED)
                .count();

        long organisationsWithCriticalProjects = organisations.stream()
                .filter(org -> projectRepository.findAllByOrganisationId(org.getId())
                        .stream()
                        .anyMatch(project -> computeHealth(project, today) == HealthStatus.RED))
                .count();

        List<PlatformAlertDto> alerts = new ArrayList<>();

        if (delayedProjects > 0) {
            alerts.add(new PlatformAlertDto(
                    "SCHEDULE",
                    "HIGH",
                    delayedProjects + " delayed projects detected across the platform."
            ));
        }

        if (criticalProjects > 0) {
            alerts.add(new PlatformAlertDto(
                    "HEALTH",
                    "HIGH",
                    criticalProjects + " critical projects are currently in RED health."
            ));
        }

        if (organisationsWithCriticalProjects > 0) {
            alerts.add(new PlatformAlertDto(
                    "TENANT_RISK",
                    "MEDIUM",
                    organisationsWithCriticalProjects + " organisations have at least one critical project."
            ));
        }

        if (alerts.isEmpty()) {
            alerts.add(new PlatformAlertDto(
                    "INFO",
                    "LOW",
                    "No major cross-tenant risks detected."
            ));
        }

        return alerts;
    }

    private boolean isCompletedProject(Project project) {
        return "COMPLETED".equalsIgnoreCase(project.getProjectPhase());
    }

    private boolean isActiveProject(Project project) {
        String status = project.getProjectPhase();
        return status != null && (
                status.equalsIgnoreCase("IN_PROGRESS") ||
                status.equalsIgnoreCase("ACTIVE") ||
                status.equalsIgnoreCase("ONGOING")
        );
    }

    private boolean isDelayedProject(Project project, LocalDate today) {
        return project.getPlannedEnd() != null
                && project.getPlannedEnd().isBefore(today)
                && !isCompletedProject(project);
    }

    private String computeStatus(Project project, LocalDate today) {
        if (isCompletedProject(project)) {
            return "COMPLETED";
        }

        if (isDelayedProject(project, today)) {
            return "DELAYED";
        }

        if (isActiveProject(project)) {
            return "IN_PROGRESS";
        }

        return project.getProjectPhase() != null
                ? project.getProjectPhase().toUpperCase()
                : "PLANNED";
    }

    private Integer computeProgress(Project project, LocalDate today) {
        if (project.getPlannedStart() == null || project.getPlannedEnd() == null) {
            return 0;
        }

        if (today.isBefore(project.getPlannedStart())) {
            return 0;
        }

        if (!today.isBefore(project.getPlannedEnd())) {
            return isCompletedProject(project) ? 100 : 90;
        }

        long totalDays = ChronoUnit.DAYS.between(project.getPlannedStart(), project.getPlannedEnd());
        long elapsedDays = ChronoUnit.DAYS.between(project.getPlannedStart(), today);

        if (totalDays <= 0) {
            return 0;
        }

        int progress = (int) ((elapsedDays * 100) / totalDays);
        return Math.max(0, Math.min(progress, 100));
    }

    private HealthStatus computeHealth(Project project, LocalDate today) {
        if (isDelayedProject(project, today)) {
            return HealthStatus.RED;
        }

        if (project.getPlannedEnd() != null) {
            LocalDate warningDate = project.getPlannedEnd().minusDays(7);
            if ((today.isEqual(warningDate) || today.isAfter(warningDate)) && !isCompletedProject(project)) {
                return HealthStatus.YELLOW;
            }
        }

        return HealthStatus.GREEN;
    }

    private HealthStatus computeOrganisationHealth(List<Project> projects, LocalDate today) {
        if (projects.isEmpty()) {
            return HealthStatus.GREEN;
        }

        boolean hasRed = projects.stream()
                .anyMatch(project -> computeHealth(project, today) == HealthStatus.RED);
        if (hasRed) {
            return HealthStatus.RED;
        }

        boolean hasYellow = projects.stream()
                .anyMatch(project -> computeHealth(project, today) == HealthStatus.YELLOW);
        if (hasYellow) {
            return HealthStatus.YELLOW;
        }

        return HealthStatus.GREEN;
    }
}