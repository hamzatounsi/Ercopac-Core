package com.ercopac.ercopac_tracker.crm.web;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/web/CrmController.java

import com.ercopac.ercopac_tracker.crm.dto.*;
import com.ercopac.ercopac_tracker.crm.service.CrmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/organisations/{orgId}")
@PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
public class CrmController {

    private final CrmService service;

    public CrmController(CrmService service) {
        this.service = service;
    }

    // ══════════════════════════════════════════════════════════
    // DASHBOARD
    // GET /api/crm/organisations/{orgId}/dashboard
    // ══════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    public CrmDashboardDto getDashboard(@PathVariable Long orgId) {
        return service.getDashboard(orgId);
    }

    // ══════════════════════════════════════════════════════════
    // PIPELINE STAGES
    // GET    /api/crm/organisations/{orgId}/stages
    // POST   /api/crm/organisations/{orgId}/stages
    // PUT    /api/crm/organisations/{orgId}/stages/{stageId}
    // DELETE /api/crm/organisations/{orgId}/stages/{stageId}
    // ══════════════════════════════════════════════════════════

    @GetMapping("/stages")
    public List<CrmPipelineStageDto> getStages(@PathVariable Long orgId) {
        return service.getStages(orgId);
    }

    @PostMapping("/stages")
    public CrmPipelineStageDto createStage(@PathVariable Long orgId,
                                            @RequestBody CrmPipelineStageDto dto) {
        return service.createStage(orgId, dto);
    }

    @PutMapping("/stages/{stageId}")
    public CrmPipelineStageDto updateStage(@PathVariable Long orgId,
                                            @PathVariable Long stageId,
                                            @RequestBody CrmPipelineStageDto dto) {
        return service.updateStage(orgId, stageId, dto);
    }

    @DeleteMapping("/stages/{stageId}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long orgId,
                                             @PathVariable Long stageId) {
        service.deleteStage(orgId, stageId);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════
    // LEADS
    // GET    /api/crm/organisations/{orgId}/leads?search=&status=
    // POST   /api/crm/organisations/{orgId}/leads
    // PUT    /api/crm/organisations/{orgId}/leads/{leadId}
    // DELETE /api/crm/organisations/{orgId}/leads/{leadId}
    // POST   /api/crm/organisations/{orgId}/leads/{leadId}/convert
    // ══════════════════════════════════════════════════════════

    @GetMapping("/leads")
    public List<CrmLeadDto> getLeads(@PathVariable Long orgId,
                                      @RequestParam(required = false) String search,
                                      @RequestParam(required = false) String status) {
        return service.getLeads(orgId, search, status);
    }

    @PostMapping("/leads")
    public CrmLeadDto createLead(@PathVariable Long orgId,
                                  @RequestBody CrmLeadDto dto) {
        return service.createLead(orgId, dto);
    }

    @PutMapping("/leads/{leadId}")
    public CrmLeadDto updateLead(@PathVariable Long orgId,
                                  @PathVariable Long leadId,
                                  @RequestBody CrmLeadDto dto) {
        return service.updateLead(orgId, leadId, dto);
    }

    @DeleteMapping("/leads/{leadId}")
    public ResponseEntity<Void> deleteLead(@PathVariable Long orgId,
                                            @PathVariable Long leadId) {
        service.deleteLead(orgId, leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/leads/{leadId}/convert")
    public CrmOpportunityDto convertLead(@PathVariable Long orgId,
                                          @PathVariable Long leadId,
                                          @RequestBody Map<String, Long> body) {
        Long stageId = body.get("stageId");
        return service.convertLead(orgId, leadId, stageId);
    }

    // ══════════════════════════════════════════════════════════
    // OPPORTUNITIES
    // GET    /api/crm/organisations/{orgId}/opportunities?ownerId=
    // POST   /api/crm/organisations/{orgId}/opportunities
    // PUT    /api/crm/organisations/{orgId}/opportunities/{oppId}
    // DELETE /api/crm/organisations/{orgId}/opportunities/{oppId}
    // POST   /api/crm/organisations/{orgId}/opportunities/{oppId}/won
    // POST   /api/crm/organisations/{orgId}/opportunities/{oppId}/lost
    // ══════════════════════════════════════════════════════════

    @GetMapping("/opportunities")
    public List<CrmOpportunityDto> getOpportunities(@PathVariable Long orgId,
                                                     @RequestParam(required = false) Long ownerId) {
        return service.getOpportunities(orgId, ownerId);
    }

    @PostMapping("/opportunities")
    public CrmOpportunityDto createOpportunity(@PathVariable Long orgId,
                                                @RequestBody CrmOpportunityDto dto) {
        return service.createOpportunity(orgId, dto);
    }

    @PutMapping("/opportunities/{oppId}")
    public CrmOpportunityDto updateOpportunity(@PathVariable Long orgId,
                                                @PathVariable Long oppId,
                                                @RequestBody CrmOpportunityDto dto) {
        return service.updateOpportunity(orgId, oppId, dto);
    }

    @DeleteMapping("/opportunities/{oppId}")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable Long orgId,
                                                   @PathVariable Long oppId) {
        service.deleteOpportunity(orgId, oppId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/opportunities/{oppId}/won")
    public CrmOpportunityDto markWon(@PathVariable Long orgId,
                                      @PathVariable Long oppId) {
        return service.markWon(orgId, oppId);
    }

    @PostMapping("/opportunities/{oppId}/lost")
    public CrmOpportunityDto markLost(@PathVariable Long orgId,
                                       @PathVariable Long oppId) {
        return service.markLost(orgId, oppId);
    }
}