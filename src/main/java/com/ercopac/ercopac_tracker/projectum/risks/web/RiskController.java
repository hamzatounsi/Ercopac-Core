package com.ercopac.ercopac_tracker.projectum.risks.web;

import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskItemDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskSummaryDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.UpsertRiskItemRequest;
import com.ercopac.ercopac_tracker.projectum.risks.service.RiskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<RiskItemDto>> getProjectRisks(@PathVariable Long projectId) {
        return ResponseEntity.ok(riskService.getProjectRisks(projectId));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<RiskSummaryDto> getSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(riskService.getSummary(projectId));
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<RiskItemDto>> getPendingApprovals(@PathVariable Long projectId) {
        return ResponseEntity.ok(riskService.getPendingApprovals(projectId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<RiskItemDto> create(@PathVariable Long projectId,
                                              @Valid @RequestBody UpsertRiskItemRequest request) {
        return ResponseEntity.ok(riskService.create(projectId, request));
    }

    @PutMapping("/{riskId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<RiskItemDto> update(@PathVariable Long projectId,
                                              @PathVariable Long riskId,
                                              @Valid @RequestBody UpsertRiskItemRequest request) {
        return ResponseEntity.ok(riskService.update(projectId, riskId, request));
    }

    @PostMapping("/{riskId}/approve")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<RiskItemDto> approve(@PathVariable Long projectId,
                                               @PathVariable Long riskId) {
        return ResponseEntity.ok(riskService.approve(projectId, riskId));
    }

    @PostMapping("/{riskId}/reject")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<RiskItemDto> reject(@PathVariable Long projectId,
                                              @PathVariable Long riskId) {
        return ResponseEntity.ok(riskService.reject(projectId, riskId));
    }

    @DeleteMapping("/{riskId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long riskId) {
        riskService.delete(projectId, riskId);
        return ResponseEntity.noContent().build();
    }
}