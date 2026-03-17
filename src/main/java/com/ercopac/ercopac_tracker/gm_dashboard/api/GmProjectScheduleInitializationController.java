package com.ercopac.ercopac_tracker.gm_dashboard.api;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.InitializeProjectScheduleRequest;
import com.ercopac.ercopac_tracker.gm_dashboard.dto.InitializedProjectResponse;
import com.ercopac.ercopac_tracker.gm_dashboard.service.GmProjectScheduleInitializationService;

@RestController
@RequestMapping("/api/gm/projects")
public class GmProjectScheduleInitializationController {

    private final GmProjectScheduleInitializationService initializationService;

    public GmProjectScheduleInitializationController(
            GmProjectScheduleInitializationService initializationService
    ) {
        this.initializationService = initializationService;
    }

    @PostMapping("/schedule-init")
    @PreAuthorize("hasRole('GENERAL_MANAGER')")
    public ResponseEntity<InitializedProjectResponse> initializeProjectSchedule(
            @Valid @RequestBody InitializeProjectScheduleRequest request
    ) {
        InitializedProjectResponse response = initializationService.initializeProject(request);
        return ResponseEntity.ok(response);
    }
}