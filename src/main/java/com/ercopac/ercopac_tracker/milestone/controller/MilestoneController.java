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
    public ResponseEntity<List<MilestoneTypeDto>> getAllMilestoneTypes() {
        return ResponseEntity.ok(milestoneTypeService.getAllMilestoneTypes());
    }

    @PostMapping("/types")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<MilestoneTypeDto> createMilestoneType(@RequestBody MilestoneTypeDto dto) {
        return ResponseEntity.ok(milestoneTypeService.createMilestoneType(dto));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<MilestoneTypeDto> updateMilestoneType(@PathVariable Long id, @RequestBody MilestoneTypeDto dto) {
        return ResponseEntity.ok(milestoneTypeService.updateMilestoneType(id, dto));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<Void> deleteMilestoneType(@PathVariable Long id) {
        milestoneTypeService.deleteMilestoneType(id);
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

    @PostMapping("/projects/{projectId}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<ProjectMilestoneDto> createMilestone(@PathVariable Long projectId, @RequestBody ProjectMilestoneDto dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.ok(projectMilestoneService.createMilestone(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<ProjectMilestoneDto> updateMilestone(@PathVariable Long id, @RequestBody ProjectMilestoneDto dto) {
        return ResponseEntity.ok(projectMilestoneService.updateMilestone(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MILESTONES_WRITE)
    public ResponseEntity<Void> deleteMilestone(@PathVariable Long id) {
        projectMilestoneService.deleteMilestone(id);
        return ResponseEntity.noContent().build();
    }
}