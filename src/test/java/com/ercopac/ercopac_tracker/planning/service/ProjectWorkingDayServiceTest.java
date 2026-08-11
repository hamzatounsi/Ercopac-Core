package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import com.ercopac.ercopac_tracker.planning.repository.ProjectCalendarRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectWorkingDayServiceTest {

    private final ProjectCalendarRepository calendars = mock(ProjectCalendarRepository.class);
    private final ProjectWorkingDayService service = new ProjectWorkingDayService(calendars);

    @Test
    void standardCalendarExcludesWeekends() {
        when(calendars.findByProjectIdAndOrganisationIdAndIsDefaultTrue(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThat(service.addWorkingDays(1L, 1L, LocalDate.of(2026, 9, 4), 1))
                .isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(service.workingDuration(1L, 1L, LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 11)))
                .isEqualTo(5);
    }

    @Test
    void customFourDayCalendarSkipsFridayThroughSunday() {
        ProjectCalendar calendar = new ProjectCalendar();
        calendar.setWorkingDays("1,2,3,4");
        when(calendars.findByProjectIdAndOrganisationIdAndIsDefaultTrue(anyLong(), anyLong()))
                .thenReturn(Optional.of(calendar));

        assertThat(service.addWorkingDays(1L, 1L, LocalDate.of(2026, 9, 3), 1))
                .isEqualTo(LocalDate.of(2026, 9, 7));
    }
}
