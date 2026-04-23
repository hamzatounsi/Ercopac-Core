package com.ercopac.ercopac_tracker.planning.web;

import com.ercopac.ercopac_tracker.planning.dto.CreateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectCalendarDto;
import com.ercopac.ercopac_tracker.planning.dto.UpdateProjectCalendarRequest;
import com.ercopac.ercopac_tracker.planning.service.ProjectCalendarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/calendars")
public class ProjectCalendarController {

    private final ProjectCalendarService calendarService;

    public ProjectCalendarController(ProjectCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public List<ProjectCalendarDto> getCalendars(@PathVariable Long projectId) {
        return calendarService.getProjectCalendars(projectId);
    }

    @PostMapping
    public ProjectCalendarDto createCalendar(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectCalendarRequest request
    ) {
        return calendarService.createCalendar(projectId, request);
    }

    @PutMapping("/{calendarId}")
    public ProjectCalendarDto updateCalendar(
            @PathVariable Long projectId,
            @PathVariable Long calendarId,
            @Valid @RequestBody UpdateProjectCalendarRequest request
    ) {
        return calendarService.updateCalendar(projectId, calendarId, request);
    }

    @DeleteMapping("/{calendarId}")
    public void deleteCalendar(
            @PathVariable Long projectId,
            @PathVariable Long calendarId
    ) {
        calendarService.deleteCalendar(projectId, calendarId);
    }

    @PostMapping("/{calendarId}/make-default")
    public void makeDefault(
            @PathVariable Long projectId,
            @PathVariable Long calendarId
    ) {
        calendarService.makeDefault(projectId, calendarId);
    }
}