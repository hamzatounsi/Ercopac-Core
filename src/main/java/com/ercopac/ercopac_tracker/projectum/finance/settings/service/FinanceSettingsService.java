package com.ercopac.ercopac_tracker.projectum.finance.settings.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceWbsHierarchy;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsRowType;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.PaymentMilestone; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.*; // ✅ Inclut les nouveaux DTOs Payment
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceWbsTemplateRowRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.PaymentMilestoneRepository; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.ForecastBaseline;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.ForecastBaselineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;

/**
 * Finance Settings hold the single source of truth for a project's financial WBS. Every structural change
 * (import, add, rename, move, delete) goes through {@link #saveSettings} and is then materialised into the
 * Finance rows that Finance and Forecast both read.
 */
@Service
@Transactional
public class FinanceSettingsService {

    private static final int MAX_REPORTED_ROWS = 20;

    private final FinanceSettingsRepository financeSettingsRepository;
    private final FinanceWbsTemplateRowRepository templateRowRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final ForecastEntryRepository forecastEntryRepository;
    private final ProjectAccessService projectAccessService;
    private final FinanceService financeService;
    private final OrganisationRepository organisationRepository;
    private final SecurityUtils securityUtils;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final PaymentMilestoneRepository paymentMilestoneRepository; // ✅ NOUVEAU
    private final ForecastBaselineRepository forecastBaselineRepository;
    public FinanceSettingsService(
            FinanceSettingsRepository financeSettingsRepository,
            FinanceWbsTemplateRowRepository templateRowRepository,
            FinanceEntryRepository financeEntryRepository,
            ForecastEntryRepository forecastEntryRepository,
            ProjectAccessService projectAccessService,
            FinanceService financeService,
            OrganisationRepository organisationRepository,
            SecurityUtils securityUtils,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            ResourceTypeRepository resourceTypeRepository,
            PaymentMilestoneRepository paymentMilestoneRepository,
            ForecastBaselineRepository forecastBaselineRepository// ✅ NOUVEAU
    ) {
        this.financeSettingsRepository = financeSettingsRepository;
        this.templateRowRepository = templateRowRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.forecastEntryRepository = forecastEntryRepository;
        this.projectAccessService = projectAccessService;
        this.financeService = financeService;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.paymentMilestoneRepository = paymentMilestoneRepository;
        this.forecastBaselineRepository = forecastBaselineRepository; // ✅ NOUVEAU
    }

    @Transactional(readOnly = true)
    public FinanceSettingsDto getSettings(Long projectId) {
        Long orgId = requireOrganisationId();
        if (projectId != null) requireProject(projectId, orgId);

        FinanceSettings settings = financeSettingsRepository.findByOrganisationId(orgId)
                .orElseGet(() -> {
                    FinanceSettings s = new FinanceSettings();
                    s.setDefaultHourlyRate(BigDecimal.valueOf(65));
                    return s;
                });

        FinanceSettingsDto dto = new FinanceSettingsDto();
        dto.setDefaultHourlyRate(settings.getDefaultHourlyRate());

        List<FinanceWbsTemplateRow> rows;
        if (projectId != null) {
            rows = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);
        } else {
            rows = templateRowRepository.findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(orgId);
        }

