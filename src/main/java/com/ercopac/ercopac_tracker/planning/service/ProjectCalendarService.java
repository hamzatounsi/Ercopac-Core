package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectCalendarDto;
import com.ercopac.ercopac_tracker.planning.dto.UpdateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.repository.ProjectCalendarRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectCalendarService {

    private final ProjectCalendarRepository calendarRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final ProjectWorkingDayService workingDayService;
    private final SecurityUtils securityUtils;

    public ProjectCalendarService(
            ProjectCalendarRepository calendarRepository,
            ProjectRepository projectRepository,
            ProjectTaskRepository taskRepository,
            ProjectWorkingDayService workingDayService,
            SecurityUtils securityUtils
    ) {
        this.calendarRepository = calendarRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.workingDayService = workingDayService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public List<ProjectCalendarDto> getProjectCalendars(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        getProject(projectId, organisationId);
        ensureStandardCalendar(projectId, organisationId);

        return calendarRepository
                .findByProjectIdAndOrganisationIdOrderByIdDesc(projectId, organisationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProjectCalendarDto createCalendar(Long projectId, CreateProjectCalendarRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        getProject(projectId, organisationId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForProject(projectId, organisationId);
        }

        ProjectCalendar calendar = new ProjectCalendar();
        calendar.setOrganisationId(organisationId);
        calendar.setProjectId(projectId);
        calendar.setName(request.getName().trim());
        calendar.setWorkingDays(toWorkingDaysString(request.getWorkingDays()));
        calendar.setHoursPerDay(request.getHoursPerDay());
        calendar.setStartTime(request.getStartTime().trim());
        calendar.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) || Boolean.TRUE.equals(calendar.getIsDefault()));

        ProjectCalendar saved = calendarRepository.save(calendar);
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            recalculateScheduleDurations(projectId, organisationId);
        }
        return toDto(saved);
    }

    @Transactional
    public ProjectCalendarDto updateCalendar(Long projectId, Long calendarId, UpdateProjectCalendarRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        getProject(projectId, organisationId);

        ProjectCalendar calendar = calendarRepository.findByIdAndOrganisationId(calendarId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));

        if (!Objects.equals(calendar.getProjectId(), projectId)) {
            throw new EntityNotFoundException("Calendar not found for this project");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForProject(projectId, organisationId);
        }

        calendar.setName(request.getName().trim());
        calendar.setWorkingDays(toWorkingDaysString(request.getWorkingDays()));
        calendar.setHoursPerDay(request.getHoursPerDay());
        calendar.setStartTime(request.getStartTime().trim());
        calendar.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) || Boolean.TRUE.equals(calendar.getIsDefault()));

        ProjectCalendar saved = calendarRepository.save(calendar);
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            recalculateScheduleDurations(projectId, organisationId);
        }
        return toDto(saved);
    }

    @Transactional
    public void deleteCalendar(Long projectId, Long calendarId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectCalendar calendar = calendarRepository.findByIdAndOrganisationId(calendarId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));

        if (!Objects.equals(calendar.getProjectId(), projectId)) {
            throw new EntityNotFoundException("Calendar not found for this project");
        }

        if (Boolean.TRUE.equals(calendar.getIsDefault())) {
            throw new IllegalStateException("The active project calendar cannot be deleted. Apply another calendar first.");
        }

        calendarRepository.delete(calendar);
    }

    @Transactional
    public void makeDefault(Long projectId, Long calendarId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectCalendar calendar = calendarRepository.findByIdAndOrganisationId(calendarId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));

        if (!Objects.equals(calendar.getProjectId(), projectId)) {
            throw new EntityNotFoundException("Calendar not found for this project");
        }

        clearDefaultForProject(projectId, organisationId);
        calendar.setIsDefault(true);
        calendarRepository.save(calendar);
        recalculateScheduleDurations(projectId, organisationId);
    }

    private void clearDefaultForProject(Long projectId, Long organisationId) {
        calendarRepository.findByProjectIdAndOrganisationIdAndIsDefaultTrue(projectId, organisationId)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    calendarRepository.save(existing);
                });
    }

    private void ensureStandardCalendar(Long projectId, Long organisationId) {
        if (calendarRepository.findByProjectIdAndOrganisationIdAndIsDefaultTrue(projectId, organisationId).isPresent()) return;
        ProjectCalendar calendar = new ProjectCalendar();
        calendar.setOrganisationId(organisationId);
        calendar.setProjectId(projectId);
        calendar.setName("Standard 5-Day Calendar");
        calendar.setWorkingDays("1,2,3,4,5");
        calendar.setHoursPerDay(8);
        calendar.setStartTime("08:00");
        calendar.setIsDefault(true);
        calendarRepository.save(calendar);
    }

    private Project getProject(Long projectId, Long organisationId) {
        return projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    /** Calendar changes affect durations only; task dates and saved baselines remain untouched. */
    private void recalculateScheduleDurations(Long projectId, Long organisationId) {
        List<ProjectTask> tasks = taskRepository
                .findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(projectId, organisationId);
        for (ProjectTask task : tasks) {
            String type = task.getTaskType() == null ? "ACTIVITY" : task.getTaskType().toUpperCase();
            if ("MILESTONE".equals(type)) {
                task.setDurationDays(0);
            } else if (!"SUMMARY".equals(type)) {
                java.time.LocalDate start = task.getBaselineStart() != null ? task.getBaselineStart()
                        : task.getPlannedStart() != null ? task.getPlannedStart() : task.getActualStart();
                java.time.LocalDate end = task.getBaselineEnd() != null ? task.getBaselineEnd()
                        : task.getPlannedEnd() != null ? task.getPlannedEnd() : task.getActualEnd();
                if (start != null && end != null) {
                    task.setDurationDays(workingDayService.workingDuration(projectId, organisationId, start, end));
                }
            }
        }
        taskRepository.saveAll(tasks);
    }

    private ProjectCalendarDto toDto(ProjectCalendar calendar) {
        ProjectCalendarDto dto = new ProjectCalendarDto();
        dto.setId(calendar.getId());
        dto.setProjectId(calendar.getProjectId());
        dto.setName(calendar.getName());
        dto.setWorkingDays(toWorkingDaysList(calendar.getWorkingDays()));
        dto.setHoursPerDay(calendar.getHoursPerDay());
        dto.setStartTime(calendar.getStartTime());
        dto.setIsDefault(Boolean.TRUE.equals(calendar.getIsDefault()));
        return dto;
    }

    private String toWorkingDaysString(List<Integer> workingDays) {
        return workingDays.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Integer> toWorkingDaysList(String workingDays) {
        if (workingDays == null || workingDays.isBlank()) {
            return List.of();
        }

        return Arrays.stream(workingDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .toList();
    }
}
