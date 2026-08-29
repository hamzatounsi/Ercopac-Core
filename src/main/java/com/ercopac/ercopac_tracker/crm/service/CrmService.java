package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.*;
import com.ercopac.ercopac_tracker.crm.dto.*;
import com.ercopac.ercopac_tracker.crm.repository.*;
import com.ercopac.ercopac_tracker.admin.domain.ProjectCategory;
import com.ercopac.ercopac_tracker.admin.repository.ProjectCategoryRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmService {
    private static final Set<String> ATTACHMENT_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final List<Role> SALES_ROLES = List.of(Role.SALES_MANAGER_LEAD, Role.SALES_MANAGER);
    private static final List<Role> OPPORTUNITY_TEAM_ROLES = List.of(
            Role.SALES_MANAGER_LEAD, Role.SALES_MANAGER, Role.SYSTEM_ENGINEER);

    private final CrmPipelineStageRepository stageRepo;
    private final CrmLeadRepository leadRepo;
    private final CrmOpportunityRepository opportunityRepo;
    private final CrmActivityRepository activityRepo;
    private final CrmAccountRepository accountRepo;
    private final CrmIndustryRepository industryRepo;
    private final CrmSupplyCategoryRepository categoryRepo;
    private final ProjectCategoryRepository projectCategoryRepo;
    private final CrmOpportunityNoteRepository noteRepo;
    private final CrmOpportunityAttachmentRepository attachmentRepo;
    private final CrmOpportunityHistoryRepository historyRepo;
    private final CrmOpportunityStageHistoryRepository stageHistoryRepo;
    private final CrmSalesTargetRepository targetRepo;
    private final CrmNotificationPreferenceRepository notificationPreferenceRepo;
    private final OrganisationRepository organisationRepo;
    private final UserRepository userRepo;
    private final SecurityUtils security;
    private final Path attachmentRoot;

    @Autowired
    public CrmService(CrmPipelineStageRepository stageRepo,
                      CrmLeadRepository leadRepo,
                      CrmOpportunityRepository opportunityRepo,
                      CrmActivityRepository activityRepo,
                      CrmAccountRepository accountRepo,
                      CrmIndustryRepository industryRepo,
                      CrmSupplyCategoryRepository categoryRepo,
                      ProjectCategoryRepository projectCategoryRepo,
                      CrmOpportunityNoteRepository noteRepo,
                      CrmOpportunityAttachmentRepository attachmentRepo,
                      CrmOpportunityHistoryRepository historyRepo,
                      CrmOpportunityStageHistoryRepository stageHistoryRepo,
                      CrmSalesTargetRepository targetRepo,
                      CrmNotificationPreferenceRepository notificationPreferenceRepo,
                      OrganisationRepository organisationRepo,
                      UserRepository userRepo,
                      SecurityUtils security,
                      @Value("${crm.storage.path:uploads/crm}") String attachmentPath) {
        this.stageRepo = stageRepo;
        this.leadRepo = leadRepo;
        this.opportunityRepo = opportunityRepo;
        this.activityRepo = activityRepo;
        this.accountRepo = accountRepo;
        this.industryRepo = industryRepo;
        this.categoryRepo = categoryRepo;
        this.projectCategoryRepo = projectCategoryRepo;
        this.noteRepo = noteRepo;
        this.attachmentRepo = attachmentRepo;
        this.historyRepo = historyRepo;
        this.stageHistoryRepo = stageHistoryRepo;
        this.targetRepo = targetRepo;
        this.notificationPreferenceRepo = notificationPreferenceRepo;
        this.organisationRepo = organisationRepo;
        this.userRepo = userRepo;
        this.security = security;
        this.attachmentRoot = Paths.get(attachmentPath).toAbsolutePath().normalize();
    }

    /** Compatibility seam for existing focused service tests. */
    CrmService(CrmPipelineStageRepository stageRepo,
               CrmLeadRepository leadRepo,
               CrmOpportunityRepository opportunityRepo,
               CrmActivityRepository activityRepo,
               CrmAccountRepository accountRepo,
               CrmSupplyCategoryRepository categoryRepo,
               CrmOpportunityNoteRepository noteRepo,
               CrmOpportunityAttachmentRepository attachmentRepo,
               CrmOpportunityHistoryRepository historyRepo,
               CrmOpportunityStageHistoryRepository stageHistoryRepo,
               CrmSalesTargetRepository targetRepo,
               OrganisationRepository organisationRepo,
               UserRepository userRepo,
               SecurityUtils security,
               String attachmentPath) {
        this(stageRepo, leadRepo, opportunityRepo, activityRepo, accountRepo, null, categoryRepo, null,
                noteRepo, attachmentRepo, historyRepo, stageHistoryRepo, targetRepo, null,
                organisationRepo, userRepo, security, attachmentPath);
    }

    @Transactional(readOnly = true)
    public CrmNotificationPreferenceDto getNotificationPreferences(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        AppUser user = currentUser();
        return notificationPreferenceRepo.findByOrganisation_IdAndUser_Id(organisationId, user.getId())
                .map(this::toNotificationPreferenceDto)
                .orElse(new CrmNotificationPreferenceDto(true, true, false));
    }

    public CrmNotificationPreferenceDto saveNotificationPreferences(Long requestedOrganisationId, CrmNotificationPreferenceDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        AppUser user = currentUser();
        CrmNotificationPreference entity = notificationPreferenceRepo
                .findByOrganisation_IdAndUser_Id(organisation.getId(), user.getId())
                .orElseGet(CrmNotificationPreference::new);
        entity.setOrganisation(organisation); entity.setUser(user);
        entity.setEmailNotifications(dto.emailNotifications());
        entity.setStageChangeAlerts(dto.stageChangeAlerts());
        entity.setClosingDateReminders(dto.closingDateReminders());
        return toNotificationPreferenceDto(notificationPreferenceRepo.save(entity));
    }

    private CrmNotificationPreferenceDto toNotificationPreferenceDto(CrmNotificationPreference entity) {
        return new CrmNotificationPreferenceDto(entity.isEmailNotifications(), entity.isStageChangeAlerts(), entity.isClosingDateReminders());
    }

    private Long tenant(Long requestedOrganisationId) {
        if (security.isPlatformUser()) {
            if (requestedOrganisationId == null) throw badRequest("An organisation is required.");
            return requestedOrganisationId;
        }
        Long authenticatedOrganisationId = security.getCurrentOrganisationId();
        if (requestedOrganisationId != null && !Objects.equals(requestedOrganisationId, authenticatedOrganisationId)) {
            throw forbidden("The requested organisation is not accessible.");
        }
        return authenticatedOrganisationId;
    }

    private Organisation organisation(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        return organisationRepo.findById(organisationId)
                .orElseThrow(() -> notFound("Organisation not found."));
    }

    private AppUser currentUser() {
        return userRepo.findById(security.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user no longer exists."));
    }

    private AppUser tenantUser(Long organisationId, Long userId) {
        if (userId == null) return null;
        return userRepo.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> badRequest("The selected user is outside this organisation."));
    }

    private CrmAccount account(Long organisationId, Long id) {
        return accountRepo.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> notFound("Account not found."));
    }

    private CrmLead lead(Long organisationId, Long id) {
        return leadRepo.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> notFound("Lead not found."));
    }

    private CrmOpportunity opportunity(Long organisationId, Long id) {
        return opportunityRepo.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> notFound("Opportunity not found."));
    }

    private CrmPipelineStage stage(Long organisationId, Long id) {
        return stageRepo.findByIdAndOrganisation_Id(id, organisationId)
                .orElseThrow(() -> notFound("Pipeline stage not found."));
    }

    private ProjectCategory category(Long organisationId, Long id) {
        return projectCategoryRepo.findByIdAndOrganisation_Id(id, organisationId)
                .filter(ProjectCategory::isActive)
                .orElseThrow(() -> badRequest("The selected organisation category is unavailable."));
    }

    // Accounts
    @Transactional(readOnly = true)
    public List<CrmAccountDto> getAccounts(Long requestedOrganisationId, String search) {
        Long organisationId = tenant(requestedOrganisationId);
        List<CrmAccount> accounts = search == null || search.isBlank()
                ? accountRepo.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId)
                : accountRepo.search(organisationId, search.trim());
        return accounts.stream().map(this::toAccountDto).toList();
    }

    @Transactional(readOnly = true)
    public CrmAccountDto getAccount(Long requestedOrganisationId, Long accountId) {
        return toAccountDto(account(tenant(requestedOrganisationId), accountId));
    }

    public CrmAccountDto createAccount(Long requestedOrganisationId, CrmAccountDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        requireText(dto.name(), "Account name is required.");
        if (accountRepo.findByOrganisation_IdAndNameIgnoreCase(organisation.getId(), dto.name().trim()).isPresent()) {
            throw conflict("An account with this name already exists.");
        }
        CrmAccount entity = new CrmAccount();
        entity.setOrganisation(organisation);
        mapAccount(entity, dto);
        return toAccountDto(accountRepo.save(entity));
    }

    public CrmAccountDto updateAccount(Long requestedOrganisationId, Long accountId, CrmAccountDto dto) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmAccount entity = account(organisationId, accountId);
        requireText(dto.name(), "Account name is required.");
        accountRepo.findByOrganisation_IdAndNameIgnoreCase(organisationId, dto.name().trim())
                .filter(other -> !Objects.equals(other.getId(), accountId))
                .ifPresent(other -> { throw conflict("An account with this name already exists."); });
        mapAccount(entity, dto);
        syncLegacyAccountNames(organisationId, entity);
        return toAccountDto(accountRepo.save(entity));
    }

    public void deleteAccount(Long requestedOrganisationId, Long accountId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmAccount entity = account(organisationId, accountId);
        if (leadRepo.countByOrganisation_IdAndAccount_IdAndActiveTrue(organisationId, accountId) > 0
                || opportunityRepo.countByOrganisation_IdAndAccount_Id(organisationId, accountId) > 0) {
            throw conflict("This account still has related leads or opportunities.");
        }
        accountRepo.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<CrmIndustryDto> getIndustries(Long requestedOrganisationId, boolean includeInactive) {
        Long organisationId = tenant(requestedOrganisationId);
        List<CrmIndustry> industries = includeInactive
                ? industryRepo.findByOrganisation_IdOrderByNameAsc(organisationId)
                : industryRepo.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId);
        return industries
                .stream().map(this::toIndustryDto).toList();
    }

    public CrmIndustryDto createIndustry(Long requestedOrganisationId, CrmIndustryDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        String name = requiredIndustryName(dto.name());
        if (industryRepo.findByOrganisation_IdAndNameIgnoreCase(organisation.getId(), name).isPresent()) {
            throw conflict("An industry with this name already exists.");
        }
        CrmIndustry entity = new CrmIndustry();
        entity.setOrganisation(organisation); entity.setName(name); entity.setActive(dto.active());
        return toIndustryDto(industryRepo.save(entity));
    }

    public CrmIndustryDto updateIndustry(Long requestedOrganisationId, Long industryId, CrmIndustryDto dto) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmIndustry entity = industry(industryId, organisationId);
        String name = requiredIndustryName(dto.name());
        industryRepo.findByOrganisation_IdAndNameIgnoreCase(organisationId, name)
                .filter(other -> !Objects.equals(other.getId(), industryId))
                .ifPresent(other -> { throw conflict("An industry with this name already exists."); });
        entity.setName(name); entity.setActive(dto.active());
        accountRepo.findByOrganisation_IdAndIndustryReference_Id(organisationId, industryId)
                .forEach(account -> account.setIndustry(name));
        return toIndustryDto(industryRepo.save(entity));
    }

    public void deleteIndustry(Long requestedOrganisationId, Long industryId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmIndustry entity = industry(industryId, organisationId);
        if (accountRepo.countByOrganisation_IdAndIndustryReference_Id(organisationId, industryId) > 0) {
            throw conflict("This industry is used by one or more accounts and cannot be deleted.");
        }
        industryRepo.delete(entity);
    }

    private CrmIndustry industry(Long industryId, Long organisationId) {
        return industryRepo.findByIdAndOrganisation_Id(industryId, organisationId)
                .orElseThrow(() -> notFound("Industry not found."));
    }

    private String requiredIndustryName(String value) {
        String name = blank(value);
        if (name == null) throw badRequest("Industry name is required.");
        if (name.length() > 120) throw badRequest("Industry name is too long.");
        return name;
    }

    private CrmIndustryDto toIndustryDto(CrmIndustry entity) {
        return new CrmIndustryDto(entity.getId(), entity.getName(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private void mapAccount(CrmAccount entity, CrmAccountDto dto) {
        entity.setName(dto.name().trim());
        CrmIndustry industry = dto.industryId() == null ? null : industry(dto.industryId(), entity.getOrganisation().getId());
        entity.setIndustryReference(industry);
        entity.setIndustry(industry == null ? blank(dto.industry()) : industry.getName());
        entity.setCountry(blank(dto.country()));
        entity.setCity(blank(dto.city()));
        entity.setAddress(blank(dto.address()));
        entity.setPhone(blank(dto.phone()));
        entity.setWebsite(blank(dto.website()));
        entity.setEmployees(blank(dto.employees()));
        entity.setAnnualRevenue(nonNegative(dto.annualRevenue(), "Annual revenue"));
        entity.setCurrency(blank(dto.currency()) == null ? "EUR" : dto.currency().trim().toUpperCase(Locale.ROOT));
        entity.setOwner(tenantUser(entity.getOrganisation().getId(), dto.ownerId()));
        entity.setNotes(blank(dto.notes()));
    }

    private CrmAccountDto toAccountDto(CrmAccount entity) {
        Long organisationId = entity.getOrganisation().getId();
        long leads = leadRepo.countByOrganisation_IdAndAccount_IdAndActiveTrue(organisationId, entity.getId());
        long opportunities = opportunityRepo.countByOrganisation_IdAndAccount_Id(organisationId, entity.getId());
        BigDecimal pipeline = Optional.ofNullable(opportunityRepo.sumValueByAccount(organisationId, entity.getId())).orElse(BigDecimal.ZERO);
        return new CrmAccountDto(entity.getId(), entity.getName(), entity.getIndustry(),
                entity.getIndustryReference() == null ? null : entity.getIndustryReference().getId(), entity.getCountry(), entity.getCity(),
                entity.getAddress(), entity.getPhone(), entity.getWebsite(), entity.getEmployees(), entity.getAnnualRevenue(),
                entity.getCurrency(), entity.getOwner() == null ? null : entity.getOwner().getId(),
                entity.getOwner() == null ? null : entity.getOwner().getFullName(), entity.getNotes(), leads, opportunities,
                pipeline, entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private void syncLegacyAccountNames(Long organisationId, CrmAccount entity) {
        leadRepo.findByOrganisation_IdAndAccount_IdAndActiveTrueOrderByFullNameAsc(organisationId, entity.getId())
                .forEach(item -> item.setCompany(entity.getName()));
        opportunityRepo.findByOrganisation_IdAndAccount_IdOrderByCreatedAtDesc(organisationId, entity.getId())
                .forEach(item -> item.setAccountName(entity.getName()));
    }

    // Pipeline stages and supply categories
    public List<CrmPipelineStageDto> getStages(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        seedConfiguration(organisationId);
        return stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream().map(this::toStageDto).toList();
    }

    public CrmPipelineStageDto createStage(Long requestedOrganisationId, CrmPipelineStageDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        requireText(dto.getName(), "Stage name is required.");
        if (stageRepo.existsByOrganisation_IdAndName(organisation.getId(), dto.getName().trim())) {
            throw conflict("A stage with this name already exists.");
        }
        CrmPipelineStage entity = new CrmPipelineStage();
        entity.setOrganisation(organisation);
        mapStage(entity, dto);
        return toStageDto(stageRepo.save(entity));
    }

    public CrmPipelineStageDto updateStage(Long requestedOrganisationId, Long stageId, CrmPipelineStageDto dto) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmPipelineStage entity = stage(organisationId, stageId);
        requireText(dto.getName(), "Stage name is required.");
        mapStage(entity, dto);
        return toStageDto(stageRepo.save(entity));
    }

    public void deleteStage(Long requestedOrganisationId, Long stageId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmPipelineStage entity = stage(organisationId, stageId);
        if (opportunityRepo.existsByOrganisation_IdAndStage_Id(organisationId, stageId)) {
            throw conflict("Move opportunities out of this stage before deleting it.");
        }
        stageRepo.delete(entity);
    }

    public List<CrmSupplyCategoryDto> getOrganisationCategories(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        migrateLegacySupplyCategories(organisationId);
        return projectCategoryRepo.findByOrganisation_IdAndActiveTrueOrderByNameAsc(organisationId)
                .stream().map(this::toCategoryDto).toList();
    }

    private void seedConfiguration(Long organisationId) {
        Organisation organisation = organisation(organisationId);
        if (stageRepo.countByOrganisation_Id(organisationId) == 0) {
            stageRepo.saveAll(List.of(
                    new CrmPipelineStage(organisation, "Make presentation", "#94a3b8", 0, 0, false, false),
                    new CrmPipelineStage(organisation, "Problem setting", "#6366f1", 1, 0, false, false),
                    new CrmPipelineStage(organisation, "Problem solving", "#06b6d4", 2, 0, false, false),
                    new CrmPipelineStage(organisation, "Proposal/Quote", "#3b82f6", 3, 0, false, false),
                    new CrmPipelineStage(organisation, "Negotiation/Revision", "#f59e0b", 4, 0, false, false),
                    new CrmPipelineStage(organisation, "Closed won", "#0f7b4f", 5, 0, true, false),
                    new CrmPipelineStage(organisation, "Closed lost", "#c0392b", 6, 0, false, true),
                    new CrmPipelineStage(organisation, "Abandoned", "#64748b", 7, 0, false, true)
            ));
        }
    }

    private void mapStage(CrmPipelineStage entity, CrmPipelineStageDto dto) {
        entity.setName(dto.getName().trim());
        entity.setColor(blank(dto.getColor()) == null ? "#64748b" : dto.getColor());
        entity.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : Math.max(0, dto.getDisplayOrder()));
        entity.setWon(dto.isWon()); entity.setLost(dto.isLost());
    }

    private CrmPipelineStageDto toStageDto(CrmPipelineStage entity) {
        CrmPipelineStageDto dto = new CrmPipelineStageDto();
        dto.setId(entity.getId()); dto.setName(entity.getName()); dto.setColor(entity.getColor());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setWon(entity.isWon()); dto.setLost(entity.isLost());
        return dto;
    }

    private CrmSupplyCategoryDto toCategoryDto(ProjectCategory entity) {
        return new CrmSupplyCategoryDto(entity.getId(), entity.getName(), 0, entity.isActive());
    }

    /**
     * One-time, idempotent bridge for opportunities saved before CRM reused
     * organisation categories.  Legacy rows are converted into organisation
     * categories and the opportunity is linked to that canonical category.
     */
    private void migrateLegacySupplyCategories(Long organisationId) {
        List<CrmOpportunity> opportunities = opportunityRepo.findByOrganisation_IdOrderByCreatedAtDesc(organisationId);
        for (CrmOpportunity opportunity : opportunities) {
            if (opportunity.getSupplyCategory() != null || opportunity.getLegacySupplyCategory() == null) continue;
            String name = opportunity.getLegacySupplyCategory().getName();
            ProjectCategory target = projectCategoryRepo.findByOrganisation_IdAndNameIgnoreCase(organisationId, name)
                    .orElseGet(() -> {
                        ProjectCategory created = new ProjectCategory();
                        created.setOrganisation(opportunity.getOrganisation());
                        created.setName(name);
                        created.setCode("CRM-LEGACY-" + opportunity.getLegacySupplyCategory().getId());
                        created.setDescription("Migrated from the legacy CRM supply category.");
                        created.setActive(true);
                        return projectCategoryRepo.save(created);
                    });
            opportunity.setSupplyCategory(target);
        }
    }

    // Leads
    public List<CrmLeadDto> getLeads(Long requestedOrganisationId, String search, String status, Long accountId) {
        Long organisationId = tenant(requestedOrganisationId);
        List<CrmLead> values;
        if (accountId != null) {
            account(organisationId, accountId);
            values = leadRepo.findByOrganisation_IdAndAccount_IdAndActiveTrueOrderByFullNameAsc(organisationId, accountId);
        } else if (search != null && !search.isBlank()) {
            values = leadRepo.searchByOrgAndTerm(organisationId, search.trim());
        } else if (status != null && !status.isBlank()) {
            values = leadRepo.findByOrganisation_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
                    organisationId, parseEnum(CrmLead.Status.class, status, "lead status"));
        } else {
            values = leadRepo.findByOrganisation_IdAndActiveTrueOrderByCreatedAtDesc(organisationId);
        }
        return values.stream().map(this::toLeadDto).toList();
    }

    public CrmLeadDto getLead(Long requestedOrganisationId, Long leadId) {
        return toLeadDto(lead(tenant(requestedOrganisationId), leadId));
    }

    public CrmLeadDto createLead(Long requestedOrganisationId, CrmLeadDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        CrmLead entity = new CrmLead(); entity.setOrganisation(organisation);
        mapLead(entity, dto, true);
        entity = leadRepo.save(entity);
        logActivity(organisation, currentUser(), CrmActivity.ActivityType.LEAD_CREATED,
                "New lead created: " + entity.getFullName(), entity, null);
        return toLeadDto(entity);
    }

    public CrmLeadDto updateLead(Long requestedOrganisationId, Long leadId, CrmLeadDto dto) {
        CrmLead entity = lead(tenant(requestedOrganisationId), leadId);
        mapLead(entity, dto, false);
        return toLeadDto(leadRepo.save(entity));
    }

    public void deleteLead(Long requestedOrganisationId, Long leadId) {
        leadRepo.delete(lead(tenant(requestedOrganisationId), leadId));
    }

    public CrmOpportunityDto convertLead(Long requestedOrganisationId, Long leadId, Long stageId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmLead source = lead(organisationId, leadId);
        if (source.getAccount() == null) throw badRequest("The lead must belong to an account before conversion.");
        if (source.isConverted()) throw conflict("This lead has already been converted.");
        source.setConverted(true); source.setConvertedAt(LocalDateTime.now()); source.setStatus(CrmLead.Status.CONVERTED);
        CrmOpportunity entity = new CrmOpportunity();
        entity.setOrganisation(source.getOrganisation());
        entity.setName(source.getAccount().getName() + " — " + source.getFullName());
        entity.setAccount(source.getAccount()); entity.setAccountName(source.getAccount().getName());
        entity.setOwner(source.getOwner()); entity.setLead(source);
        CrmPipelineStage selected = stageId == null ? firstStage(organisationId) : stage(organisationId, stageId);
        applyStage(entity, selected);
        entity = opportunityRepo.save(entity);
        recordStageHistory(entity, currentUser());
        logActivity(source.getOrganisation(), currentUser(), CrmActivity.ActivityType.LEAD_CONVERTED,
                "Lead converted to opportunity: " + entity.getName(), source, entity);
        return toOpportunityDto(entity);
    }

    private void mapLead(CrmLead entity, CrmLeadDto dto, boolean creating) {
        requireText(dto.getFullName(), "Lead name is required.");
        if (dto.getAccountId() == null) throw badRequest("An account is required for every lead.");
        CrmAccount selectedAccount = account(entity.getOrganisation().getId(), dto.getAccountId());
        entity.setFullName(dto.getFullName().trim()); entity.setAccount(selectedAccount); entity.setCompany(selectedAccount.getName());
        entity.setJobTitle(blank(dto.getJobTitle())); entity.setEmail(blank(dto.getEmail())); entity.setPhone(blank(dto.getPhone()));
        entity.setMobile(blank(dto.getMobile())); entity.setRating(blank(dto.getRating()));
        if (dto.getSource() != null) entity.setSource(parseEnum(CrmLead.Source.class, dto.getSource(), "lead source"));
        if (dto.getStatus() != null) entity.setStatus(parseEnum(CrmLead.Status.class, dto.getStatus(), "lead status"));
        entity.setOwner(tenantUser(entity.getOrganisation().getId(), dto.getOwnerId()));
        entity.setNotes(blank(dto.getNotes()));
        entity.setActive(creating || dto.isActive());
    }

    private CrmLeadDto toLeadDto(CrmLead entity) {
        CrmLeadDto dto = new CrmLeadDto();
        dto.setId(entity.getId()); dto.setFullName(entity.getFullName()); dto.setCompany(entity.getCompany());
        if (entity.getAccount() != null) { dto.setAccountId(entity.getAccount().getId()); dto.setAccountName(entity.getAccount().getName()); }
        dto.setJobTitle(entity.getJobTitle()); dto.setEmail(entity.getEmail()); dto.setPhone(entity.getPhone());
        dto.setMobile(entity.getMobile()); dto.setRating(entity.getRating());
        dto.setSource(entity.getSource().name()); dto.setStatus(entity.getStatus().name());
        if (entity.getOwner() != null) { dto.setOwnerId(entity.getOwner().getId()); dto.setOwnerName(entity.getOwner().getFullName()); }
        dto.setConverted(entity.isConverted()); dto.setConvertedAt(entity.getConvertedAt()); dto.setNotes(entity.getNotes());
        dto.setActive(entity.isActive()); dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Opportunities
    public List<CrmOpportunityDto> getOpportunities(Long requestedOrganisationId, Long ownerId, Long accountId,
                                                     Long leadId, Long stageId) {
        Long organisationId = tenant(requestedOrganisationId);
        migrateLegacySupplyCategories(organisationId);
        return opportunityRepo.findFiltered(organisationId, ownerId, accountId, leadId, stageId)
                .stream().map(this::toOpportunityDto).toList();
    }

    public CrmOpportunityDto getOpportunity(Long requestedOrganisationId, Long opportunityId) {
        Long organisationId = tenant(requestedOrganisationId);
        migrateLegacySupplyCategories(organisationId);
        return toOpportunityDto(opportunity(organisationId, opportunityId));
    }

    public CrmOpportunityDto createOpportunity(Long requestedOrganisationId, CrmOpportunityDto dto) {
        Organisation organisation = organisation(requestedOrganisationId);
        seedConfiguration(organisation.getId());
        CrmOpportunity entity = new CrmOpportunity(); entity.setOrganisation(organisation);
        mapOpportunity(entity, dto, true);
        entity = opportunityRepo.save(entity);
        recordStageHistory(entity, currentUser());
        logActivity(organisation, currentUser(), CrmActivity.ActivityType.OPPORTUNITY_CREATED,
                "New opportunity created: " + entity.getName(), null, entity);
        return toOpportunityDto(entity);
    }

    public CrmOpportunityDto updateOpportunity(Long requestedOrganisationId, Long opportunityId, CrmOpportunityDto dto) {
        CrmOpportunity entity = opportunity(tenant(requestedOrganisationId), opportunityId);
        Map<String, String> before = opportunitySnapshot(entity);
        Long oldStageId = entity.getStage() == null ? null : entity.getStage().getId();
        mapOpportunity(entity, dto, false);
        entity = opportunityRepo.save(entity);
        recordChanges(entity, before);
        Long newStageId = entity.getStage() == null ? null : entity.getStage().getId();
        if (!Objects.equals(oldStageId, newStageId)) {
            recordStageHistory(entity, currentUser());
            logActivity(entity.getOrganisation(), currentUser(), CrmActivity.ActivityType.STAGE_UPDATED,
                    "Stage updated to " + (entity.getStage() == null ? "Unassigned" : entity.getStage().getName()), null, entity);
        }
        return toOpportunityDto(entity);
    }

    public CrmOpportunityDto changeStage(Long requestedOrganisationId, Long opportunityId, Long stageId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmOpportunity entity = opportunity(organisationId, opportunityId);
        String old = entity.getStage() == null ? null : entity.getStage().getName();
        applyStage(entity, stage(organisationId, stageId));
        entity = opportunityRepo.save(entity);
        history(entity, "Stage", old, entity.getStage().getName());
        recordStageHistory(entity, currentUser());
        logActivity(entity.getOrganisation(), currentUser(), CrmActivity.ActivityType.STAGE_UPDATED,
                "Stage updated to " + entity.getStage().getName(), null, entity);
        return toOpportunityDto(entity);
    }

    public CrmOpportunityDto updateOpportunityTeam(Long requestedOrganisationId, Long opportunityId, List<Long> requestedUserIds) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmOpportunity entity = opportunity(organisationId, opportunityId);
        String before = entity.getTeamMembers().stream().map(AppUser::getFullName).sorted().collect(Collectors.joining(", "));
        LinkedHashSet<Long> userIds = requestedUserIds == null ? new LinkedHashSet<>() : requestedUserIds.stream()
                .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<AppUser> members = new LinkedHashSet<>();
        for (Long userId : userIds) {
            AppUser member = tenantUser(organisationId, userId);
            if (!member.isActive() || !OPPORTUNITY_TEAM_ROLES.contains(member.getRole())) {
                throw badRequest("Only active CRM users can be assigned to an opportunity team.");
            }
            members.add(member);
        }
        entity.getTeamMembers().clear();
        entity.getTeamMembers().addAll(members);
        entity = opportunityRepo.save(entity);
        String after = entity.getTeamMembers().stream().map(AppUser::getFullName).sorted().collect(Collectors.joining(", "));
        if (!Objects.equals(before, after)) history(entity, "Team", blank(before), blank(after));
        return toOpportunityDto(entity);
    }

    public CrmOpportunityDto markWon(Long requestedOrganisationId, Long id) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmPipelineStage won = stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream()
                .filter(CrmPipelineStage::isWon).findFirst().orElseThrow(() -> conflict("No won stage is configured."));
        return changeStage(organisationId, id, won.getId());
    }

    public CrmOpportunityDto markLost(Long requestedOrganisationId, Long id) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmPipelineStage lost = stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream()
                .filter(CrmPipelineStage::isLost).findFirst().orElseThrow(() -> conflict("No lost stage is configured."));
        return changeStage(organisationId, id, lost.getId());
    }

    public void deleteOpportunity(Long requestedOrganisationId, Long id) {
        opportunityRepo.delete(opportunity(tenant(requestedOrganisationId), id));
    }

    private void mapOpportunity(CrmOpportunity entity, CrmOpportunityDto dto, boolean creating) {
        Long organisationId = entity.getOrganisation().getId();
        requireText(dto.getName(), "Opportunity name is required.");
        if (dto.getAccountId() == null) throw badRequest("An account is required for every opportunity.");
        CrmAccount selectedAccount = account(organisationId, dto.getAccountId());
        entity.setName(dto.getName().trim()); entity.setAccount(selectedAccount); entity.setAccountName(selectedAccount.getName());
        entity.setOwner(tenantUser(organisationId, dto.getOwnerId()));
        if (dto.getLeadId() == null) entity.setLead(null);
        else {
            CrmLead contact = lead(organisationId, dto.getLeadId());
            if (contact.getAccount() == null || !Objects.equals(contact.getAccount().getId(), selectedAccount.getId())) {
                throw badRequest("The contact person must belong to the selected account.");
            }
            entity.setLead(contact);
        }
        entity.setSupplyCategory(dto.getSupplyCategoryId() == null ? null : category(organisationId, dto.getSupplyCategoryId()));
        entity.setOpportunityType(blank(dto.getOpportunityType())); entity.setPipeline(blank(dto.getPipeline()));
        entity.setQuoteNumber(blank(dto.getQuoteNumber())); entity.setQuoteRequestedDate(dto.getQuoteRequestedDate());
        entity.setQuoteSubmittedDate(dto.getQuoteSubmittedDate()); entity.setShipmentDate(dto.getShipmentDate());
        entity.setClosingDate(dto.getClosingDate()); entity.setNextStep(blank(dto.getNextStep()));
        entity.setDescription(blank(dto.getDescription()) != null ? blank(dto.getDescription()) : blank(dto.getNotes()));
        entity.setCurrency(blank(dto.getCurrency()) == null ? "EUR" : dto.getCurrency().trim().toUpperCase(Locale.ROOT));
        entity.setMaterialValue(nonNegative(dto.getMaterialValue(), "Material value"));
        entity.setServicesValue(nonNegative(dto.getServicesValue(), "Services value"));
        entity.setErcopacMaterialValue(nonNegative(dto.getErcopacMaterialValue(), "Ercopac material value"));
        entity.setThirdPartyMaterialValue(nonNegative(dto.getThirdPartyMaterialValue(), "Third-party material value"));
        entity.setErcopacResaleValue(nonNegative(dto.getErcopacResaleValue(), "Ercopac resale value"));
        entity.setResaleValue(nonNegative(dto.getResaleValue(), "Resale value"));
        BigDecimal calculated = sum(entity.getMaterialValue(), entity.getServicesValue());
        entity.setValue(calculated.signum() > 0 ? calculated : nonNegative(dto.getValue(), "Opportunity value"));
        CrmPipelineStage selectedStage = dto.getStageId() == null
                ? (creating ? firstStage(organisationId) : entity.getStage()) : stage(organisationId, dto.getStageId());
        if (selectedStage != null) applyStage(entity, selectedStage);
        entity.setProbability(probability(dto.getProbability()));
    }

    private void applyStage(CrmOpportunity entity, CrmPipelineStage selected) {
        entity.setStage(selected);
        entity.setWon(selected.isWon()); entity.setLost(selected.isLost());
    }

    private CrmPipelineStage firstStage(Long organisationId) {
        return stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream().findFirst()
                .orElseThrow(() -> conflict("No pipeline stage is configured."));
    }

    private CrmOpportunityDto toOpportunityDto(CrmOpportunity entity) {
        CrmOpportunityDto dto = new CrmOpportunityDto();
        dto.setId(entity.getId()); dto.setName(entity.getName()); dto.setAccountName(entity.getAccountName());
        if (entity.getAccount() != null) { dto.setAccountId(entity.getAccount().getId()); dto.setAccountCountry(entity.getAccount().getCountry()); }
        if (entity.getStage() != null) { dto.setStageId(entity.getStage().getId()); dto.setStageName(entity.getStage().getName()); dto.setStageColor(entity.getStage().getColor()); }
        dto.setValue(entity.getValue()); dto.setCurrency(entity.getCurrency()); dto.setProbability(entity.getProbability());
        dto.setClosingDate(entity.getClosingDate()); dto.setWon(entity.isWon()); dto.setLost(entity.isLost());
        if (entity.getOwner() != null) { dto.setOwnerId(entity.getOwner().getId()); dto.setOwnerName(entity.getOwner().getFullName()); }
        if (entity.getLead() != null) { dto.setLeadId(entity.getLead().getId()); dto.setContactName(entity.getLead().getFullName()); }
        if (entity.getSupplyCategory() != null) { dto.setSupplyCategoryId(entity.getSupplyCategory().getId()); dto.setSupplyCategoryName(entity.getSupplyCategory().getName()); }
        else if (entity.getLegacySupplyCategory() != null) { dto.setSupplyCategoryName(entity.getLegacySupplyCategory().getName()); }
        dto.setOpportunityType(entity.getOpportunityType()); dto.setPipeline(entity.getPipeline()); dto.setQuoteNumber(entity.getQuoteNumber());
        dto.setQuoteRequestedDate(entity.getQuoteRequestedDate()); dto.setQuoteSubmittedDate(entity.getQuoteSubmittedDate());
        dto.setShipmentDate(entity.getShipmentDate()); dto.setNextStep(entity.getNextStep()); dto.setDescription(entity.getDescription());
        dto.setNotes(entity.getDescription()); dto.setMaterialValue(entity.getMaterialValue()); dto.setServicesValue(entity.getServicesValue());
        dto.setErcopacMaterialValue(entity.getErcopacMaterialValue()); dto.setThirdPartyMaterialValue(entity.getThirdPartyMaterialValue());
        dto.setErcopacResaleValue(entity.getErcopacResaleValue()); dto.setResaleValue(entity.getResaleValue());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setTeamMembers(entity.getTeamMembers().stream()
                .sorted(Comparator.comparing(AppUser::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCrmUserDto).toList());
        return dto;
    }

    // Notes, attachments and audit
    public List<CrmOpportunityNoteDto> getNotes(Long requestedOrganisationId, Long opportunityId) {
        Long organisationId = tenant(requestedOrganisationId); opportunity(organisationId, opportunityId);
        return noteRepo.findByOpportunity_IdAndOrganisation_IdOrderByCreatedAtAsc(opportunityId, organisationId)
                .stream().map(this::toNoteDto).toList();
    }

    public CrmOpportunityNoteDto addNote(Long requestedOrganisationId, Long opportunityId, String content) {
        Long organisationId = tenant(requestedOrganisationId);
        requireText(content, "Note content is required.");
        CrmOpportunity opportunity = opportunity(organisationId, opportunityId);
        CrmOpportunityNote note = new CrmOpportunityNote(); note.setOrganisation(opportunity.getOrganisation());
        note.setOpportunity(opportunity); note.setAuthor(currentUser()); note.setContent(content.trim());
        note = noteRepo.save(note);
        logActivity(opportunity.getOrganisation(), currentUser(), CrmActivity.ActivityType.NOTE_ADDED,
                "Note added to " + opportunity.getName(), null, opportunity);
        return toNoteDto(note);
    }

    public CrmOpportunityNoteDto updateNote(Long requestedOrganisationId, Long opportunityId, Long noteId, String content) {
        Long organisationId = tenant(requestedOrganisationId); requireText(content, "Note content is required.");
        CrmOpportunityNote note = noteRepo.findByIdAndOpportunity_IdAndOrganisation_Id(noteId, opportunityId, organisationId)
                .orElseThrow(() -> notFound("Note not found."));
        note.setContent(content.trim()); return toNoteDto(noteRepo.save(note));
    }

    public void deleteNote(Long requestedOrganisationId, Long opportunityId, Long noteId) {
        Long organisationId = tenant(requestedOrganisationId);
        noteRepo.delete(noteRepo.findByIdAndOpportunity_IdAndOrganisation_Id(noteId, opportunityId, organisationId)
                .orElseThrow(() -> notFound("Note not found.")));
    }

    public List<CrmOpportunityAttachmentDto> getAttachments(Long requestedOrganisationId, Long opportunityId) {
        Long organisationId = tenant(requestedOrganisationId); opportunity(organisationId, opportunityId);
        return attachmentRepo.findByOpportunity_IdAndOrganisation_IdOrderByUploadedAtDesc(opportunityId, organisationId)
                .stream().map(this::toAttachmentDto).toList();
    }

    public CrmOpportunityAttachmentDto uploadAttachment(Long requestedOrganisationId, Long opportunityId, MultipartFile file) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmOpportunity opportunity = opportunity(organisationId, opportunityId);
        if (file == null || file.isEmpty()) throw badRequest("A non-empty attachment is required.");
        if (file.getSize() > 10 * 1024 * 1024) throw badRequest("Attachment must not exceed 10 MB.");
        String type = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        if (!ATTACHMENT_TYPES.contains(type)) throw badRequest("Unsupported attachment type.");
        String original = sanitizeFileName(file.getOriginalFilename());
        String stored = UUID.randomUUID() + extension(type);
        Path tenantRoot = attachmentRoot.resolve(String.valueOf(organisationId)).normalize();
        Path target = tenantRoot.resolve(stored).normalize();
        if (!target.startsWith(tenantRoot)) throw badRequest("Invalid attachment path.");
        try { Files.createDirectories(tenantRoot); Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING); }
        catch (IOException exception) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store attachment."); }
        CrmOpportunityAttachment attachment = new CrmOpportunityAttachment();
        attachment.setOrganisation(opportunity.getOrganisation()); attachment.setOpportunity(opportunity);
        attachment.setOriginalFileName(original); attachment.setStoredFileName(stored); attachment.setStoragePath(stored);
        attachment.setContentType(type); attachment.setFileSize(file.getSize()); attachment.setUploadedBy(currentUser());
        attachment = attachmentRepo.save(attachment);
        logActivity(opportunity.getOrganisation(), currentUser(), CrmActivity.ActivityType.OFFER_ATTACHED,
                "File attached: " + original, null, opportunity);
        return toAttachmentDto(attachment);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload downloadAttachment(Long requestedOrganisationId, Long opportunityId, Long attachmentId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmOpportunityAttachment attachment = attachmentRepo
                .findByIdAndOpportunity_IdAndOrganisation_Id(attachmentId, opportunityId, organisationId)
                .orElseThrow(() -> notFound("Attachment not found."));
        Path tenantRoot = attachmentRoot.resolve(String.valueOf(organisationId)).normalize();
        Path path = tenantRoot.resolve(attachment.getStoragePath()).normalize();
        if (!path.startsWith(tenantRoot) || !Files.isRegularFile(path)) throw notFound("Attachment file not found.");
        return new AttachmentDownload(new FileSystemResource(path), attachment.getOriginalFileName(), attachment.getContentType());
    }

    public void deleteAttachment(Long requestedOrganisationId, Long opportunityId, Long attachmentId) {
        Long organisationId = tenant(requestedOrganisationId);
        CrmOpportunityAttachment attachment = attachmentRepo
                .findByIdAndOpportunity_IdAndOrganisation_Id(attachmentId, opportunityId, organisationId)
                .orElseThrow(() -> notFound("Attachment not found."));
        Path tenantRoot = attachmentRoot.resolve(String.valueOf(organisationId)).normalize();
        Path target = tenantRoot.resolve(attachment.getStoragePath()).normalize();
        if (!target.startsWith(tenantRoot)) throw badRequest("Invalid attachment path.");
        try { Files.deleteIfExists(target); } catch (IOException ignored) { }
        attachmentRepo.delete(attachment);
    }

    public List<CrmOpportunityHistoryDto> getHistory(Long requestedOrganisationId, Long opportunityId) {
        Long organisationId = tenant(requestedOrganisationId); opportunity(organisationId, opportunityId);
        return historyRepo.findByOpportunity_IdAndOrganisation_IdOrderByCreatedAtDesc(opportunityId, organisationId)
                .stream().map(item -> new CrmOpportunityHistoryDto(item.getId(), item.getFieldName(), item.getOldValue(), item.getNewValue(),
                        item.getChangedBy() == null ? null : item.getChangedBy().getId(),
                        item.getChangedBy() == null ? null : item.getChangedBy().getFullName(), item.getCreatedAt())).toList();
    }

    public List<CrmOpportunityStageHistoryDto> getStageHistory(Long requestedOrganisationId, Long opportunityId) {
        Long organisationId = tenant(requestedOrganisationId); opportunity(organisationId, opportunityId);
        return stageHistoryRepo.findByOpportunity_IdAndOrganisation_IdOrderByEnteredAtDesc(opportunityId, organisationId)
                .stream().map(item -> new CrmOpportunityStageHistoryDto(item.getId(), item.getStage() == null ? null : item.getStage().getId(),
                        item.getStageName(), item.getProbability(), item.getClosingDate(),
                        item.getModifiedBy() == null ? null : item.getModifiedBy().getId(),
                        item.getModifiedBy() == null ? null : item.getModifiedBy().getFullName(), item.getEnteredAt())).toList();
    }

    private void recordChanges(CrmOpportunity entity, Map<String, String> before) {
        Map<String, String> after = opportunitySnapshot(entity);
        before.forEach((field, oldValue) -> {
            String newValue = after.get(field);
            if (!Objects.equals(oldValue, newValue)) history(entity, field, oldValue, newValue);
        });
    }

    private Map<String, String> opportunitySnapshot(CrmOpportunity entity) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Opportunity name", entity.getName()); result.put("Account", entity.getAccountName());
        result.put("Contact person", entity.getLead() == null ? null : entity.getLead().getFullName());
        result.put("Stage", entity.getStage() == null ? null : entity.getStage().getName());
        result.put("Value", string(entity.getValue())); result.put("Probability", string(entity.getProbability()));
        result.put("Owner", entity.getOwner() == null ? null : entity.getOwner().getFullName());
        result.put("Closing date", string(entity.getClosingDate())); result.put("Supply category", entity.getSupplyCategory() == null ? null : entity.getSupplyCategory().getName());
        result.put("Description", entity.getDescription()); result.put("Next step", entity.getNextStep());
        return result;
    }

    private void history(CrmOpportunity opportunity, String field, String oldValue, String newValue) {
        CrmOpportunityHistory item = new CrmOpportunityHistory(); item.setOrganisation(opportunity.getOrganisation());
        item.setOpportunity(opportunity); item.setFieldName(field); item.setOldValue(oldValue); item.setNewValue(newValue);
        item.setChangedBy(currentUser()); historyRepo.save(item);
    }

    private void recordStageHistory(CrmOpportunity opportunity, AppUser user) {
        if (opportunity.getStage() == null) return;
        CrmOpportunityStageHistory item = new CrmOpportunityStageHistory();
        item.setOrganisation(opportunity.getOrganisation()); item.setOpportunity(opportunity); item.setStage(opportunity.getStage());
        item.setStageName(opportunity.getStage().getName()); item.setProbability(opportunity.getProbability());
        item.setClosingDate(opportunity.getClosingDate()); item.setModifiedBy(user); stageHistoryRepo.save(item);
    }

    // Dashboard, reports and lead-only manager view
    public CrmDashboardDto getDashboard(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId); seedConfiguration(organisationId);
        CrmDashboardDto dto = new CrmDashboardDto();
        dto.setOpenOpportunities(opportunityRepo.countByOrganisation_IdAndWonFalseAndLostFalse(organisationId));
        dto.setPipelineValue(Optional.ofNullable(opportunityRepo.sumPipelineValue(organisationId)).orElse(BigDecimal.ZERO));
        dto.setActiveLeads(leadRepo.countByOrganisation_IdAndActiveTrue(organisationId));
        dto.setWonThisMonth(opportunityRepo.countWonSince(organisationId, LocalDate.now().withDayOfMonth(1).atStartOfDay()));
        dto.setRecentActivities(activityRepo.findByOrganisation_IdOrderByCreatedAtDesc(organisationId, PageRequest.of(0, 10))
                .stream().map(this::toActivityDto).toList());
        LocalDate now = LocalDate.now();
        dto.setClosingThisMonth(opportunityRepo.findByOrganisation_IdAndClosingDateBetweenOrderByClosingDateAsc(
                organisationId, now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth())).stream().map(this::toOpportunityDto).toList());
        Map<String, Long> sources = new LinkedHashMap<>();
        leadRepo.countBySource(organisationId).forEach(row -> sources.put(row[0].toString(), (Long) row[1])); dto.setLeadsBySource(sources);
        Map<Long, Long> counts = new HashMap<>();
        opportunityRepo.countByStage(organisationId).forEach(row -> counts.put((Long) row[0], (Long) row[1]));
        List<CrmPipelineStageDto> stages = stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream().map(value -> {
            CrmPipelineStageDto stageDto = toStageDto(value); stageDto.setOpportunityCount(counts.getOrDefault(value.getId(), 0L).intValue()); return stageDto;
        }).toList(); dto.setPipeline(stages);
        return dto;
    }

    public CrmReportsDto getReports(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        migrateLegacySupplyCategories(organisationId);
        List<CrmOpportunity> values = opportunityRepo.findByOrganisation_IdOrderByCreatedAtDesc(organisationId);
        BigDecimal total = values.stream().map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weighted = values.stream().map(item -> zero(item.getValue())
                .multiply(BigDecimal.valueOf(item.getProbability() == null ? 0 : item.getProbability()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CrmReportsDto(values.size(), total, weighted,
                breakdown(values, item -> item.getAccount() == null ? "Unspecified" : defaultValue(item.getAccount().getCountry(), "Unspecified")),
                breakdown(values, item -> item.getStage() == null ? "Unassigned" : item.getStage().getName()),
                breakdown(values, item -> item.getSupplyCategory() == null ?
                        (item.getLegacySupplyCategory() == null ? "Unspecified" : item.getLegacySupplyCategory().getName()) : item.getSupplyCategory().getName()),
                values.stream().map(item -> zero(item.getMaterialValue())).reduce(BigDecimal.ZERO, BigDecimal::add),
                values.stream().map(item -> zero(item.getServicesValue())).reduce(BigDecimal.ZERO, BigDecimal::add),
                values.stream().map(this::toOpportunityDto).toList());
    }

    @Transactional(readOnly = true)
    public CrmAnalyticsDto getAnalytics(Long requestedOrganisationId, String requestedOpportunityType) {
        Long organisationId = tenant(requestedOrganisationId);
        String opportunityType = blank(requestedOpportunityType);
        List<CrmOpportunity> opportunities = opportunityType == null
                ? opportunityRepo.findByOrganisation_IdOrderByCreatedAtDesc(organisationId)
                : opportunityRepo.findByOrganisation_IdAndOpportunityTypeIgnoreCaseOrderByCreatedAtDesc(organisationId, opportunityType);
        BigDecimal pipelineValue = opportunities.stream().filter(item -> !item.isWon() && !item.isLost())
                .map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal wonValue = opportunities.stream().filter(CrmOpportunity::isWon)
                .map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Long, List<CrmOpportunity>> byStage = opportunities.stream()
                .filter(item -> item.getStage() != null)
                .collect(Collectors.groupingBy(item -> item.getStage().getId()));
        List<CrmAnalyticsDto.StageMetric> stages = stageRepo.findByOrganisation_IdOrderByDisplayOrderAsc(organisationId).stream()
                .map(stage -> {
                    List<CrmOpportunity> stageOpportunities = byStage.getOrDefault(stage.getId(), List.of());
                    BigDecimal value = stageOpportunities.stream().map(item -> zero(item.getValue()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CrmAnalyticsDto.StageMetric(stage.getName(), stage.getColor(), stageOpportunities.size(), value);
                }).toList();
        List<CrmAnalyticsDto.SourceMetric> sources = leadRepo.countBySource(organisationId).stream()
                .map(row -> new CrmAnalyticsDto.SourceMetric(
                        defaultValue(row[0] == null ? null : row[0].toString(), "Unspecified"),
                        ((Number) row[1]).longValue()))
                .toList();
        return new CrmAnalyticsDto(opportunityType, opportunities.size(), pipelineValue, wonValue,
                leadRepo.countByOrganisation_IdAndActiveTrue(organisationId), stages, sources,
                opportunities.stream().map(this::toOpportunityDto).toList());
    }

    private List<CrmReportsDto.Breakdown> breakdown(List<CrmOpportunity> values, Function<CrmOpportunity, String> classifier) {
        return values.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.toList())).entrySet().stream()
                .map(entry -> new CrmReportsDto.Breakdown(entry.getKey(), entry.getValue().size(),
                        entry.getValue().stream().map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparingLong(CrmReportsDto.Breakdown::count).reversed()).toList();
    }

    public List<CrmUserDto> getCrmUsers(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        return userRepo.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(organisationId, SALES_ROLES)
                .stream().map(this::toCrmUserDto).toList();
    }

    public List<CrmUserDto> getOpportunityTeamUsers(Long requestedOrganisationId) {
        Long organisationId = tenant(requestedOrganisationId);
        return userRepo.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(organisationId, OPPORTUNITY_TEAM_ROLES)
                .stream().map(this::toCrmUserDto).toList();
    }

    private CrmUserDto toCrmUserDto(AppUser user) {
        return new CrmUserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public CrmManagerViewDto getManagerView(Long requestedOrganisationId, int year) {
        Long organisationId = tenant(requestedOrganisationId);
        List<AppUser> team = userRepo.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(organisationId, SALES_ROLES);
        List<CrmOpportunity> opportunities = opportunityRepo.findByOrganisation_IdOrderByCreatedAtDesc(organisationId);
        Map<Long, CrmSalesTarget> targets = targetRepo.findByOrganisation_IdAndTargetYear(organisationId, year).stream()
                .collect(Collectors.toMap(item -> item.getUser().getId(), Function.identity()));
        List<CrmManagerViewDto.TeamMember> members = team.stream().map(user -> {
            List<CrmOpportunity> owned = opportunities.stream().filter(item -> item.getOwner() != null && Objects.equals(item.getOwner().getId(), user.getId())).toList();
            CrmSalesTarget target = targets.get(user.getId());
            return new CrmManagerViewDto.TeamMember(user.getId(), user.getFullName(), user.getRole().name(), owned.size(),
                    owned.stream().filter(item -> !item.isLost()).map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add),
                    owned.stream().filter(CrmOpportunity::isWon).map(item -> zero(item.getValue())).reduce(BigDecimal.ZERO, BigDecimal::add),
                    target == null ? BigDecimal.ZERO : target.getAmount(), target == null ? "EUR" : target.getCurrency());
        }).toList();
        return new CrmManagerViewDto(members, opportunities.stream().map(this::toOpportunityDto).toList(), year);
    }

    public CrmManagerViewDto.TeamMember saveTarget(Long requestedOrganisationId, Long userId, int year, BigDecimal amount, String currency) {
        Organisation organisation = organisation(requestedOrganisationId);
        AppUser user = tenantUser(organisation.getId(), userId);
        if (user == null || !SALES_ROLES.contains(user.getRole())) throw badRequest("Targets can only be assigned to CRM sales users.");
        CrmSalesTarget target = targetRepo.findByOrganisation_IdAndUser_IdAndTargetYear(organisation.getId(), userId, year)
                .orElseGet(CrmSalesTarget::new);
        target.setOrganisation(organisation); target.setUser(user); target.setTargetYear(year);
        target.setAmount(nonNegative(amount, "Target")); target.setCurrency(blank(currency) == null ? "EUR" : currency.toUpperCase(Locale.ROOT));
        targetRepo.save(target);
        return getManagerView(organisation.getId(), year).team().stream().filter(item -> Objects.equals(item.userId(), userId)).findFirst().orElseThrow();
    }

    private void logActivity(Organisation organisation, AppUser user, CrmActivity.ActivityType type, String description,
                             CrmLead lead, CrmOpportunity opportunity) {
        CrmActivity item = CrmActivity.of(organisation, user, type, description); item.setLead(lead); item.setOpportunity(opportunity); activityRepo.save(item);
    }

    private CrmActivityDto toActivityDto(CrmActivity item) {
        CrmActivityDto dto = new CrmActivityDto(); dto.setId(item.getId()); dto.setActivityType(item.getActivityType().name());
        dto.setDescription(item.getDescription()); dto.setCreatedAt(item.getCreatedAt()); dto.setMetadata(item.getMetadata());
        if (item.getUser() != null) { dto.setUserId(item.getUser().getId()); dto.setUserName(item.getUser().getFullName()); }
        if (item.getLead() != null) dto.setLeadId(item.getLead().getId());
        if (item.getOpportunity() != null) dto.setOpportunityId(item.getOpportunity().getId());
        return dto;
    }

    private CrmOpportunityNoteDto toNoteDto(CrmOpportunityNote note) {
        return new CrmOpportunityNoteDto(note.getId(), note.getAuthor().getId(), note.getAuthor().getFullName(), note.getContent(), note.getCreatedAt(), note.getUpdatedAt());
    }
    private CrmOpportunityAttachmentDto toAttachmentDto(CrmOpportunityAttachment attachment) {
        return new CrmOpportunityAttachmentDto(attachment.getId(), attachment.getOriginalFileName(), attachment.getContentType(),
                attachment.getFileSize(), attachment.getUploadedBy().getId(), attachment.getUploadedBy().getFullName(), attachment.getUploadedAt());
    }
    public record AttachmentDownload(Resource resource, String fileName, String contentType) {}

    private String extension(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf"; case "image/png" -> ".png"; case "image/jpeg" -> ".jpg";
            case "text/plain" -> ".txt"; case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ".xlsx";
            default -> throw badRequest("Unsupported attachment type.");
        };
    }
    private String sanitizeFileName(String value) {
        String name = Optional.ofNullable(value).orElse("attachment").replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1); return name.replaceAll("[^a-zA-Z0-9._ -]", "_");
    }
    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String label) {
        try { return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception exception) { throw badRequest("Invalid " + label + "."); }
    }
    private int clamp(int value) { return Math.max(0, Math.min(100, value)); }
    private int probability(Integer value) {
        int probability = value == null ? 0 : value;
        if (probability < 0 || probability > 100) throw badRequest("Probability must be between 0 and 100.");
        return probability;
    }
    private BigDecimal sum(BigDecimal left, BigDecimal right) { return zero(left).add(zero(right)); }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private BigDecimal nonNegative(BigDecimal value, String label) {
        if (value != null && value.signum() < 0) throw badRequest(label + " cannot be negative."); return value;
    }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String defaultValue(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String string(Object value) { return value == null ? null : value.toString(); }
    private void requireText(String value, String message) { if (value == null || value.isBlank()) throw badRequest(message); }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException forbidden(String message) { return new ResponseStatusException(HttpStatus.FORBIDDEN, message); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
