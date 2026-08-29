package com.ercopac.ercopac_tracker.crm.web;

import com.ercopac.ercopac_tracker.crm.dto.*;
import com.ercopac.ercopac_tracker.crm.service.CrmService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm/organisations/{orgId}")
public class CrmController {
    private static final String CRM_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).CRM)";
    private static final String CRM_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).CRM)";
    private static final String CRM_MANAGER = "@permissionChecker.canAccessCrmManagerView(authentication)";
    private final CrmService service;
    public CrmController(CrmService service) { this.service = service; }

    @GetMapping("/dashboard") @PreAuthorize(CRM_READ)
    public CrmDashboardDto dashboard(@PathVariable Long orgId) { return service.getDashboard(orgId); }
    @GetMapping("/reports") @PreAuthorize(CRM_READ)
    public CrmReportsDto reports(@PathVariable Long orgId) { return service.getReports(orgId); }
    @GetMapping("/analytics") @PreAuthorize(CRM_READ)
    public CrmAnalyticsDto analytics(@PathVariable Long orgId,
            @RequestParam(required = false) String opportunityType) {
        return service.getAnalytics(orgId, opportunityType);
    }
    @GetMapping("/users") @PreAuthorize(CRM_READ)
    public List<CrmUserDto> users(@PathVariable Long orgId) { return service.getCrmUsers(orgId); }
    @GetMapping("/notification-preferences") @PreAuthorize(CRM_READ)
    public CrmNotificationPreferenceDto notificationPreferences(@PathVariable Long orgId) { return service.getNotificationPreferences(orgId); }
    @PutMapping("/notification-preferences") @PreAuthorize(CRM_READ)
    public CrmNotificationPreferenceDto saveNotificationPreferences(@PathVariable Long orgId, @RequestBody CrmNotificationPreferenceDto dto) { return service.saveNotificationPreferences(orgId, dto); }
    @GetMapping("/opportunities/team-users") @PreAuthorize(CRM_READ)
    public List<CrmUserDto> opportunityTeamUsers(@PathVariable Long orgId) { return service.getOpportunityTeamUsers(orgId); }

    @GetMapping("/accounts") @PreAuthorize(CRM_READ)
    public List<CrmAccountDto> accounts(@PathVariable Long orgId, @RequestParam(required = false) String search) {
        return service.getAccounts(orgId, search);
    }
    @GetMapping("/accounts/{id}") @PreAuthorize(CRM_READ)
    public CrmAccountDto account(@PathVariable Long orgId, @PathVariable Long id) { return service.getAccount(orgId, id); }
    @PostMapping("/accounts") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.CREATED)
    public CrmAccountDto createAccount(@PathVariable Long orgId, @RequestBody CrmAccountDto dto) { return service.createAccount(orgId, dto); }
    @PutMapping("/accounts/{id}") @PreAuthorize(CRM_WRITE)
    public CrmAccountDto updateAccount(@PathVariable Long orgId, @PathVariable Long id, @RequestBody CrmAccountDto dto) { return service.updateAccount(orgId, id, dto); }
    @DeleteMapping("/accounts/{id}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long orgId, @PathVariable Long id) { service.deleteAccount(orgId, id); }

    @GetMapping("/stages") @PreAuthorize(CRM_READ)
    public List<CrmPipelineStageDto> stages(@PathVariable Long orgId) { return service.getStages(orgId); }
    @PostMapping("/stages") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.CREATED)
    public CrmPipelineStageDto createStage(@PathVariable Long orgId, @RequestBody CrmPipelineStageDto dto) { return service.createStage(orgId, dto); }
    @PutMapping("/stages/{id}") @PreAuthorize(CRM_WRITE)
    public CrmPipelineStageDto updateStage(@PathVariable Long orgId, @PathVariable Long id, @RequestBody CrmPipelineStageDto dto) { return service.updateStage(orgId, id, dto); }
    @DeleteMapping("/stages/{id}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStage(@PathVariable Long orgId, @PathVariable Long id) { service.deleteStage(orgId, id); }

    @GetMapping("/categories") @PreAuthorize(CRM_READ)
    public List<CrmSupplyCategoryDto> categories(@PathVariable Long orgId) { return service.getOrganisationCategories(orgId); }

    @GetMapping("/leads") @PreAuthorize(CRM_READ)
    public List<CrmLeadDto> leads(@PathVariable Long orgId, @RequestParam(required = false) String search,
                                  @RequestParam(required = false) String status, @RequestParam(required = false) Long accountId) {
        return service.getLeads(orgId, search, status, accountId);
    }
    @GetMapping("/leads/{id}") @PreAuthorize(CRM_READ)
    public CrmLeadDto lead(@PathVariable Long orgId, @PathVariable Long id) { return service.getLead(orgId, id); }
    @PostMapping("/leads") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.CREATED)
    public CrmLeadDto createLead(@PathVariable Long orgId, @RequestBody CrmLeadDto dto) { return service.createLead(orgId, dto); }
    @PutMapping("/leads/{id}") @PreAuthorize(CRM_WRITE)
    public CrmLeadDto updateLead(@PathVariable Long orgId, @PathVariable Long id, @RequestBody CrmLeadDto dto) { return service.updateLead(orgId, id, dto); }
    @DeleteMapping("/leads/{id}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLead(@PathVariable Long orgId, @PathVariable Long id) { service.deleteLead(orgId, id); }
    @PostMapping("/leads/{id}/convert") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto convert(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, Long> body) {
        return service.convertLead(orgId, id, body.get("stageId"));
    }

    @GetMapping("/opportunities") @PreAuthorize(CRM_READ)
    public List<CrmOpportunityDto> opportunities(@PathVariable Long orgId,
            @RequestParam(required = false) Long ownerId, @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long leadId, @RequestParam(required = false) Long stageId) {
        return service.getOpportunities(orgId, ownerId, accountId, leadId, stageId);
    }
    @GetMapping("/opportunities/{id}") @PreAuthorize(CRM_READ)
    public CrmOpportunityDto opportunity(@PathVariable Long orgId, @PathVariable Long id) { return service.getOpportunity(orgId, id); }
    @PostMapping("/opportunities") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.CREATED)
    public CrmOpportunityDto createOpportunity(@PathVariable Long orgId, @RequestBody CrmOpportunityDto dto) { return service.createOpportunity(orgId, dto); }
    @PutMapping("/opportunities/{id}") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto updateOpportunity(@PathVariable Long orgId, @PathVariable Long id, @RequestBody CrmOpportunityDto dto) { return service.updateOpportunity(orgId, id, dto); }
    @PutMapping("/opportunities/{id}/team") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto updateOpportunityTeam(@PathVariable Long orgId, @PathVariable Long id,
            @RequestBody CrmOpportunityTeamRequest request) { return service.updateOpportunityTeam(orgId, id, request.userIds()); }
    @PatchMapping("/opportunities/{id}/stage") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto changeStage(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, Long> body) {
        return service.changeStage(orgId, id, body.get("stageId"));
    }
    @DeleteMapping("/opportunities/{id}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOpportunity(@PathVariable Long orgId, @PathVariable Long id) { service.deleteOpportunity(orgId, id); }
    @PostMapping("/opportunities/{id}/won") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto won(@PathVariable Long orgId, @PathVariable Long id) { return service.markWon(orgId, id); }
    @PostMapping("/opportunities/{id}/lost") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityDto lost(@PathVariable Long orgId, @PathVariable Long id) { return service.markLost(orgId, id); }

    @GetMapping("/opportunities/{id}/notes") @PreAuthorize(CRM_READ)
    public List<CrmOpportunityNoteDto> notes(@PathVariable Long orgId, @PathVariable Long id) { return service.getNotes(orgId, id); }
    @PostMapping("/opportunities/{id}/notes") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.CREATED)
    public CrmOpportunityNoteDto addNote(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, String> body) { return service.addNote(orgId, id, body.get("content")); }
    @PutMapping("/opportunities/{id}/notes/{noteId}") @PreAuthorize(CRM_WRITE)
    public CrmOpportunityNoteDto updateNote(@PathVariable Long orgId, @PathVariable Long id, @PathVariable Long noteId, @RequestBody Map<String, String> body) { return service.updateNote(orgId, id, noteId, body.get("content")); }
    @DeleteMapping("/opportunities/{id}/notes/{noteId}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long orgId, @PathVariable Long id, @PathVariable Long noteId) { service.deleteNote(orgId, id, noteId); }

    @GetMapping("/opportunities/{id}/attachments") @PreAuthorize(CRM_READ)
    public List<CrmOpportunityAttachmentDto> attachments(@PathVariable Long orgId, @PathVariable Long id) { return service.getAttachments(orgId, id); }
    @PostMapping(value = "/opportunities/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize(CRM_WRITE)
    public CrmOpportunityAttachmentDto upload(@PathVariable Long orgId, @PathVariable Long id, @RequestPart("file") MultipartFile file) { return service.uploadAttachment(orgId, id, file); }
    @GetMapping("/opportunities/{id}/attachments/{attachmentId}") @PreAuthorize(CRM_READ)
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long orgId, @PathVariable Long id, @PathVariable Long attachmentId) {
        CrmService.AttachmentDownload download = service.downloadAttachment(orgId, id, attachmentId);
        ContentDisposition disposition = ContentDisposition.attachment().filename(download.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(download.resource());
    }
    @DeleteMapping("/opportunities/{id}/attachments/{attachmentId}") @PreAuthorize(CRM_WRITE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@PathVariable Long orgId, @PathVariable Long id, @PathVariable Long attachmentId) { service.deleteAttachment(orgId, id, attachmentId); }
    @GetMapping("/opportunities/{id}/history") @PreAuthorize(CRM_READ)
    public List<CrmOpportunityHistoryDto> history(@PathVariable Long orgId, @PathVariable Long id) { return service.getHistory(orgId, id); }
    @GetMapping("/opportunities/{id}/stage-history") @PreAuthorize(CRM_READ)
    public List<CrmOpportunityStageHistoryDto> stageHistory(@PathVariable Long orgId, @PathVariable Long id) { return service.getStageHistory(orgId, id); }

    @GetMapping("/manager") @PreAuthorize(CRM_MANAGER)
    public CrmManagerViewDto manager(@PathVariable Long orgId, @RequestParam(required = false) Integer year) {
        return service.getManagerView(orgId, year == null ? LocalDate.now().getYear() : year);
    }
    @PutMapping("/manager/targets/{userId}") @PreAuthorize(CRM_MANAGER)
    public CrmManagerViewDto.TeamMember target(@PathVariable Long orgId, @PathVariable Long userId,
            @RequestParam int year, @RequestBody Map<String, Object> body) {
        BigDecimal amount = body.get("amount") == null ? BigDecimal.ZERO : new BigDecimal(body.get("amount").toString());
        return service.saveTarget(orgId, userId, year, amount, String.valueOf(body.getOrDefault("currency", "EUR")));
    }
}