        dto.setTemplateRows(rows.stream().map(this::toDto).toList());
        return dto;
    }

    public FinanceSettingsDto saveSettings(Long projectId, SaveFinanceSettingsRequest request) {
        return saveSettings(projectId, request, false, false);
    }

    private FinanceSettingsDto saveSettings(Long projectId, SaveFinanceSettingsRequest request,
            boolean preserveUnmatched, boolean pruneUnmanaged) {
        Long orgId = requireOrganisationId();
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        Project project = null;
        List<FinanceWbsTemplateRow> oldRows;
        if (projectId != null) {
            project = requireProject(projectId, orgId);
            oldRows = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);
        } else {
            oldRows = templateRowRepository.findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(orgId);
        }

        Map<Long, FinanceWbsTemplateRow> oldById = oldRows.stream()
                .collect(Collectors.toMap(FinanceWbsTemplateRow::getId, row -> row));
        List<FinanceWbsTemplateRowDto> requested = request.getTemplateRows() == null ? List.of() : request.getTemplateRows();
        Set<String> codes = new HashSet<>();
        Set<Long> retainedIds = new HashSet<>();
        if (projectId != null && !oldRows.isEmpty() && requested.isEmpty()
                && !financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId).isEmpty())
            throw new IllegalArgumentException("Cannot remove the entire WBS while Finance rows exist");
        int previousLevel = 0;
        for (int i = 0; i < requested.size(); i++) {
            FinanceWbsTemplateRowDto dto = requested.get(i);
            String label = "Row " + (i + 1) + (dto.getCodeTemplate() == null ? "" : " (" + dto.getCodeTemplate() + ")");
            if (dto.getCodeTemplate() == null || dto.getCodeTemplate().isBlank() || dto.getDescription() == null || dto.getDescription().isBlank() || dto.getType() == null)
                throw new IllegalArgumentException(label + ": each WBS row needs a code, description and type");
            if (!codes.add(dto.getCodeTemplate().trim().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException(label + ": duplicate WBS code " + dto.getCodeTemplate());
            if (dto.getLevel() == null || dto.getLevel() < 1 || dto.getLevel() > previousLevel + 1)
                throw new IllegalArgumentException(label + ": level " + dto.getLevel() + " cannot follow level " + previousLevel
                        + " (a node can only be one level below its parent)");
            previousLevel = dto.getLevel();
            if (dto.getId() != null) {
                if (!oldById.containsKey(dto.getId()) || !retainedIds.add(dto.getId()))
                    throw new IllegalArgumentException(label + ": WBS row does not belong to this project");
            }
        }

        if (project != null) {
            linkLegacyFinanceRows(project, oldRows);
            assertStructureKeepsData(project, oldRows, requested, retainedIds);
        }

        FinanceSettings settings = financeSettingsRepository.findByOrganisationId(orgId)
                .orElseGet(() -> {
                    FinanceSettings s = new FinanceSettings();
                    s.setOrganisation(organisation);
                    return s;
                });
        settings.setDefaultHourlyRate(nvl(request.getDefaultHourlyRate(), BigDecimal.valueOf(65)));
        financeSettingsRepository.save(settings);

        for (FinanceWbsTemplateRowDto dto : requested) {
            FinanceWbsTemplateRow row = dto.getId() == null ? new FinanceWbsTemplateRow() : oldById.get(dto.getId());
            row.setOrganisation(organisation);
            row.setProject(project);
            row.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
            row.setLevel(dto.getLevel());
            row.setCodeTemplate(dto.getCodeTemplate());
            row.setDescription(dto.getDescription());
            row.setType(dto.getType());

            if (dto.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .filter(candidate -> candidate.getOrganisation() != null && orgId.equals(candidate.getOrganisation().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + dto.getDepartmentId()));
                row.setDepartment(dept);
            } else {
                row.setDepartment(null);
            }

            if (dto.getResourceType() != null && !dto.getResourceType().isBlank()) {
                String resourceTypeCode = extractResourceTypeCode(dto.getResourceType());
                row.setResourceType(resourceTypeCode);
            } else {
                row.setResourceType(null);
            }

            if (dto.getOwnerId() != null) {
                AppUser owner = userRepository.findById(dto.getOwnerId())
                    .filter(candidate -> candidate.getOrganisation() != null && orgId.equals(candidate.getOrganisation().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found with ID: " + dto.getOwnerId()));

                if (row.getDepartment() != null && owner.getDepartment() != null) {
                    if (!owner.getDepartment().getId().equals(row.getDepartment().getId())) {
                        throw new IllegalArgumentException("Selected owner '" + owner.getFullName() + "' does not belong to the selected department.");
                    }
                } else if (row.getDepartment() == null) {
                    throw new IllegalArgumentException("A department must be selected before assigning an owner.");
                }

                row.setOwner(owner);
                row.setOwnerKey(owner.getFullName());
            } else {
                row.setOwner(null);
                row.setOwnerKey(blankToNull(dto.getOwnerKey()));
            }

            row.setHourRate(dto.getHourRate());
            templateRowRepository.save(row);
        }

        for (FinanceWbsTemplateRow old : oldRows) if (!retainedIds.contains(old.getId())) templateRowRepository.delete(old);
        if (projectId != null && !requested.isEmpty())
            applyTemplate(projectId, new ApplyFinanceTemplateRequest(), preserveUnmatched, pruneUnmanaged);

        return getSettings(projectId);
    }

    /** Links Finance rows created before template_row_id existed to their template row by WBS code. */
    private void linkLegacyFinanceRows(Project project, List<FinanceWbsTemplateRow> oldRows) {
        Map<String, FinanceEntry> financeByCode = financeService.loadWbsRows(project)
                .stream().collect(Collectors.toMap(FinanceEntry::getWbsCode, row -> row, (a, b) -> a));
        for (FinanceWbsTemplateRow old : oldRows) {
            FinanceEntry finance = financeByCode.get(buildFinalWbsCode(old.getCodeTemplate(), project.getCode()));
            if (finance != null && finance.getTemplateRowId() == null) finance.setTemplateRowId(old.getId());
        }
    }

    /**
     * Blocks any structure change that would lose or hide financial data: deleting a node that holds data or
     * still has children, or putting children under a node that holds data (parent values are calculated,
     * so its own values would silently disappear from every total).
     */
    private void assertStructureKeepsData(Project project, List<FinanceWbsTemplateRow> oldRows,
                                          List<FinanceWbsTemplateRowDto> requested, Set<Long> retainedIds) {
        NodeState state = nodeState(project);
        List<String> problems = new ArrayList<>();
        for (FinanceWbsTemplateRow old : oldRows) {
            if (retainedIds.contains(old.getId())) continue;
            FinanceEntry entry = state.entryByTemplate.get(old.getId());
            if (entry == null) continue;
            String code = entry.getWbsCode();
            List<String> data = state.dataByEntry.getOrDefault(entry.getId(), List.of());
            if (!data.isEmpty()) problems.add("Cannot delete WBS node " + code + " because it holds " + String.join(", ", data));
            boolean keepsChildren = state.parents.entrySet().stream()
                    .filter(link -> entry.getId().equals(link.getValue()))
                    .map(link -> state.entryById.get(link.getKey()))
                    .anyMatch(child -> child.getTemplateRowId() == null || retainedIds.contains(child.getTemplateRowId()));
            if (keepsChildren) problems.add("Cannot delete WBS node " + code + " because it has child nodes; delete or move its children first");
        }
        for (int i = 0; i < requested.size(); i++) {
            FinanceWbsTemplateRowDto dto = requested.get(i);
            boolean getsChildren = i + 1 < requested.size() && requested.get(i + 1).getLevel() > dto.getLevel();
            FinanceEntry entry = dto.getId() == null ? null : state.entryByTemplate.get(dto.getId());
            if (!getsChildren || entry == null || state.parents.containsValue(entry.getId())) continue;
            List<String> data = state.dataByEntry.getOrDefault(entry.getId(), List.of());
            if (!data.isEmpty()) problems.add("WBS node " + entry.getWbsCode() + " holds " + String.join(", ", data)
                    + " and must stay a leaf; it cannot receive child nodes");
        }
        if (!problems.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nothing was saved. " + limit(problems) + ".");
    }

    private NodeState nodeState(Project project) {
        List<FinanceEntry> entries = financeService.loadWbsRows(project);
        Map<Long, List<ForecastEntry>> forecasts = financeService.forecastEntriesByRow(project, entries);
        NodeState state = new NodeState();
        state.parents = FinanceWbsHierarchy.parents(entries);
        for (FinanceEntry entry : entries) {
            state.entryById.put(entry.getId(), entry);
            if (entry.getTemplateRowId() != null) state.entryByTemplate.putIfAbsent(entry.getTemplateRowId(), entry);
            List<String> data = financeService.dataDescription(entry);
            if (forecasts.containsKey(entry.getId())) data.add("forecast entries");
            if (!data.isEmpty()) state.dataByEntry.put(entry.getId(), data);
        }
        return state;
    }

    private static class NodeState {
        Map<Long, Long> parents = new HashMap<>();
        Map<Long, FinanceEntry> entryById = new HashMap<>();
        Map<Long, FinanceEntry> entryByTemplate = new HashMap<>();
        Map<Long, List<String>> dataByEntry = new HashMap<>();
    }

 // ==========================================
 // ✅ FORECAST BASELINE - MÉTHODES
 // ==========================================

 @Transactional(readOnly = true)
 public ForecastBaselineDto getForecastBaseline(Long projectId) {
     Long orgId = requireOrganisationId();
     Project project = requireProject(projectId, orgId);

     ForecastBaselineDto dto = new ForecastBaselineDto();
     dto.setProjectId(projectId);
     dto.setProjectName(project.getName());

     List<ForecastBaseline> baselines = forecastBaselineRepository
             .findAllByOrganisationIdAndProjectIdOrderByWbsCodeAsc(orgId, projectId);

     dto.setHasBaseline(!baselines.isEmpty());

     if (!baselines.isEmpty()) {
         dto.setBaselineDate(baselines.get(0).getBaselineDate());
     }

     Map<String, ForecastBaseline> baselineByWbs = baselines.stream()
             .collect(Collectors.toMap(ForecastBaseline::getWbsCode, b -> b, (a, b) -> a));

     BigDecimal totalBaseline = BigDecimal.ZERO;
     BigDecimal totalCurrent = BigDecimal.ZERO;

     // The calculated Finance rows (leaf forecasts rolled up) are the current forecast
     for (FinanceEntryDto entry : financeService.getProjectFinance(projectId)) {
         ForecastBaselineRowDto row = new ForecastBaselineRowDto();
         row.setWbsCode(entry.getWbsCode());
         row.setDescription(entry.getDescription());
         row.setLevel(entry.getLevel());
         row.setCurrentForecast(nvl(entry.getForecast()));

         ForecastBaseline baseline = baselineByWbs.get(entry.getWbsCode());
         if (baseline != null) {
             row.setBaselineForecast(baseline.getBaselineForecast());
             row.setBaselineDate(baseline.getBaselineDate());
         } else {
             row.setBaselineForecast(BigDecimal.ZERO);
         }

         // Project totals count leaves only, so parent rows are not added twice
         if (entry.isLeaf()) {
             totalBaseline = totalBaseline.add(nvl(row.getBaselineForecast()));
             totalCurrent = totalCurrent.add(row.getCurrentForecast());
         }

         BigDecimal variance = row.getCurrentForecast().subtract(nvl(row.getBaselineForecast()));
         row.setVariance(variance);

         if (row.getBaselineForecast() != null && row.getBaselineForecast().compareTo(BigDecimal.ZERO) != 0) {
             double variancePercent = variance.divide(row.getBaselineForecast(), 4, RoundingMode.HALF_UP)
                     .multiply(BigDecimal.valueOf(100)).doubleValue();
             row.setVariancePercent(variancePercent);
         } else {
             row.setVariancePercent(null);
         }

         dto.getRows().add(row);
     }

     dto.setTotalBaselineForecast(totalBaseline);
     dto.setTotalCurrentForecast(totalCurrent);
     dto.setTotalVariance(totalCurrent.subtract(totalBaseline));

     return dto;
 }

 public ForecastBaselineDto setForecastBaseline(Long projectId) {
     Long orgId = requireOrganisationId();
     Project project = requireProject(projectId, orgId);
     Organisation organisation = project.getOrganisation();

     // Supprimer l'ancienne baseline
     forecastBaselineRepository.deleteAllByProjectId(projectId);

     LocalDateTime now = LocalDateTime.now();

     // Créer une nouvelle baseline pour chaque ligne, avec la prévision calculée
     for (FinanceEntryDto entry : financeService.getProjectFinance(projectId)) {
         ForecastBaseline baseline = new ForecastBaseline();
         baseline.setOrganisation(organisation);
         baseline.setProject(project);
         baseline.setWbsCode(entry.getWbsCode());
         baseline.setDescription(entry.getDescription());
         baseline.setLevel(entry.getLevel());
         baseline.setBaselineForecast(nvl(entry.getForecast()));
         baseline.setBaselineDate(now);
         forecastBaselineRepository.save(baseline);
     }

     return getForecastBaseline(projectId);
 }

 public ForecastBaselineDto resetForecastBaseline(Long projectId) {
     requireProject(projectId, requireOrganisationId());
     forecastBaselineRepository.deleteAllByProjectId(projectId);
     return getForecastBaseline(projectId);
 }
    @Transactional
    public ApplyFinanceTemplateResultDto applyTemplate(Long projectId, ApplyFinanceTemplateRequest request) {
        return applyTemplate(projectId, request, false, true);
    }

    private ApplyFinanceTemplateResultDto applyTemplate(Long projectId, ApplyFinanceTemplateRequest request,
            boolean preserveUnmatched, boolean pruneUnmanaged) {
        Long orgId = requireOrganisationId();
        Project project = requireProject(projectId, orgId);

        List<FinanceWbsTemplateRow> templateRows = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);

        if (templateRows.isEmpty()) {
            throw new IllegalArgumentException("No finance WBS template configured for this specific project. Please define one first.");
        }

        List<FinanceEntry> existingRows = financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);
        Map<String, FinanceEntry> existingByWbs = existingRows.stream()
                .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e, (a, b) -> a));
        Map<Long, FinanceEntry> existingByTemplate = existingRows.stream()
                .filter(row -> row.getTemplateRowId() != null)
                .collect(Collectors.toMap(FinanceEntry::getTemplateRowId, row -> row, (a, b) -> a));
        Set<Long> templateIds = templateRows.stream().map(FinanceWbsTemplateRow::getId).collect(Collectors.toSet());
        Map<Long, List<ForecastEntry>> forecasts = financeService.forecastEntriesByRow(project, existingRows);

        Set<String> expectedWbsCodes = new HashSet<>();
        for (FinanceWbsTemplateRow template : templateRows) {
            expectedWbsCodes.add(buildFinalWbsCode(template.getCodeTemplate(), project.getCode()));
        }
        int unmanagedOffset = pruneUnmanaged ? 0 : existingRows.stream()
                .filter(row -> row.getTemplateRowId() == null && !expectedWbsCodes.contains(row.getWbsCode()))
                .map(FinanceEntry::getDisplayOrder).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0);

        for (FinanceEntry existing : existingRows) {
            boolean matched = expectedWbsCodes.contains(existing.getWbsCode())
                    || templateIds.contains(existing.getTemplateRowId());
            if (shouldPrune(existing, matched, preserveUnmatched, pruneUnmanaged)) {
                if (forecasts.containsKey(existing.getId()) || !financeService.dataDescription(existing).isEmpty())
                    throw new IllegalArgumentException("Cannot remove WBS " + existing.getWbsCode()
                            + " because it has financial or forecast entries. For an import, uncheck Replace existing to add rows, or reconcile this WBS in Finance.");
                financeEntryRepository.delete(existing);
            }
        }

        int generatedRows = 0;
        for (FinanceWbsTemplateRow template : templateRows) {
            String finalWbsCode = buildFinalWbsCode(template.getCodeTemplate(), project.getCode());

            FinanceEntry entry = existingByTemplate.getOrDefault(template.getId(), existingByWbs.get(finalWbsCode));
            boolean isNew = false;

            if (entry == null) {
                entry = new FinanceEntry();
                entry.setOrganisation(project.getOrganisation());
                entry.setProject(project);
                isNew = true;
            }

            String previousCode = entry.getWbsCode();
            entry.setWbsCode(finalWbsCode);
            entry.setTemplateRowId(template.getId());
            entry.setDescription(template.getDescription());
            entry.setLevel(template.getLevel());
            entry.setRowType(template.getType() == null ? null : template.getType().name());
            entry.setIsSummary("SUMMARY".equalsIgnoreCase(template.getType() != null ? template.getType().name() : ""));
            entry.setOwnerName(resolveOwnerDisplay(template.getOwnerKey()));

            String resourceTypeCode = null;
            if (template.getResourceType() != null && !template.getResourceType().isBlank()) {
                String rawValue = template.getResourceType().trim();
                List<ResourceType> allTypes = resourceTypeRepository.findByOrganisation_IdAndActiveTrue(orgId);
                ResourceType matched = allTypes.stream()
                    .filter(rt -> rt.getCode().equalsIgnoreCase(rawValue) || rt.getLabel().equalsIgnoreCase(rawValue))
                    .findFirst()
                    .orElse(null);

                if (matched != null) {
                    resourceTypeCode = matched.getCode();
                } else {
                    resourceTypeCode = extractResourceTypeCode(rawValue);
                }
            }

            entry.setResourceTypeCode(resourceTypeCode);
            entry.setHourRate(template.getHourRate());
            entry.setDisplayOrder(unmanagedOffset + template.getSortOrder());

            if (isNew) {
                entry.setSales(BigDecimal.ZERO);
                entry.setBudget(BigDecimal.ZERO);
                entry.setCommitment(BigDecimal.ZERO);
                entry.setActualCost(BigDecimal.ZERO);
                entry.setForecast(BigDecimal.ZERO);
            } else {
                entry.setSales(nvl(entry.getSales()));
                entry.setBudget(nvl(entry.getBudget()));
                entry.setCommitment(nvl(entry.getCommitment()));
                entry.setActualCost(nvl(entry.getActualCost()));
                entry.setForecast(nvl(entry.getForecast()));
            }

            financeEntryRepository.save(entry);
            // A rename keeps the row id; forecast entries keep pointing at it and only their code label changes
            if (previousCode != null && !previousCode.equals(finalWbsCode)) {
                for (ForecastEntry forecastEntry : forecasts.getOrDefault(entry.getId(), List.of())) {
                    forecastEntry.setFinanceEntry(entry);
                    forecastEntry.setWbsCode(finalWbsCode);
                    forecastEntryRepository.save(forecastEntry);
                }
            }
            generatedRows++;
        }

        Map<Long, FinanceEntry> byTemplate = financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId)
                .stream().filter(row -> row.getTemplateRowId() != null)
                .collect(Collectors.toMap(FinanceEntry::getTemplateRowId, row -> row, (a, b) -> a));
        Deque<FinanceEntry> ancestors = new ArrayDeque<>();
        for (FinanceWbsTemplateRow template : templateRows) {
            FinanceEntry entry = byTemplate.get(template.getId());
            while (!ancestors.isEmpty() && ancestors.peek().getLevel() >= entry.getLevel()) ancestors.pop();
            entry.setParentId(ancestors.isEmpty() ? null : ancestors.peek().getId());
            ancestors.push(entry);
        }

        ApplyFinanceTemplateResultDto result = new ApplyFinanceTemplateResultDto();
        result.setProjectsProcessed(1);
        result.setRowsGenerated(generatedRows);
        return result;
    }

    static boolean shouldPrune(FinanceEntry entry, boolean matched, boolean preserveUnmatched, boolean pruneUnmanaged) {
        return !matched && !preserveUnmatched && (pruneUnmanaged || entry.getTemplateRowId() != null);
    }

    /**
     * Imports a WBS from a spreadsheet. Rows are validated with their row number, codes already in the WBS are
     * matched (so importing the same file twice never creates duplicates), and the import stops with a report
     * instead of changing or removing nodes that already hold financial or forecast data.
     */
    public FinanceSettingsDto importWbsTemplate(Long projectId, ImportFinanceWbsTemplateRequest request) {
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("Import file contains no WBS rows.");
        }
        FinanceSettingsDto current = getSettings(projectId);
        Map<String, FinanceWbsTemplateRowDto> byCode = current.getTemplateRows().stream()
                .collect(Collectors.toMap(row -> row.getCodeTemplate().trim().toLowerCase(Locale.ROOT), row -> row, (a, b) -> a));

        List<String> errors = new ArrayList<>();
        Map<String, Integer> fileRowByCode = new HashMap<>();
        Map<FinanceWbsTemplateRowDto, Integer> sourceRow = new IdentityHashMap<>();
        for (int i = 0; i < request.getRows().size(); i++) {
            int rowNumber = i + 1;
            FinanceWbsTemplateRowDto row = request.getRows().get(i);
            if (row.getCodeTemplate() == null || row.getCodeTemplate().isBlank()) { errors.add("Row " + rowNumber + ": missing WBS code"); continue; }
            row.setCodeTemplate(row.getCodeTemplate().trim());
            String code = row.getCodeTemplate().toLowerCase(Locale.ROOT);
            Integer firstRow = fileRowByCode.putIfAbsent(code, rowNumber);
            if (firstRow != null) { errors.add("Row " + rowNumber + ": duplicate WBS code " + row.getCodeTemplate() + " (already on row " + firstRow + ")"); continue; }
            if (row.getDescription() == null || row.getDescription().isBlank()) errors.add("Row " + rowNumber + " (" + row.getCodeTemplate() + "): missing description");
            if (row.getType() == null) row.setType(FinanceWbsRowType.COST);
            if (row.getLevel() == null) row.setLevel(detectLevel(row.getCodeTemplate()));
            if (row.getLevel() < 1) errors.add("Row " + rowNumber + " (" + row.getCodeTemplate() + "): level must be 1 or more");
            FinanceWbsTemplateRowDto existing = byCode.get(code);
            row.setId(existing == null ? null : existing.getId());
            sourceRow.put(row, rowNumber);
        }
        if (!errors.isEmpty()) throw new IllegalArgumentException("Import rejected, nothing was saved. " + limit(errors));

        List<FinanceWbsTemplateRowDto> merged = new ArrayList<>();
        Set<Long> importedIds = request.getRows().stream().map(FinanceWbsTemplateRowDto::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!request.isReplaceExisting())
            current.getTemplateRows().stream().filter(row -> !importedIds.contains(row.getId())).forEach(merged::add);
        merged.addAll(request.getRows());
        int previousLevel = 0;
        for (int i = 0; i < merged.size(); i++) {
            FinanceWbsTemplateRowDto row = merged.get(i);
            row.setSortOrder(i + 1);
            if (row.getLevel() > previousLevel + 1) {
                Integer fileRow = sourceRow.get(row);
                errors.add((fileRow != null ? "Row " + fileRow : "Existing WBS") + " (" + row.getCodeTemplate() + "): level " + row.getLevel()
                        + " cannot follow level " + previousLevel + " (a node can only be one level below its parent)");
            }
            previousLevel = row.getLevel();
        }
        if (!errors.isEmpty()) throw new IllegalArgumentException("Import rejected, nothing was saved. " + limit(errors));

        if (projectId != null) assertImportKeepsData(requireProject(projectId, requireOrganisationId()), current.getTemplateRows(), merged, sourceRow);

        SaveFinanceSettingsRequest save = new SaveFinanceSettingsRequest();
        save.setDefaultHourlyRate(current.getDefaultHourlyRate());
        save.setTemplateRows(merged);
        return saveSettings(projectId, save, !request.isReplaceExisting(), request.isReplaceExisting());
    }

    /** Reports every node holding data that the import would change, move or remove, instead of overwriting it. */
    private void assertImportKeepsData(Project project, List<FinanceWbsTemplateRowDto> currentRows,
                                       List<FinanceWbsTemplateRowDto> merged, Map<FinanceWbsTemplateRowDto, Integer> sourceRow) {
        NodeState state = nodeState(project);
        Map<Long, FinanceWbsTemplateRowDto> currentById = currentRows.stream()
                .collect(Collectors.toMap(FinanceWbsTemplateRowDto::getId, row -> row));
        Map<Long, Long> currentParents = templateParents(currentRows);
        Map<Long, Long> newParents = templateParents(merged);
        Set<Long> mergedIds = merged.stream().map(FinanceWbsTemplateRowDto::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<String> conflicts = new ArrayList<>();
        for (FinanceWbsTemplateRowDto row : merged) {
            if (row.getId() == null || !sourceRow.containsKey(row)) continue;
            FinanceEntry entry = state.entryByTemplate.get(row.getId());
            List<String> data = entry == null ? List.of() : state.dataByEntry.getOrDefault(entry.getId(), List.of());
            if (data.isEmpty()) continue;
            FinanceWbsTemplateRowDto before = currentById.get(row.getId());
            List<String> changes = new ArrayList<>();
            if (!Objects.equals(before.getLevel(), row.getLevel())) changes.add("level " + before.getLevel() + " -> " + row.getLevel());
            if (before.getType() != row.getType()) changes.add("type " + before.getType() + " -> " + row.getType());
            if (!Objects.equals(trim(before.getDescription()), trim(row.getDescription()))) changes.add("description");
            if (!Objects.equals(currentParents.get(row.getId()), newParents.get(row.getId()))) changes.add("parent");
            if (!changes.isEmpty()) conflicts.add("Row " + sourceRow.get(row) + " (" + row.getCodeTemplate() + ") holds "
                    + String.join(", ", data) + "; the import would change its " + String.join(", ", changes));
        }
        for (FinanceWbsTemplateRowDto row : currentRows) {
            if (mergedIds.contains(row.getId())) continue;
            FinanceEntry entry = state.entryByTemplate.get(row.getId());
            List<String> data = entry == null ? List.of() : state.dataByEntry.getOrDefault(entry.getId(), List.of());
            if (!data.isEmpty()) conflicts.add("WBS " + row.getCodeTemplate() + " holds " + String.join(", ", data) + " and would be removed");
        }
        if (!conflicts.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Import stopped, nothing was saved. These WBS nodes already hold data: " + limit(conflicts)
                            + ". Edit them in the WBS Structure tab or remove them from the file.");
    }

    /** Parent template id of every existing row in an ordered level list (null for roots and new parents). */
    private Map<Long, Long> templateParents(List<FinanceWbsTemplateRowDto> rows) {
        Map<Long, Long> parents = new HashMap<>();
        Deque<FinanceWbsTemplateRowDto> stack = new ArrayDeque<>();
        for (FinanceWbsTemplateRowDto row : rows) {
            while (!stack.isEmpty() && stack.peek().getLevel() >= row.getLevel()) stack.pop();
            if (row.getId() != null) parents.put(row.getId(), stack.isEmpty() ? null : stack.peek().getId());
            stack.push(row);
        }
        return parents;
    }

    // ==========================================
    // ✅ NOUVEAUX : Méthodes pour les Payments
    // ==========================================

    @Transactional(readOnly = true)
    public PaymentScheduleDto getPaymentSchedule(Long projectId) {
        Long orgId = requireOrganisationId();
        Project project = requireProject(projectId, orgId);

        List<PaymentMilestone> milestones = paymentMilestoneRepository
                .findAllByOrganisationIdAndProjectIdOrderByMilestoneOrderAsc(orgId, projectId);

        PaymentScheduleDto dto = new PaymentScheduleDto();
        dto.setProjectId(projectId);
        dto.setProjectName(project.getName());

        if (!milestones.isEmpty()) {
            PaymentMilestone first = milestones.get(0);
            dto.setSalesValue(first.getSalesValue());
            dto.setCurrency(first.getCurrency());
        }

        int totalPercentage = milestones.stream()
                .mapToInt(m -> m.getPercentage() != null ? m.getPercentage() : 0)
                .sum();
        dto.setTotalPercentage(totalPercentage);

        dto.setMilestones(milestones.stream().map(this::toPaymentDto).collect(Collectors.toList()));
        return dto;
    }

    public PaymentScheduleDto savePaymentSchedule(Long projectId, SavePaymentScheduleRequest request) {
        Long orgId = requireOrganisationId();
        Project project = requireProject(projectId, orgId);
        Organisation organisation = project.getOrganisation();

        int totalPercentage = request.getMilestones().stream()
                .mapToInt(m -> m.getPercentage() != null ? m.getPercentage() : 0)
                .sum();

        if (totalPercentage != 100) {
            throw new IllegalArgumentException("Payment percentages must sum to exactly 100%. Current total: " + totalPercentage + "%");
        }

        paymentMilestoneRepository.deleteAllByProjectId(projectId);

        int order = 1;
        for (PaymentMilestoneDto milestoneDto : request.getMilestones()) {
            PaymentMilestone milestone = new PaymentMilestone();
            milestone.setOrganisation(organisation);
            milestone.setProject(project);
            milestone.setSalesValue(request.getSalesValue());
            milestone.setCurrency(request.getCurrency());
            milestone.setMilestoneOrder(order++);
            milestone.setPercentage(milestoneDto.getPercentage());
            milestone.setDueDate(milestoneDto.getDueDate());
            milestone.setStatus(milestoneDto.getStatus() != null ? milestoneDto.getStatus() : "EXPECTED");
            paymentMilestoneRepository.save(milestone);
        }

        return getPaymentSchedule(projectId);
    }

    private PaymentMilestoneDto toPaymentDto(PaymentMilestone milestone) {
        PaymentMilestoneDto dto = new PaymentMilestoneDto();
        dto.setId(milestone.getId());
        dto.setMilestoneOrder(milestone.getMilestoneOrder());
        dto.setPercentage(milestone.getPercentage());
        dto.setDueDate(milestone.getDueDate());
        dto.setStatus(milestone.getStatus());

        if (milestone.getSalesValue() != null && milestone.getPercentage() != null) {
            BigDecimal calculatedValue = milestone.getSalesValue()
                    .multiply(BigDecimal.valueOf(milestone.getPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            dto.setCalculatedValue(calculatedValue);
        }
        return dto;
    }

    // ==========================================
    // ✅ Méthodes utilitaires existantes
    // ==========================================

    private String extractResourceTypeCode(String resourceTypeDisplay) {
        if (resourceTypeDisplay == null || resourceTypeDisplay.isBlank()) {
            return null;
        }
        if (resourceTypeDisplay.contains("(")) {
            int startIndex = resourceTypeDisplay.indexOf("(") + 1;
            int endIndex = resourceTypeDisplay.indexOf(")");
            if (endIndex > startIndex) {
                return resourceTypeDisplay.substring(startIndex, endIndex).trim();
            }
        }
        return resourceTypeDisplay.trim();
    }

    private FinanceWbsTemplateRowDto toDto(FinanceWbsTemplateRow row) {
        FinanceWbsTemplateRowDto dto = new FinanceWbsTemplateRowDto();
        dto.setId(row.getId());
        dto.setSortOrder(row.getSortOrder());
        dto.setLevel(row.getLevel());
        dto.setCodeTemplate(row.getCodeTemplate());
        dto.setDescription(row.getDescription());
        dto.setType(row.getType());

        if (row.getDepartment() != null) {
            dto.setDepartmentId(row.getDepartment().getId());
            dto.setDepartmentName(row.getDepartment().getLabel());
        }
        if (row.getOwner() != null) {
            dto.setOwnerId(row.getOwner().getId());
            dto.setOwnerName(row.getOwner().getFullName());
        }
        dto.setOwnerKey(row.getOwnerKey());
        dto.setResourceType(row.getResourceType());
        dto.setHourRate(row.getHourRate());
        return dto;
    }

    private String buildFinalWbsCode(String codeTemplate, String projectCode) {
        if (codeTemplate == null || codeTemplate.isBlank()) throw new IllegalArgumentException("Template WBS code cannot be blank");
        if (projectCode == null || projectCode.isBlank()) throw new IllegalArgumentException("Project code cannot be blank");
        return codeTemplate.replace("xxx25", projectCode.trim());
    }

    private String resolveOwnerDisplay(String ownerKey) {
        return (ownerKey == null || ownerKey.isBlank()) ? "—" : ownerKey;
    }

    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private BigDecimal nvl(BigDecimal value, BigDecimal fallback) { return value == null ? fallback : value; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String trim(String value) { return value == null ? null : value.trim(); }

    private static String limit(List<String> messages) {
        String joined = String.join("; ", messages.subList(0, Math.min(MAX_REPORTED_ROWS, messages.size())));
        return messages.size() > MAX_REPORTED_ROWS ? joined + "; and " + (messages.size() - MAX_REPORTED_ROWS) + " more" : joined;
    }

    private Long requireOrganisationId() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) throw new IllegalStateException("User has no organisation");
        return orgId;
    }

    /** Project access goes through ProjectAccessService (tenant and project-manager scope) and must match the tenant. */
    private Project requireProject(Long projectId, Long orgId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        if (project.getOrganisation() == null || !orgId.equals(project.getOrganisation().getId()))
            throw new IllegalArgumentException("Project not accessible");
        return project;
    }

    private int detectLevel(String code) {
        if (code == null || code.isBlank()) return 1;
        String cleaned = code.trim();
        if (cleaned.contains(".")) return Math.max(1, cleaned.split("\\.").length);
        if (cleaned.contains("-")) return Math.max(1, cleaned.split("-").length - 1);
        return 1;
    }
}
