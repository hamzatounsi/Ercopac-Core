package com.ercopac.ercopac_tracker.crm.service;

// Path: src/main/java/com/ercopac/ercopac_tracker/crm/service/CrmService.java

import com.ercopac.ercopac_tracker.crm.domain.*;
import com.ercopac.ercopac_tracker.crm.dto.*;
import com.ercopac.ercopac_tracker.crm.repository.*;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmService {

    private final CrmPipelineStageRepository stageRepo;
    private final CrmLeadRepository leadRepo;
    private final CrmOpportunityRepository oppRepo;
    private final CrmActivityRepository activityRepo;
    private final OrganisationRepository orgRepo;
    private final UserRepository userRepo;

    public CrmService(CrmPipelineStageRepository stageRepo,
                      CrmLeadRepository leadRepo,
                      CrmOpportunityRepository oppRepo,
                      CrmActivityRepository activityRepo,
                      OrganisationRepository orgRepo,
                      UserRepository userRepo) {
        this.stageRepo   = stageRepo;
        this.leadRepo    = leadRepo;
        this.oppRepo     = oppRepo;
        this.activityRepo = activityRepo;
        this.orgRepo     = orgRepo;
        this.userRepo    = userRepo;
    }

    // ── Helpers ───────────────────────────────────────────────
    private Organisation getOrg(Long orgId) {
        return orgRepo.findById(orgId)
            .orElseThrow(() -> new IllegalArgumentException("Organisation not found: " + orgId));
    }

    private AppUser getUser(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    // ══════════════════════════════════════════════════════════
    // PIPELINE STAGES
    // ══════════════════════════════════════════════════════════

    public List<CrmPipelineStageDto> getStages(Long orgId) {
        seedDefaultStagesIfNone(orgId);
        return stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(orgId)
            .stream().map(this::toStageDto).collect(Collectors.toList());
    }

    public CrmPipelineStageDto createStage(Long orgId, CrmPipelineStageDto dto) {
        Organisation org = getOrg(orgId);
        CrmPipelineStage stage = new CrmPipelineStage();
        stage.setOrganisation(org);
        mapStageFromDto(stage, dto);
        return toStageDto(stageRepo.save(stage));
    }

    public CrmPipelineStageDto updateStage(Long orgId, Long stageId, CrmPipelineStageDto dto) {
        CrmPipelineStage stage = stageRepo.findById(stageId)
            .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));
        mapStageFromDto(stage, dto);
        return toStageDto(stageRepo.save(stage));
    }

    public void deleteStage(Long orgId, Long stageId) {
        stageRepo.deleteById(stageId);
    }

    private void seedDefaultStagesIfNone(Long orgId) {
        if (stageRepo.countByOrganisation_Id(orgId) > 0) return;
        Organisation org = getOrg(orgId);
        List<CrmPipelineStage> defaults = Arrays.asList(
            new CrmPipelineStage(org, "Make presentation",    "#94a3b8", 0, false, false),
            new CrmPipelineStage(org, "Proposal/Quote",       "#3b82f6", 1, false, false),
            new CrmPipelineStage(org, "Negotiation/Revision", "#f59e0b", 2, false, false),
            new CrmPipelineStage(org, "Closed won",           "#22c55e", 3, true,  false),
            new CrmPipelineStage(org, "Closed lost",          "#ef4444", 4, false, true)
        );
        stageRepo.saveAll(defaults);
    }

    private void mapStageFromDto(CrmPipelineStage s, CrmPipelineStageDto dto) {
        s.setName(dto.getName());
        s.setColor(dto.getColor() != null ? dto.getColor() : "#64748b");
        s.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        s.setWon(dto.isWon());
        s.setLost(dto.isLost());
    }

    private CrmPipelineStageDto toStageDto(CrmPipelineStage s) {
        CrmPipelineStageDto dto = new CrmPipelineStageDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setColor(s.getColor());
        dto.setDisplayOrder(s.getDisplayOrder());
        dto.setWon(s.isWon());
        dto.setLost(s.isLost());
        return dto;
    }

    // ══════════════════════════════════════════════════════════
    // LEADS
    // ══════════════════════════════════════════════════════════

    public List<CrmLeadDto> getLeads(Long orgId, String search, String status) {
        List<CrmLead> leads;
        if (search != null && !search.isBlank()) {
            leads = leadRepo.searchByOrgAndTerm(orgId, search);
        } else if (status != null && !status.isBlank()) {
            leads = leadRepo.findByOrganisation_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
                orgId, CrmLead.Status.valueOf(status));
        } else {
            leads = leadRepo.findByOrganisation_IdAndActiveTrueOrderByCreatedAtDesc(orgId);
        }
        return leads.stream().map(this::toLeadDto).collect(Collectors.toList());
    }

    public CrmLeadDto createLead(Long orgId, CrmLeadDto dto) {
        Organisation org = getOrg(orgId);
        CrmLead lead = new CrmLead();
        lead.setOrganisation(org);
        mapLeadFromDto(lead, dto);
        lead = leadRepo.save(lead);

        // Log activity
        logActivity(org, dto.getOwnerId() != null ? getUser(dto.getOwnerId()) : null,
            CrmActivity.ActivityType.LEAD_CREATED,
            "New lead created: " + lead.getFullName(), lead, null);

        return toLeadDto(lead);
    }

    public CrmLeadDto updateLead(Long orgId, Long leadId, CrmLeadDto dto) {
        CrmLead lead = leadRepo.findById(leadId)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
        mapLeadFromDto(lead, dto);
        return toLeadDto(leadRepo.save(lead));
    }

    public void deleteLead(Long orgId, Long leadId) {
        leadRepo.deleteById(leadId);
    }

    // Convert lead to opportunity
    public CrmOpportunityDto convertLead(Long orgId, Long leadId, Long stageId) {
        CrmLead lead = leadRepo.findById(leadId)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
        Organisation org = getOrg(orgId);

        // Mark lead as converted
        lead.setConverted(true);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setStatus(CrmLead.Status.CONVERTED);
        leadRepo.save(lead);

        // Create opportunity from lead
        CrmOpportunity opp = new CrmOpportunity();
        opp.setOrganisation(org);
        opp.setName(lead.getFullName() + (lead.getCompany() != null ? " — " + lead.getCompany() : ""));
        opp.setAccountName(lead.getCompany());
        opp.setOwner(lead.getOwner());
        opp.setLead(lead);

        if (stageId != null) {
            stageRepo.findById(stageId).ifPresent(opp::setStage);
        }

        opp = oppRepo.save(opp);

        // Log activity
        logActivity(org, lead.getOwner(),
            CrmActivity.ActivityType.LEAD_CONVERTED,
            "Lead converted to opportunity: " + opp.getName(), lead, opp);

        return toOppDto(opp);
    }

    private void mapLeadFromDto(CrmLead lead, CrmLeadDto dto) {
        lead.setFullName(dto.getFullName());
        lead.setCompany(dto.getCompany());
        lead.setEmail(dto.getEmail());
        lead.setPhone(dto.getPhone());
        if (dto.getSource() != null) lead.setSource(CrmLead.Source.valueOf(dto.getSource()));
        if (dto.getStatus() != null) lead.setStatus(CrmLead.Status.valueOf(dto.getStatus()));
        if (dto.getOwnerId() != null) lead.setOwner(getUser(dto.getOwnerId()));
        lead.setNotes(dto.getNotes());
        lead.setActive(dto.isActive());
    }

    private CrmLeadDto toLeadDto(CrmLead l) {
        CrmLeadDto dto = new CrmLeadDto();
        dto.setId(l.getId());
        dto.setFullName(l.getFullName());
        dto.setCompany(l.getCompany());
        dto.setEmail(l.getEmail());
        dto.setPhone(l.getPhone());
        dto.setSource(l.getSource().name());
        dto.setStatus(l.getStatus().name());
        if (l.getOwner() != null) {
            dto.setOwnerId(l.getOwner().getId());
            dto.setOwnerName(l.getOwner().getFullName());
        }
        dto.setConverted(l.isConverted());
        dto.setConvertedAt(l.getConvertedAt());
        dto.setNotes(l.getNotes());
        dto.setActive(l.isActive());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }

    // ══════════════════════════════════════════════════════════
    // OPPORTUNITIES
    // ══════════════════════════════════════════════════════════

    public List<CrmOpportunityDto> getOpportunities(Long orgId, Long ownerId) {
        List<CrmOpportunity> list = ownerId != null
            ? oppRepo.findByOrganisation_IdAndOwner_IdOrderByCreatedAtDesc(orgId, ownerId)
            : oppRepo.findByOrganisation_IdOrderByCreatedAtDesc(orgId);
        return list.stream().map(this::toOppDto).collect(Collectors.toList());
    }

    public CrmOpportunityDto createOpportunity(Long orgId, CrmOpportunityDto dto) {
        Organisation org = getOrg(orgId);
        CrmOpportunity opp = new CrmOpportunity();
        opp.setOrganisation(org);
        mapOppFromDto(opp, dto);
        opp = oppRepo.save(opp);

        logActivity(org, opp.getOwner(),
            CrmActivity.ActivityType.OPPORTUNITY_CREATED,
            "New opportunity created: " + opp.getName(), null, opp);

        return toOppDto(opp);
    }

    public CrmOpportunityDto updateOpportunity(Long orgId, Long oppId, CrmOpportunityDto dto) {
        CrmOpportunity opp = oppRepo.findById(oppId)
            .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + oppId));
        String oldStageName = opp.getStage() != null ? opp.getStage().getName() : "—";
        mapOppFromDto(opp, dto);
        opp = oppRepo.save(opp);

        // Log stage change if changed
        String newStageName = opp.getStage() != null ? opp.getStage().getName() : "—";
        if (!oldStageName.equals(newStageName)) {
            logActivity(opp.getOrganisation(), opp.getOwner(),
                CrmActivity.ActivityType.STAGE_UPDATED,
                "Stage updated to " + newStageName, null, opp);
        }

        return toOppDto(opp);
    }

    public CrmOpportunityDto markWon(Long orgId, Long oppId) {
        CrmOpportunity opp = oppRepo.findById(oppId)
            .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + oppId));
        opp.setWon(true);
        opp.setLost(false);
        // Set to won stage if exists
        stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(orgId)
            .stream().filter(CrmPipelineStage::isWon).findFirst()
            .ifPresent(opp::setStage);
        opp = oppRepo.save(opp);

        logActivity(opp.getOrganisation(), opp.getOwner(),
            CrmActivity.ActivityType.DEAL_WON,
            "Deal won: " + opp.getName(), null, opp);

        return toOppDto(opp);
    }

    public CrmOpportunityDto markLost(Long orgId, Long oppId) {
        CrmOpportunity opp = oppRepo.findById(oppId)
            .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + oppId));
        opp.setLost(true);
        opp.setWon(false);
        stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(orgId)
            .stream().filter(CrmPipelineStage::isLost).findFirst()
            .ifPresent(opp::setStage);
        opp = oppRepo.save(opp);

        logActivity(opp.getOrganisation(), opp.getOwner(),
            CrmActivity.ActivityType.DEAL_LOST,
            "Deal lost: " + opp.getName(), null, opp);

        return toOppDto(opp);
    }

    public void deleteOpportunity(Long orgId, Long oppId) {
        oppRepo.deleteById(oppId);
    }

    private void mapOppFromDto(CrmOpportunity opp, CrmOpportunityDto dto) {
        opp.setName(dto.getName());
        opp.setAccountName(dto.getAccountName());
        opp.setValue(dto.getValue());
        opp.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "EUR");
        opp.setProbability(dto.getProbability() != null ? dto.getProbability() : 0);
        opp.setClosingDate(dto.getClosingDate());
        opp.setNotes(dto.getNotes());
        if (dto.getStageId() != null) {
            stageRepo.findById(dto.getStageId()).ifPresent(s -> {
                opp.setStage(s);
                opp.setWon(s.isWon());
                opp.setLost(s.isLost());
            });
        }
        if (dto.getOwnerId() != null) opp.setOwner(getUser(dto.getOwnerId()));
        if (dto.getLeadId() != null) leadRepo.findById(dto.getLeadId()).ifPresent(opp::setLead);
    }

    private CrmOpportunityDto toOppDto(CrmOpportunity o) {
        CrmOpportunityDto dto = new CrmOpportunityDto();
        dto.setId(o.getId());
        dto.setName(o.getName());
        dto.setAccountName(o.getAccountName());
        dto.setValue(o.getValue());
        dto.setCurrency(o.getCurrency());
        dto.setProbability(o.getProbability());
        dto.setClosingDate(o.getClosingDate());
        dto.setWon(o.isWon());
        dto.setLost(o.isLost());
        dto.setNotes(o.getNotes());
        dto.setCreatedAt(o.getCreatedAt());
        if (o.getStage() != null) {
            dto.setStageId(o.getStage().getId());
            dto.setStageName(o.getStage().getName());
            dto.setStageColor(o.getStage().getColor());
        }
        if (o.getOwner() != null) {
            dto.setOwnerId(o.getOwner().getId());
            dto.setOwnerName(o.getOwner().getFullName());
        }
        if (o.getLead() != null) dto.setLeadId(o.getLead().getId());
        return dto;
    }

    // ══════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════

    public CrmDashboardDto getDashboard(Long orgId) {
        seedDefaultStagesIfNone(orgId);
        CrmDashboardDto dash = new CrmDashboardDto();

        // KPIs
        dash.setOpenOpportunities(oppRepo.countByOrganisation_IdAndWonFalseAndLostFalse(orgId));
        dash.setPipelineValue(Optional.ofNullable(
            oppRepo.sumPipelineValue(orgId)).orElse(BigDecimal.ZERO));
        dash.setActiveLeads(leadRepo.countByOrganisation_IdAndActiveTrue(orgId));
        dash.setWonThisMonth(oppRepo.countWonSince(orgId,
            LocalDateTime.now().withDayOfMonth(1).withHour(0)));

        // Recent activity (last 10)
        dash.setRecentActivities(
            activityRepo.findByOrganisation_IdOrderByCreatedAtDesc(orgId, PageRequest.of(0, 10))
                .stream().map(this::toActivityDto).collect(Collectors.toList()));

        // Closing this month
        LocalDate now = LocalDate.now();
        dash.setClosingThisMonth(
            oppRepo.findByOrganisation_IdAndClosingDateBetweenOrderByClosingDateAsc(
                orgId, now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()))
                .stream().map(this::toOppDto).collect(Collectors.toList()));

        // Leads by source
        Map<String, Long> bySource = new LinkedHashMap<>();
        leadRepo.countBySource(orgId).forEach(row ->
            bySource.put(row[0].toString(), (Long) row[1]));
        dash.setLeadsBySource(bySource);

        // Pipeline stages with opp counts
        List<CrmPipelineStageDto> pipeline = stageRepo
            .findByOrganisation_IdOrderByDisplayOrderAsc(orgId)
            .stream().map(s -> {
                CrmPipelineStageDto sdto = toStageDto(s);
                long count = oppRepo.findByOrganisation_IdOrderByCreatedAtDesc(orgId)
                    .stream().filter(o -> o.getStage() != null &&
                        o.getStage().getId().equals(s.getId())).count();
                sdto.setOpportunityCount((int) count);
                return sdto;
            }).collect(Collectors.toList());
        dash.setPipeline(pipeline);

        return dash;
    }

    // ══════════════════════════════════════════════════════════
    // ACTIVITY HELPER
    // ══════════════════════════════════════════════════════════

    private void logActivity(Organisation org, AppUser user,
                              CrmActivity.ActivityType type, String description,
                              CrmLead lead, CrmOpportunity opp) {
        CrmActivity activity = CrmActivity.of(org, user, type, description);
        activity.setLead(lead);
        activity.setOpportunity(opp);
        activityRepo.save(activity);
    }

    private CrmActivityDto toActivityDto(CrmActivity a) {
        CrmActivityDto dto = new CrmActivityDto();
        dto.setId(a.getId());
        dto.setActivityType(a.getActivityType().name());
        dto.setDescription(a.getDescription());
        dto.setCreatedAt(a.getCreatedAt());
        if (a.getUser() != null) {
            dto.setUserId(a.getUser().getId());
            dto.setUserName(a.getUser().getFullName());
        }
        if (a.getLead() != null) dto.setLeadId(a.getLead().getId());
        if (a.getOpportunity() != null) dto.setOpportunityId(a.getOpportunity().getId());
        dto.setMetadata(a.getMetadata());
        return dto;
    }
}