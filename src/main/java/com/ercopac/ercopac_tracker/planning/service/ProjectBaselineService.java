package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectBaseline;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectBaselineRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectBaselineDto;
import com.ercopac.ercopac_tracker.planning.repository.ProjectBaselineRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectBaselineService {

    private final ProjectBaselineRepository baselineRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    public ProjectBaselineService(
        ProjectBaselineRepository baselineRepository,
        ProjectRepository projectRepository,
        ProjectTaskRepository taskRepository,
        SecurityUtils securityUtils,
        ObjectMapper objectMapper
    ) {
        this.baselineRepository = baselineRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectBaselineDto> getProjectBaselines(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        return baselineRepository
            .findByProjectIdAndOrganisationIdOrderByCreatedAtDesc(projectId, organisationId)
            .stream()
            .map(baseline -> toDto(baseline, getProject(projectId, organisationId).getActiveBaselineId()))
            .toList();
    }

    @Transactional
    public ProjectBaselineDto createBaseline(Long projectId, CreateProjectBaselineRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        Project project = getProject(projectId, organisationId);
        List<ProjectTask> tasks = taskRepository.findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(projectId, organisationId);

        ProjectBaseline baseline = new ProjectBaseline();
        baseline.setOrganisationId(organisationId);
        baseline.setProjectId(projectId);
        String requestedName = request.getName() == null ? "" : request.getName().trim();
        baseline.setName(requestedName.isEmpty()
                ? "Baseline " + (baselineRepository.countByProjectIdAndOrganisationId(projectId, organisationId) + 1)
                : requestedName);
        baseline.setSnapshotJson(serializeSnapshot(tasks));

        return toDto(baselineRepository.save(baseline), project.getActiveBaselineId());
    }

    @Transactional
    public void deleteBaseline(Long projectId, Long baselineId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        Project project = getProject(projectId, organisationId);

        ProjectBaseline baseline = baselineRepository
            .findByIdAndProjectIdAndOrganisationId(baselineId, projectId, organisationId)
            .orElseThrow(() -> new EntityNotFoundException("Baseline not found"));

        baselineRepository.delete(baseline);
        if (baselineId.equals(project.getActiveBaselineId())) {
            project.setActiveBaselineId(null);
            projectRepository.save(project);
        }
    }

    private ProjectBaselineDto toDto(ProjectBaseline baseline, Long activeBaselineId) {
        return new ProjectBaselineDto(
            baseline.getId(),
            baseline.getProjectId(),
            baseline.getName(),
            baseline.getCreatedAt(),
            baseline.getSnapshotJson(),
            baseline.getId().equals(activeBaselineId)
        );
    }
    
    public ProjectBaselineDto renameBaseline(
            Long projectId,
            Long baselineId,
            String name
    ) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectBaseline baseline = baselineRepository
                .findByIdAndProjectIdAndOrganisationId(baselineId, projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Baseline not found"));

        baseline.setName(name.trim());

        return toDto(baselineRepository.save(baseline), getProject(projectId, organisationId).getActiveBaselineId());
    }

    @Transactional
    public ProjectBaselineDto applyBaseline(Long projectId, Long baselineId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        Project project = getProject(projectId, organisationId);
        ProjectBaseline baseline = baselineRepository
                .findByIdAndProjectIdAndOrganisationId(baselineId, projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Baseline not found"));

        project.setActiveBaselineId(baseline.getId());
        projectRepository.save(project);
        return toDto(baseline, baseline.getId());
    }

    private Project getProject(Long projectId, Long organisationId) {
        return projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    private String serializeSnapshot(List<ProjectTask> tasks) {
        try {
            return objectMapper.writeValueAsString(new BaselineSnapshot(1, tasks.stream()
                    .map(this::snapshotTask)
                    .toList()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to create baseline snapshot", exception);
        }
    }

    private BaselineTaskSnapshot snapshotTask(ProjectTask task) {
        LocalDate start = firstNonNull(task.getActualStart(), task.getPlannedStart(), task.getBaselineStart());
        LocalDate end = firstNonNull(task.getActualEnd(), task.getPlannedEnd(), task.getBaselineEnd(), start);
        if ("MILESTONE".equalsIgnoreCase(task.getTaskType())) {
            LocalDate milestoneDate = firstNonNull(start, end);
            start = milestoneDate;
            end = milestoneDate;
        }
        return new BaselineTaskSnapshot(task.getId(), task.getTaskType(), start, end,
                "MILESTONE".equalsIgnoreCase(task.getTaskType()) ? 0 : task.getDurationDays());
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) if (value != null) return value;
        return null;
    }

    private record BaselineSnapshot(int version, List<BaselineTaskSnapshot> tasks) { }
    private record BaselineTaskSnapshot(Long taskId, String taskType, LocalDate start, LocalDate end, Integer durationDays) { }
}
