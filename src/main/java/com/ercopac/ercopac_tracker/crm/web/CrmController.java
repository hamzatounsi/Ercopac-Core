package com.ercopac.ercopac_tracker.crm.web;

import com.ercopac.ercopac_tracker.crm.dto.*;
import com.ercopac.ercopac_tracker.crm.service.CrmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/organisations/{orgId}")
public class CrmController {

    private static final String CRM_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).CRM)";

    private static final String CRM_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).CRM)";

    private final CrmService service;

    public CrmController(CrmService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize(CRM_READ)
    public CrmDashboardDto getDashboard(@PathVariable Long orgId) {
        return service.getDashboard(orgId);
    }

    @GetMapping("/stages")
    @PreAuthorize(CRM_READ)
    public List<CrmPipelineStageDto> getStages(@PathVariable Long orgId) {
        return service.getStages(orgId);
    }

    @PostMapping("/stages")
    @PreAuthorize(CRM_WRITE)
    public CrmPipelineStageDto createStage(
            @PathVariable Long orgId,
            @RequestBody CrmPipelineStageDto dto
    ) {
        return service.createStage(orgId, dto);
    }

    @PutMapping("/stages/{stageId}")
    @PreAuthorize(CRM_WRITE)
    public CrmPipelineStageDto updateStage(
            @PathVariable Long orgId,
            @PathVariable Long stageId,
            @RequestBody CrmPipelineStageDto dto
    ) {
        return service.updateStage(orgId, stageId, dto);
    }

    @DeleteMapping("/stages/{stageId}")
    @PreAuthorize(CRM_WRITE)
    public ResponseEntity<Void> deleteStage(
            @PathVariable Long orgId,
            @PathVariable Long stageId
    ) {
        service.deleteStage(orgId, stageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/leads")
    @PreAuthorize(CRM_READ)
    public List<CrmLeadDto> getLeads(
            @PathVariable Long orgId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return service.getLeads(orgId, search, status);
    }

    @PostMapping("/leads")
    @PreAuthorize(CRM_WRITE)
    public CrmLeadDto createLead(
            @PathVariable Long orgId,
            @RequestBody CrmLeadDto dto
    ) {
        return service.createLead(orgId, dto);
    }

    @PutMapping("/leads/{leadId}")
    @PreAuthorize(CRM_WRITE)
    public CrmLeadDto updateLead(
            @PathVariable Long orgId,
            @PathVariable Long leadId,
            @RequestBody CrmLeadDto dto
    ) {
        return service.updateLead(orgId, leadId, dto);
    }

    @DeleteMapping("/leads/{leadId}")
    @PreAuthorize(CRM_WRITE)
    public ResponseEntity<Void> deleteLead(
            @PathVariable Long orgId,
            @PathVariable Long leadId
    ) {
        service.deleteLead(orgId, leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/leads/{leadId}/convert")
    @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto convertLead(
            @PathVariable Long orgId,
            @PathVariable Long leadId,
            @RequestBody Map<String, Long> body
    ) {
        Long stageId = body.get("stageId");
        return service.convertLead(orgId, leadId, stageId);
    }

    @GetMapping("/opportunities")
    @PreAuthorize(CRM_READ)
    public List<CrmOpportunityDto> getOpportunities(
            @PathVariable Long orgId,
            @RequestParam(required = false) Long ownerId
    ) {
        return service.getOpportunities(orgId, ownerId);
    }

    @PostMapping("/opportunities")
    @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto createOpportunity(
            @PathVariable Long orgId,
            @RequestBody CrmOpportunityDto dto
    ) {
        return service.createOpportunity(orgId, dto);
    }

    @PutMapping("/opportunities/{oppId}")
    @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto updateOpportunity(
            @PathVariable Long orgId,
            @PathVariable Long oppId,
            @RequestBody CrmOpportunityDto dto
    ) {
        return service.updateOpportunity(orgId, oppId, dto);
    }

    @DeleteMapping("/opportunities/{oppId}")
    @PreAuthorize(CRM_WRITE)
    public ResponseEntity<Void> deleteOpportunity(
            @PathVariable Long orgId,
            @PathVariable Long oppId
    ) {
        service.deleteOpportunity(orgId, oppId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/opportunities/{oppId}/won")
    @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto markWon(
            @PathVariable Long orgId,
            @PathVariable Long oppId
    ) {
        return service.markWon(orgId, oppId);
    }

    @PostMapping("/opportunities/{oppId}/lost")
    @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto markLost(
            @PathVariable Long orgId,
            @PathVariable Long oppId
    ) {
        return service.markLost(orgId, oppId);
    }
}