package com.ercopac.ercopac_tracker.milestone.controller;

import com.ercopac.ercopac_tracker.milestone.dto.MilestoneTypeDto;
import com.ercopac.ercopac_tracker.milestone.dto.ProjectMilestoneDto;
import com.ercopac.ercopac_tracker.milestone.service.MilestoneTypeService;
import com.ercopac.ercopac_tracker.milestone.service.ProjectMilestoneService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private static final String MILESTONES_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private static final String MILESTONES_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).TASKS)";

    private final MilestoneTypeService milestoneTypeService;
    private final ProjectMilestoneService projectMilestoneService;

    public MilestoneController(MilestoneTypeService milestoneTypeService,
                               ProjectMilestoneService projectMilestoneService) {
        this.milestoneTypeService = milestoneTypeService;
        this.projectMilestoneService = projectMilestoneService;
    }

    // ========== MILESTONE TYPES ==========
    @GetMapping("/types")
    @PreAuthorize(MILESTONES_READ)
    public ResponseEntity<List<MilestoneTypeDto>> getAllMilestoneTypes(@RequestParam Long projectId) {
        return ResponseEntity.ok(milestoneTypeService.getMilestoneTypes(projectId));
    }

    @PostMapping("/projects/{projectId}/types")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<MilestoneTypeDto> createMilestoneType(@PathVariable Long projectId, @RequestBody MilestoneTypeDto dto) {
        return ResponseEntity.ok(milestoneTypeService.createMilestoneType(projectId, dto));
    }

    @PutMapping("/projects/{projectId}/types/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<MilestoneTypeDto> updateMilestoneType(@PathVariable Long projectId, @PathVariable Long id, @RequestBody MilestoneTypeDto dto) {
        return ResponseEntity.ok(milestoneTypeService.updateMilestoneType(projectId, id, dto));
    }

    @DeleteMapping("/projects/{projectId}/types/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<Void> deleteMilestoneType(@PathVariable Long projectId, @PathVariable Long id) {
        milestoneTypeService.deleteMilestoneType(projectId, id);
        return ResponseEntity.noContent().build();
    }

    // ========== PROJECT MILESTONES ==========
    @GetMapping("/projects/{projectId}")
    @PreAuthorize(MILESTONES_READ)
    public ResponseEntity<List<ProjectMilestoneDto>> getMilestonesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectMilestoneService.getMilestonesByProject(projectId));
    }

    @GetMapping("/calendar")
    @PreAuthorize(MILESTONES_READ)
    public ResponseEntity<List<ProjectMilestoneDto>> getMilestonesForCalendar(
            @RequestParam Long pmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(projectMilestoneService.getMilestonesForPMCalendar(pmId, startDate, endDate));
    }

    // ✅ NEW ENDPOINT FOR DASHBOARD
    @GetMapping("/range")
    @PreAuthorize(MILESTONES_READ)
    public ResponseEntity<List<ProjectMilestoneDto>> getMilestonesByDateRange(
            @RequestParam List<Long> projectIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(projectMilestoneService.getMilestonesByDateRange(projectIds, startDate, endDate));
    }

}
