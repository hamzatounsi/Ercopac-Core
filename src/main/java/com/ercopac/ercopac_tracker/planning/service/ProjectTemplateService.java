package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectTemplate;
import com.ercopac.ercopac_tracker.planning.dto.ApplyProjectTemplateResultDto;
import com.ercopac.ercopac_tracker.planning.dto.ApplyStandardTemplateResultDto;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectTemplateRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectTemplateTaskSnapshot;
import com.ercopac.ercopac_tracker.planning.dto.ProjectTemplateDto;
import com.ercopac.ercopac_tracker.planning.repository.ProjectTemplateRepository;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;

import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskService;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final UserRepository userRepository;
    private final ProjectWorkingDayService workingDayService;
    private final ProjectTaskService projectTaskService;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    public ProjectTemplateService(
            ProjectTemplateRepository templateRepository,
            ProjectRepository projectRepository,
            ProjectTaskRepository projectTaskRepository,
            TaskDependencyRepository taskDependencyRepository,
            TaskResourceAssignmentRepository taskResourceAssignmentRepository,
            DepartmentRepository departmentRepository,
            ResourceTypeRepository resourceTypeRepository,
            UserRepository userRepository,
            ProjectWorkingDayService workingDayService,
            ProjectTaskService projectTaskService,
            ObjectMapper objectMapper,
            SecurityUtils securityUtils
    ) {
        this.templateRepository = templateRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
        this.departmentRepository = departmentRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.userRepository = userRepository;
        this.workingDayService = workingDayService;
        this.projectTaskService = projectTaskService;
        this.objectMapper = objectMapper;
        this.securityUtils = securityUtils;
    }

    /**
     * ✅ FIX: templates are shared across the whole organisation, not
     * limited to the project where they were created/imported. projectId
     * is kept as a parameter for controller/frontend call compatibility
     * but is no longer used to filter the list.
     */
    @Transactional(readOnly = true)
    public List<ProjectTemplateDto> getProjectTemplates(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        return templateRepository
                .findByOrganisationIdOrderByCreatedAtDesc(organisationId)
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

    /**
     * ✅ FIX: an organisation-wide template can be deleted from any
     * project's settings, not only the one it was created in.
     */
    @Transactional
    public void deleteTemplate(Long projectId, Long templateId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectTemplate template = templateRepository
                .findByIdAndOrganisationId(templateId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        templateRepository.delete(template);
    }

    /**
     * Materializes a reusable template snapshot as real project tasks.  A full
     * template keeps the editor's existing replace behaviour; a selected
     * template appends once and is idempotent for retry/double-click safety.
     *
     * ✅ FIX: the template lookup is organisation-wide — it may have been
     * created/imported from a different project of the same organisation.
     * The generated tasks are still created in THIS project (projectId);
     * only the template lookup scope changed.
     */
    @Transactional
    public ApplyProjectTemplateResultDto applyTemplate(Long projectId, Long templateId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        ProjectTemplate template = templateRepository
                .findByIdAndOrganisationId(templateId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));
        Project project = projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        List<ProjectTemplateTaskSnapshot> snapshots = readSnapshot(template.getSnapshotJson());
        if (snapshots.isEmpty()) {
            throw new IllegalArgumentException("Template does not contain any tasks");
        }

        boolean append = "selected".equalsIgnoreCase(template.getScope());
        if (append && projectTaskRepository.existsByProjectIdAndSourceTemplateId(projectId, templateId)) {
            return new ApplyProjectTemplateResultDto()
                    .setTemplateId(templateId)
                    .setTemplateName(template.getName())
                    .setAlreadyApplied(true);
        }

        if (!append) {
            clearSchedule(projectId);
        }

        int displayOffset = append
                ? projectTaskRepository.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId).size()
                : 0;
        LocalDate defaultStart = project.getPlannedStart() != null ? project.getPlannedStart() : LocalDate.now();
        Map<Long, ProjectTask> createdBySnapshotId = new LinkedHashMap<>();
        List<ProjectTask> created = new ArrayList<>();

        for (int index = 0; index < snapshots.size(); index++) {
            ProjectTemplateTaskSnapshot snapshot = snapshots.get(index);
            ProjectTask task = materializeTask(project, templateId, snapshot, defaultStart, displayOffset + index + 1);
            task = projectTaskRepository.save(task);
            created.add(task);
            if (snapshot.id != null) {
                createdBySnapshotId.put(snapshot.id, task);
            }
        }

        for (int index = 0; index < snapshots.size(); index++) {
            Long parentSnapshotId = snapshots.get(index).parentId;
            ProjectTask parent = parentSnapshotId == null ? null : createdBySnapshotId.get(parentSnapshotId);
            created.get(index).setParentId(parent == null ? null : parent.getId());
        }
        projectTaskRepository.saveAll(created);

        int dependenciesCreated = saveTemplateDependencies(projectId, snapshots, createdBySnapshotId);
        projectTaskService.rollupSummariesPublic(projectId);

        return new ApplyProjectTemplateResultDto()
                .setTemplateId(templateId)
                .setTemplateName(template.getName())
                .setTasksCreated(created.size())
                .setDependenciesCreated(dependenciesCreated);
    }

    private List<ProjectTemplateTaskSnapshot> readSnapshot(String snapshotJson) {
        try {
            List<ProjectTemplateTaskSnapshot> snapshots = objectMapper.readValue(
                    snapshotJson,
                    new TypeReference<List<ProjectTemplateTaskSnapshot>>() {}
            );
            return snapshots == null ? List.of() : snapshots;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Template snapshot is invalid", exception);
        }
    }

    private void clearSchedule(Long projectId) {
        List<ProjectTask> existing = projectTaskRepository.findByProjectId(projectId);

        // ✅ FIX: casse les références parent_id AVANT suppression, sinon
        // Postgres rejette le DELETE avec une violation de contrainte FK
        // (fk67wf4s9c7219sj0ac7e0915mj) dès qu'une tâche a des enfants.
        for (ProjectTask task : existing) {
            if (task.getParentId() != null) {
                task.setParentId(null);
            }
        }
        projectTaskRepository.saveAll(existing);
        projectTaskRepository.flush();

        for (ProjectTask task : existing) {
            taskDependencyRepository.deleteByPredecessorTaskId(task.getId());
            taskDependencyRepository.deleteBySuccessorTaskId(task.getId());
            taskResourceAssignmentRepository.deleteByProjectIdAndTaskId(projectId, task.getId());
        }
        projectTaskRepository.deleteAll(existing);
        projectTaskRepository.flush();
    }

    private ProjectTask materializeTask(
            Project project,
            Long templateId,
            ProjectTemplateTaskSnapshot source,
            LocalDate defaultStart,
            int displayOrder
    ) {
        String taskType = normalizeTaskType(source.taskType);
        LocalDate start = firstDate(source.plannedStart, source.baselineStart, defaultStart);
        Integer duration = source.durationDays;
        if ("MILESTONE".equals(taskType)) {
            duration = 0;
        } else if (duration == null || duration < 1) {
            LocalDate knownEnd = firstDate(source.plannedEnd, source.baselineEnd, null);
            duration = knownEnd == null ? 1 : workingDayService.workingDuration(project.getId(), project.getOrganisation().getId(), start, knownEnd);
            duration = Math.max(1, duration);
        }
        LocalDate calculatedEnd = "MILESTONE".equals(taskType)
                ? start
                : workingDayService.addWorkingDays(project.getId(), project.getOrganisation().getId(), start, duration - 1);
        LocalDate plannedEnd = "MILESTONE".equals(taskType) ? start : firstDate(source.plannedEnd, calculatedEnd);
        LocalDate baselineStart = firstDate(source.baselineStart, start);
        LocalDate baselineEnd = "MILESTONE".equals(taskType) ? baselineStart : firstDate(source.baselineEnd, plannedEnd);

        ProjectTask task = new ProjectTask();
        task.setProjectId(project.getId());
        task.setOrganisationId(project.getOrganisation().getId());
        task.setSourceTemplateId(templateId);
        task.setName(hasText(source.name) ? source.name.trim() : "Untitled task");
        task.setDescription(source.description);
        task.setTaskType(taskType);
        task.setWbsCode(trimToNull(source.wbsCode));
        task.setOutlineLevel(source.outlineLevel != null ? source.outlineLevel : 1);
        task.setDisplayOrder(source.displayOrder != null ? displayOrder : displayOrder);
        task.setPlannedStart(start);
        task.setPlannedEnd(plannedEnd);
        task.setBaselineStart(baselineStart);
        task.setBaselineEnd(baselineEnd);
        task.setActualStart(source.actualStart);
        task.setActualEnd("MILESTONE".equals(taskType) ? source.actualStart : source.actualEnd);
        task.setDurationDays(duration);
        task.setPercentComplete(source.percentComplete != null ? source.percentComplete : 0);
        task.setAllocationPercent(source.allocationPercent != null ? source.allocationPercent : 100);
        task.setPlannedHours(source.plannedHours);
        task.setActualHours(source.actualHours);
        task.setPriority(source.priority != null ? source.priority : 500);
        task.setScheduleMode(hasText(source.scheduleMode) ? source.scheduleMode : "AUTO");
        task.setStatus(hasText(source.status) ? source.status : "NOT_STARTED");
        task.setColor(source.color);
        task.setActive(source.active == null || source.active);
        task.setCustomerMilestone(source.customerMilestone != null && source.customerMilestone);

        if ("ACTIVITY".equals(taskType)) {
            if (hasText(source.departmentCode)) {
                departmentRepository.findByCodeAndOrganisationId(source.departmentCode, project.getOrganisation().getId())
                        .ifPresent(task::setDepartment);
            }
            if (hasText(source.resourceType)) {
                resourceTypeRepository.findByCodeAndOrganisationId(source.resourceType, project.getOrganisation().getId())
                        .ifPresent(task::setResourceType);
            }
            if (source.assignedUserId != null) {
                userRepository.findByIdAndOrganisation_Id(source.assignedUserId, project.getOrganisation().getId())
                        .ifPresent(task::setAssignedUser);
            }
        }
        return task;
    }

    private int saveTemplateDependencies(
            Long projectId,
            List<ProjectTemplateTaskSnapshot> snapshots,
            Map<Long, ProjectTask> createdBySnapshotId
    ) {
        int count = 0;
        for (ProjectTemplateTaskSnapshot successorSnapshot : snapshots) {
            if (successorSnapshot.id == null || successorSnapshot.dependencies == null) continue;
            ProjectTask successor = createdBySnapshotId.get(successorSnapshot.id);
            if (successor == null) continue;
            for (ProjectTemplateTaskSnapshot.ProjectTemplateDependencySnapshot dependency : successorSnapshot.dependencies) {
                ProjectTask predecessor = dependency.predecessorTaskId == null
                        ? null : createdBySnapshotId.get(dependency.predecessorTaskId);
                if (predecessor == null || predecessor.getId().equals(successor.getId())) continue;
                TaskDependency saved = new TaskDependency();
                saved.setProjectId(projectId);
                saved.setPredecessorTaskId(predecessor.getId());
                saved.setSuccessorTaskId(successor.getId());
                saved.setDependencyType(hasText(dependency.dependencyType) ? dependency.dependencyType : "FS");
                saved.setLagDays(dependency.lagDays != null ? dependency.lagDays : 0);
                taskDependencyRepository.save(saved);
                count++;
            }
        }
        return count;
    }

    private String normalizeTaskType(String value) {
        String taskType = hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "ACTIVITY";
        if (!Set.of("ACTIVITY", "SUMMARY", "MILESTONE").contains(taskType)) {
            throw new IllegalArgumentException("Template contains an invalid task type");
        }
        return taskType;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDate firstDate(LocalDate... dates) {
        for (LocalDate date : dates) {
            if (date != null) return date;
        }
        return null;
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

        ProjectTask kickoff = buildTask(project);
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
        kickoff.setResourceTypeCode("PM");
        kickoff = projectTaskRepository.save(kickoff);
        createdTaskIds.put("KICKOFF", kickoff.getId());

        ProjectTask engineering = buildTask(project);
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
        engineering.setResourceTypeCode("ME");
        engineering = projectTaskRepository.save(engineering);
        createdTaskIds.put("ENGINEERING", engineering.getId());

        ProjectTask procurement = buildTask(project);
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
        procurement.setResourceTypeCode("PRC");
        procurement = projectTaskRepository.save(procurement);
        createdTaskIds.put("PROCUREMENT", procurement.getId());

        ProjectTask delivery = buildTask(project);
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
        delivery.setResourceTypeCode("PM");
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

    private ProjectTask buildTask(Project project) {

        if (project.getOrganisation() == null) {
            throw new IllegalArgumentException("Project has no organisation.");
        }

        ProjectTask task = new ProjectTask();

        task.setProjectId(project.getId());
        task.setOrganisationId(project.getOrganisation().getId());
        task.setActive(true);

        return task;
    }
}