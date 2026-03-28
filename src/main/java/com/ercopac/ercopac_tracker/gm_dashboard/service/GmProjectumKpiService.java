package com.ercopac.ercopac_tracker.gm_dashboard.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.PortfolioKpiDto;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectKpiDto;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GmProjectumKpiService {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public PortfolioKpiDto getPortfolioKpis() {
        List<Project> projects = projectRepository.findAll();

        long totalProjects = projects.size();

        long activeProjects = projects.stream()
                .filter(project -> project.getProjectPhase() != null)
                .filter(project ->
                        !"COMPLETED".equalsIgnoreCase(project.getProjectPhase()) &&
                        !"CLOSED".equalsIgnoreCase(project.getProjectPhase()))
                .count();

        long delayedProjects = projects.stream()
                .filter(project ->
                        project.getPlannedEnd() != null &&
                        project.getPlannedEnd().isBefore(LocalDate.now()) &&
                        (project.getProjectPhase() == null ||
                         !"COMPLETED".equalsIgnoreCase(project.getProjectPhase())))
                .count();

        int averageProgress = calculatePortfolioAverageProgress(projects);

        double totalBudget = projects.stream()
                .map(Project::getProjectBudget)
                .filter(budget -> budget != null)
                .mapToDouble(Number::doubleValue)
                .sum();

        int onTimeRate = totalProjects == 0
                ? 0
                : (int) Math.round(((double) (totalProjects - delayedProjects) / totalProjects) * 100.0);

        return new PortfolioKpiDto(
                totalProjects,
                activeProjects,
                delayedProjects,
                averageProgress,
                totalBudget,
                onTimeRate
        );
    }

    public ProjectKpiDto getProjectKpis(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        List<ProjectTask> tasks = projectTaskRepository.findByProjectId(projectId);

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(task -> task.getPercentComplete() != null && task.getPercentComplete() >= 100)
                .count();

        long delayedTasks = tasks.stream()
                .filter(task ->
                        task.getPlannedEnd() != null &&
                        task.getBaselineEnd() != null &&
                        task.getPlannedEnd().isAfter(task.getBaselineEnd()))
                .count();

        int averageTaskProgress = totalTasks == 0
                ? 0
                : (int) Math.round(
                        tasks.stream()
                                .map(ProjectTask::getPercentComplete)
                                .filter(value -> value != null)
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                );

        double projectBudget = project.getProjectBudget() != null
                ? project.getProjectBudget().doubleValue()
                : 0.0;

        long plannedDurationDays = 0;
        if (project.getPlannedStart() != null && project.getPlannedEnd() != null) {
            plannedDurationDays = ChronoUnit.DAYS.between(project.getPlannedStart(), project.getPlannedEnd());
        }

        return new ProjectKpiDto(
                totalTasks,
                completedTasks,
                delayedTasks,
                averageTaskProgress,
                projectBudget,
                plannedDurationDays
        );
    }

    private int calculatePortfolioAverageProgress(List<Project> projects) {
        if (projects.isEmpty()) {
            return 0;
        }

        double avg = projects.stream()
                .map(Project::getId)
                .mapToInt(projectId -> {
                    List<ProjectTask> tasks = projectTaskRepository.findByProjectId(projectId);
                    return (int) Math.round(
                            tasks.stream()
                                    .map(ProjectTask::getPercentComplete)
                                    .filter(value -> value != null)
                                    .mapToInt(Integer::intValue)
                                    .average()
                                    .orElse(0)
                    );
                })
                .average()
                .orElse(0);

        return (int) Math.round(avg);
    }
}