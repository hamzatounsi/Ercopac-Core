package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectTemplate;
import com.ercopac.ercopac_tracker.planning.dto.ApplyStandardTemplateResultDto;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectTemplateRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectTemplateDto;
import com.ercopac.ercopac_tracker.planning.repository.ProjectTemplateRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ProjectTemplateService {

    private static final Set<String> ALLOWED_SCOPES = Set.of("all", "selected");

    private final ProjectTemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final SecurityUtils securityUtils;

    public ProjectTemplateService(
            ProjectTemplateRepository templateRepository,
            ProjectRepository projectRepository,
            ProjectTaskRepository projectTaskRepository,
            TaskDependencyRepository taskDependencyRepository,
            SecurityUtils securityUtils
    ) {
        this.templateRepository = templateRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ProjectTemplateDto> getProjectTemplates(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        return templateRepository
                .findByProjectIdAndOrganisationIdOrderByCreatedAtDesc(projectId, organisationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProjectTemplateDto createTemplate(Long projectId, CreateProjectTemplateRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        String normalizedScope = normalizeScope(request.getScope());

        ProjectTemplate template = new ProjectTemplate();
        template.setOrganisationId(organisationId);
        template.setProjectId(projectId);
        template.setName(request.getName().trim());
        template.setScope(normalizedScope);
        template.setDescription(trimToNull(request.getDescription()));
        template.setSnapshotJson(request.getSnapshotJson());

        return toDto(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(Long projectId, Long templateId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectTemplate template = templateRepository
                .findByIdAndProjectIdAndOrganisationId(templateId, projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        templateRepository.delete(template);
    }

    @Transactional
    public ApplyStandardTemplateResultDto applyStandardTemplate(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (project.getOrganisation() == null ||
            !organisationId.equals(project.getOrganisation().getId())) {
            throw new IllegalArgumentException("Project does not belong to your organisation.");
        }

        // Version 1: replace current schedule of this exact project
        taskDependencyRepository.deleteByProjectId(projectId);
        projectTaskRepository.deleteByProjectId(projectId);

        LocalDate projectStart = project.getPlannedStart() != null
                ? project.getPlannedStart()
                : LocalDate.now();

        Map<String, Long> createdTaskIds = new LinkedHashMap<>();

        ProjectTask kickoff = new ProjectTask();
        kickoff.setProjectId(projectId);
        kickoff.setName("Kickoff");
        kickoff.setDescription("Project kickoff milestone");
        kickoff.setTaskType("MILESTONE");
        kickoff.setWbsCode("1");
        kickoff.setPlannedStart(projectStart);
        kickoff.setPlannedEnd(projectStart);
        kickoff.setBaselineStart(projectStart);
        kickoff.setBaselineEnd(projectStart);
        kickoff.setDurationDays(0);
        kickoff.setPercentComplete(0);
        kickoff.setAllocationPercent(100);
        kickoff.setPriority(100);
        kickoff.setScheduleMode("AUTO");
        kickoff.setActive(true);
        kickoff.setDisplayOrder(1);
        kickoff.setDepartmentCode("PM");
        kickoff.setResourceType("PM");
        kickoff = projectTaskRepository.save(kickoff);
        createdTaskIds.put("KICKOFF", kickoff.getId());

        ProjectTask engineering = new ProjectTask();
        engineering.setProjectId(projectId);
        engineering.setName("Engineering Study");
        engineering.setDescription("Initial engineering analysis");
        engineering.setTaskType("ACTIVITY");
        engineering.setWbsCode("2");
        engineering.setPlannedStart(projectStart);
        engineering.setPlannedEnd(projectStart.plusDays(4));
        engineering.setBaselineStart(projectStart);
        engineering.setBaselineEnd(projectStart.plusDays(4));
        engineering.setDurationDays(5);
        engineering.setPercentComplete(0);
        engineering.setAllocationPercent(100);
        engineering.setPriority(200);
        engineering.setScheduleMode("AUTO");
        engineering.setActive(true);
        engineering.setDisplayOrder(2);
        engineering.setDepartmentCode("ME");
        engineering.setResourceType("ME");
        engineering = projectTaskRepository.save(engineering);
        createdTaskIds.put("ENGINEERING", engineering.getId());

        ProjectTask procurement = new ProjectTask();
        procurement.setProjectId(projectId);
        procurement.setName("Procurement Preparation");
        procurement.setDescription("Preparation of procurement package");
        procurement.setTaskType("ACTIVITY");
        procurement.setWbsCode("3");
        procurement.setPlannedStart(projectStart.plusDays(4));
        procurement.setPlannedEnd(projectStart.plusDays(8));
        procurement.setBaselineStart(projectStart.plusDays(4));
        procurement.setBaselineEnd(projectStart.plusDays(8));
        procurement.setDurationDays(5);
        procurement.setPercentComplete(0);
        procurement.setAllocationPercent(100);
        procurement.setPriority(300);
        procurement.setScheduleMode("AUTO");
        procurement.setActive(true);
        procurement.setDisplayOrder(3);
        procurement.setDepartmentCode("PRC");
        procurement.setResourceType("PRC");
        procurement = projectTaskRepository.save(procurement);
        createdTaskIds.put("PROCUREMENT", procurement.getId());

        ProjectTask delivery = new ProjectTask();
        delivery.setProjectId(projectId);
        delivery.setName("Delivery Review");
        delivery.setDescription("Final review milestone");
        delivery.setTaskType("MILESTONE");
        delivery.setWbsCode("4");
        delivery.setPlannedStart(projectStart.plusDays(8));
        delivery.setPlannedEnd(projectStart.plusDays(8));
        delivery.setBaselineStart(projectStart.plusDays(8));
        delivery.setBaselineEnd(projectStart.plusDays(8));
        delivery.setDurationDays(0);
        delivery.setPercentComplete(0);
        delivery.setAllocationPercent(100);
        delivery.setPriority(400);
        delivery.setScheduleMode("AUTO");
        delivery.setActive(true);
        delivery.setDisplayOrder(4);
        delivery.setDepartmentCode("PM");
        delivery.setResourceType("PM");
        delivery = projectTaskRepository.save(delivery);
        createdTaskIds.put("DELIVERY", delivery.getId());

        int dependencyCount = 0;
        dependencyCount += saveDependency(projectId, createdTaskIds.get("KICKOFF"), createdTaskIds.get("ENGINEERING"), "FS", 0);
        dependencyCount += saveDependency(projectId, createdTaskIds.get("ENGINEERING"), createdTaskIds.get("PROCUREMENT"), "FS", 0);
        dependencyCount += saveDependency(projectId, createdTaskIds.get("PROCUREMENT"), createdTaskIds.get("DELIVERY"), "FS", 0);

        return new ApplyStandardTemplateResultDto()
                .setProjectId(projectId)
                .setTemplateName("Standard Template V1")
                .setTasksCreated(createdTaskIds.size())
                .setDependenciesCreated(dependencyCount);
    }

    private int saveDependency(Long projectId, Long predecessorTaskId, Long successorTaskId, String dependencyType, int lagDays) {
        TaskDependency dependency = new TaskDependency();
        dependency.setProjectId(projectId);
        dependency.setPredecessorTaskId(predecessorTaskId);
        dependency.setSuccessorTaskId(successorTaskId);
        dependency.setDependencyType(dependencyType);
        dependency.setLagDays(lagDays);
        taskDependencyRepository.save(dependency);
        return 1;
    }

    private ProjectTemplateDto toDto(ProjectTemplate template) {
        return new ProjectTemplateDto(
                template.getId(),
                template.getProjectId(),
                template.getName(),
                template.getScope(),
                template.getDescription(),
                template.getCreatedAt(),
                template.getSnapshotJson()
        );
    }

    private String normalizeScope(String scope) {
        String value = scope == null ? "" : scope.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_SCOPES.contains(value)) {
            throw new IllegalArgumentException("Invalid template scope. Allowed values: all, selected");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}