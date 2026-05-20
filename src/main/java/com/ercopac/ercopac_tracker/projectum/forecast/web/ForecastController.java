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
        "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    private static final String FORECAST_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<List<ForecastRowDto>> getForecastGrid(@PathVariable Long projectId,
                                                                @RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getForecastGrid(projectId, periods));
    }

    @GetMapping("/summary")
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<ForecastSummaryDto> getForecastSummary(@PathVariable Long projectId,
                                                                 @RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getSummary(projectId, periods));
    }

    @GetMapping("/periods")
    @PreAuthorize(FORECAST_READ)
    public ResponseEntity<List<String>> getPeriods(@RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getPeriods(periods));
    }

    @PutMapping
    @PreAuthorize(FORECAST_WRITE)
    public ResponseEntity<Void> upsertForecast(@PathVariable Long projectId,
                                               @Valid @RequestBody UpsertForecastEntryRequest request) {
        forecastService.upsertForecast(projectId, request);
        return ResponseEntity.ok().build();
    }
}