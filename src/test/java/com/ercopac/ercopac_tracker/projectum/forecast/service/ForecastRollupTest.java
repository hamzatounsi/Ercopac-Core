package com.ercopac.ercopac_tracker.projectum.forecast.service;

import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastGridCellDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastRowDto;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ForecastRollupTest {
    private final ForecastService service = new ForecastService(null, null, null, null);

    private ForecastRowDto row(long id, Long parentId, int amount) {
        ForecastRowDto row = new ForecastRowDto();
        row.setFinanceEntryId(id);
        row.setParentId(parentId);
        row.setBudget(BigDecimal.ZERO);
        row.setActualCost(BigDecimal.ZERO);
        row.setRemainingHours(BigDecimal.ZERO);
        row.setRemainingCost(BigDecimal.ZERO);
        row.setTotalForecast(BigDecimal.valueOf(amount));
        row.setPeriods(List.of(new ForecastGridCellDto("2026-W44", BigDecimal.valueOf(amount))));
        return row;
    }

    @Test
    void rollsUpLeafUpdatesAndMovesWithoutCountingAncestorsTwice() {
        ForecastRowDto root = row(1, null, 0);
        ForecastRowDto mechanical = row(2, 1L, 0);
        ForecastRowDto electrical = row(3, 1L, 0);
        ForecastRowDto expense = row(4, 2L, 500);
        List<ForecastRowDto> rows = List.of(root, mechanical, electrical, expense);

        service.rollupRows(rows);
        assertEquals(500, root.getTotalForecast().intValueExact());
        assertEquals(500, mechanical.getTotalForecast().intValueExact());

        expense.setTotalForecast(BigDecimal.valueOf(800));
        expense.getPeriods().get(0).setAmount(BigDecimal.valueOf(800));
        service.rollupRows(rows);
        assertEquals(800, root.getTotalForecast().intValueExact());

        List<ForecastRowDto> movedRows = List.of(row(1, null, 0), row(2, 1L, 0), row(3, 1L, 0), row(4, 3L, 800));
        service.rollupRows(movedRows);
        assertEquals(0, movedRows.get(1).getTotalForecast().intValueExact());
        assertEquals(800, movedRows.get(2).getTotalForecast().intValueExact());
        assertEquals(800, movedRows.get(0).getTotalForecast().intValueExact());
    }

    @Test
    void remainingScheduleUsesOutstandingHoursEvenWhenTaskIsOverdue() {
        ProjectTask task = mock(ProjectTask.class);
        when(task.getId()).thenReturn(1L);
        when(task.getPlannedHours()).thenReturn(BigDecimal.valueOf(100));
        when(task.getActualHours()).thenReturn(BigDecimal.valueOf(40));
        assertEquals(60, service.calculateRemainingHoursFromToday(task, List.of(task)).intValueExact());
        ProjectTask unstarted = mock(ProjectTask.class);
        when(unstarted.getId()).thenReturn(2L);
        when(unstarted.getPlannedHours()).thenReturn(BigDecimal.valueOf(100));
        assertEquals(100, service.calculateRemainingHoursFromToday(unstarted, List.of(unstarted)).intValueExact());
    }
}
