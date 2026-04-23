package com.ercopac.ercopac_tracker.planning.web;

import com.ercopac.ercopac_tracker.planning.dto.CreateProjectBaselineRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectBaselineDto;
import com.ercopac.ercopac_tracker.planning.service.ProjectBaselineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/baselines")
public class ProjectBaselineController {

    private final ProjectBaselineService baselineService;

    public ProjectBaselineController(ProjectBaselineService baselineService) {
        this.baselineService = baselineService;
    }

    @GetMapping
    public List<ProjectBaselineDto> getBaselines(@PathVariable Long projectId) {
        System.out.println("GET BASELINES HIT for project " + projectId);
        return baselineService.getProjectBaselines(projectId);
    }

    @PostMapping
    public ProjectBaselineDto createBaseline(
        @PathVariable Long projectId,
        @Valid @RequestBody CreateProjectBaselineRequest request
    ) {
        return baselineService.createBaseline(projectId, request);
    }

    @DeleteMapping("/{baselineId}")
    public void deleteBaseline(
        @PathVariable Long projectId,
        @PathVariable Long baselineId
    ) {
        baselineService.deleteBaseline(projectId, baselineId);
    }
}