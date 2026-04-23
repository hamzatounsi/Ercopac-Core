package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectCalendarDto;
import com.ercopac.ercopac_tracker.planning.dto.UpdateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.repository.ProjectCalendarRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    public ProjectCalendarService(
            ProjectCalendarRepository calendarRepository,
            SecurityUtils securityUtils
    ) {
        this.calendarRepository = calendarRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ProjectCalendarDto> getProjectCalendars(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        return calendarRepository
                .findByProjectIdAndOrganisationIdOrderByIdDesc(projectId, organisationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProjectCalendarDto createCalendar(Long projectId, CreateProjectCalendarRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

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
        calendar.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        return toDto(calendarRepository.save(calendar));
    }

    @Transactional
    public ProjectCalendarDto updateCalendar(Long projectId, Long calendarId, UpdateProjectCalendarRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

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
        calendar.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        return toDto(calendarRepository.save(calendar));
    }

    @Transactional
    public void deleteCalendar(Long projectId, Long calendarId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectCalendar calendar = calendarRepository.findByIdAndOrganisationId(calendarId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));

        if (!Objects.equals(calendar.getProjectId(), projectId)) {
            throw new EntityNotFoundException("Calendar not found for this project");
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
    }

    private void clearDefaultForProject(Long projectId, Long organisationId) {
        calendarRepository.findByProjectIdAndOrganisationIdAndIsDefaultTrue(projectId, organisationId)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    calendarRepository.save(existing);
                });
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