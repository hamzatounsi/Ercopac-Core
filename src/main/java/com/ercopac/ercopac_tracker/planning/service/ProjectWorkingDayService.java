package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import com.ercopac.ercopac_tracker.planning.repository.ProjectCalendarRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared working-day calculations for the active project calendar. */
@Service
public class ProjectWorkingDayService {

    private static final Set<DayOfWeek> STANDARD_WORKING_DAYS = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final ProjectCalendarRepository calendarRepository;

    public ProjectWorkingDayService(ProjectCalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public LocalDate addWorkingDays(Long projectId, Long organisationId, LocalDate start, int daysAfterStart) {
        if (start == null || daysAfterStart == 0) return start;
        Set<DayOfWeek> workingDays = workingDays(projectId, organisationId);
        LocalDate cursor = start;
        int remaining = Math.abs(daysAfterStart);
        int direction = daysAfterStart < 0 ? -1 : 1;
        while (remaining > 0) {
            cursor = cursor.plusDays(direction);
            if (workingDays.contains(cursor.getDayOfWeek())) remaining--;
        }
        return cursor;
    }

    public int workingDuration(Long projectId, Long organisationId, LocalDate start, LocalDate end) {
        if (start == null || end == null) return 1;
        if (end.isBefore(start)) return 1;
        Set<DayOfWeek> workingDays = workingDays(projectId, organisationId);
        int duration = 0;
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            if (workingDays.contains(cursor.getDayOfWeek())) duration++;
        }
        return Math.max(1, duration);
    }

    private Set<DayOfWeek> workingDays(Long projectId, Long organisationId) {
        return calendarRepository.findByProjectIdAndOrganisationIdAndIsDefaultTrue(projectId, organisationId)
                .map(ProjectCalendar::getWorkingDays)
                .map(this::parseWorkingDays)
                .filter(days -> !days.isEmpty())
                .orElseGet(() -> createStandardCalendar(projectId, organisationId));
    }

    private Set<DayOfWeek> createStandardCalendar(Long projectId, Long organisationId) {
        ProjectCalendar calendar = new ProjectCalendar();
        calendar.setOrganisationId(organisationId);
        calendar.setProjectId(projectId);
        calendar.setName("Standard 5-Day Calendar");
        calendar.setWorkingDays("1,2,3,4,5");
        calendar.setHoursPerDay(8);
        calendar.setStartTime("08:00");
        calendar.setIsDefault(true);
        calendarRepository.save(calendar);
        return STANDARD_WORKING_DAYS;
    }

    private Set<DayOfWeek> parseWorkingDays(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(day -> !day.isEmpty())
                .map(Integer::parseInt)
                .filter(day -> day >= 1 && day <= 7)
                .map(DayOfWeek::of)
                .collect(Collectors.toSet());
    }
}
