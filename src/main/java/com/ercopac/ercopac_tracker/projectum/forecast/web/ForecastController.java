package com.ercopac.ercopac_tracker.projectum.forecast.web;

import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastRowDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastSummaryDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.UpsertForecastEntryRequest;
import com.ercopac.ercopac_tracker.projectum.forecast.service.ForecastService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    private static final String FORECAST_READ =
            "@permissionChecker.canRead(authentication, " +
            "T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    private static final String FORECAST_WRITE =
            "@permissionChecker.canWrite(authentication, " +
            "T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<List<ForecastRowDto>> getForecastGrid(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "12") int periods,
            @RequestParam(defaultValue = "month") String periodType) {

        return ResponseEntity.ok(
                forecastService.getForecastGrid(projectId, periods, periodType)
        );
    }

    @GetMapping("/summary")
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<ForecastSummaryDto> getForecastSummary(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "12") int periods) {

        return ResponseEntity.ok(
                forecastService.getSummary(projectId, periods)
        );
    }

    @GetMapping("/periods")
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<List<String>> getPeriods(
            @RequestParam(defaultValue = "12") int periods,
            @RequestParam(defaultValue = "month") String periodType) {

        return ResponseEntity.ok(
                forecastService.getPeriods(periods, periodType)
        );
    }

    /**
     * Create or update a forecast cell.
     */
    @PutMapping
    @PreAuthorize(FORECAST_WRITE)
    public ResponseEntity<Void> upsertForecast(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertForecastEntryRequest request) {

        forecastService.upsertForecast(projectId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Link a Finance row to a Schedule/WBS task.
     */
    @PatchMapping("/linked-wbs")
    @PreAuthorize(FORECAST_WRITE)
    public ResponseEntity<Void> updateLinkedScheduleWbs(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request) {

        Object financeEntryIdValue = request.get("financeEntryId");

        if (!(financeEntryIdValue instanceof Number)) {
            throw new IllegalArgumentException("financeEntryId is required");
        }

        Long financeEntryId = ((Number) financeEntryIdValue).longValue();

        String linkedScheduleWbs =
                request.get("linkedScheduleWbs") != null
                        ? request.get("linkedScheduleWbs").toString()
                        : null;

        forecastService.updateLinkedScheduleWbs(
                projectId,
                financeEntryId,
                linkedScheduleWbs
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Delete the forecast entry (all period values) of one WBS row.
     * The Finance WBS node is kept.
     */
    @DeleteMapping("/{financeEntryId}")
    @PreAuthorize(FORECAST_WRITE)
    public ResponseEntity<Void> deleteForecastEntry(
            @PathVariable Long projectId,
            @PathVariable Long financeEntryId) {
        forecastService.deleteForecastEntries(projectId, financeEntryId);
        return ResponseEntity.noContent().build();
    }
}
