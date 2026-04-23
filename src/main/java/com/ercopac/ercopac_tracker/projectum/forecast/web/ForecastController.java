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

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<ForecastRowDto>> getForecastGrid(@PathVariable Long projectId,
                                                                @RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getForecastGrid(projectId, periods));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<ForecastSummaryDto> getForecastSummary(@PathVariable Long projectId,
                                                                 @RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getSummary(projectId, periods));
    }

    @GetMapping("/periods")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<String>> getPeriods(@RequestParam(defaultValue = "12") int periods) {
        return ResponseEntity.ok(forecastService.getPeriods(periods));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<Void> upsertForecast(@PathVariable Long projectId,
                                               @Valid @RequestBody UpsertForecastEntryRequest request) {
        forecastService.upsertForecast(projectId, request);
        return ResponseEntity.ok().build();
    }
}