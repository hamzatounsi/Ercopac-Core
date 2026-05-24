package com.ercopac.ercopac_tracker.projectum.risks.web;

import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskApprovalRuleDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.UpsertRiskApprovalRuleRequest;
import com.ercopac.ercopac_tracker.projectum.risks.service.RiskApprovalRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/risks/approval-rules")
public class RiskApprovalRuleController {

    private final RiskApprovalRuleService ruleService;

    public RiskApprovalRuleController(RiskApprovalRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER')")
    public ResponseEntity<List<RiskApprovalRuleDto>> getRules(@PathVariable Long projectId) {
        return ResponseEntity.ok(ruleService.getRules(projectId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER')")
    public ResponseEntity<RiskApprovalRuleDto> create(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertRiskApprovalRuleRequest request) {
        return ResponseEntity.ok(ruleService.create(projectId, request));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER')")
    public ResponseEntity<RiskApprovalRuleDto> update(
            @PathVariable Long projectId,
            @PathVariable Long ruleId,
            @Valid @RequestBody UpsertRiskApprovalRuleRequest request) {
        return ResponseEntity.ok(ruleService.update(ruleId, request));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long ruleId) {
        ruleService.delete(ruleId);
        return ResponseEntity.noContent().build();
    }
}