package com.ercopac.ercopac_tracker.projectum.risks.web;

import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskItemDto;
import com.ercopac.ercopac_tracker.projectum.risks.service.RiskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskGlobalController {

    private final RiskService riskService;

    public RiskGlobalController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER','ROLE_PROJECT_MANAGER','PLATFORM_OWNER','ROLE_PLATFORM_OWNER')")
    public ResponseEntity<List<RiskItemDto>> getAllPendingApprovals() {
        return ResponseEntity.ok(riskService.getAllPendingApprovalsForOrg());
    }

    @GetMapping("/pending-approvals/count")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER','ROLE_PROJECT_MANAGER','PLATFORM_OWNER','ROLE_PLATFORM_OWNER')")
    public ResponseEntity<Long> getPendingApprovalsCount() {
        return ResponseEntity.ok((long) riskService.getAllPendingApprovalsForOrg().size());
    }
}
