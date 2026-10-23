package com.ercopac.ercopac_tracker.projectum.finance.settings.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.PaymentMilestone; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.*; // ✅ Inclut les nouveaux DTOs Payment
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceWbsTemplateRowRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.PaymentMilestoneRepository; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.ForecastBaseline;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.ForecastBaselineRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;

@Service
@Transactional
public class FinanceSettingsService {

    private final FinanceSettingsRepository financeSettingsRepository;
    private final FinanceWbsTemplateRowRepository templateRowRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final ProjectRepository projectRepository;
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
            ProjectRepository projectRepository,
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
        this.projectRepository = projectRepository;
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
        Long orgId = requireOrganisationId();
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        FinanceSettings settings = financeSettingsRepository.findByOrganisationId(orgId)
                .orElseGet(() -> {
                    FinanceSettings s = new FinanceSettings();
                    s.setOrganisation(organisation);
                    return s;
                });

        settings.setDefaultHourlyRate(nvl(request.getDefaultHourlyRate(), BigDecimal.valueOf(65)));
        financeSettingsRepository.save(settings);

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
            templateRowRepository.deleteAllByProjectId(projectId);
        } else {
            templateRowRepository.deleteAllByOrganisationId(orgId);
        }

        for (FinanceWbsTemplateRowDto dto : request.getTemplateRows()) {
            FinanceWbsTemplateRow row = new FinanceWbsTemplateRow();
            row.setOrganisation(organisation);
            row.setProject(project);
            row.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
            row.setLevel(dto.getLevel());
            row.setCodeTemplate(dto.getCodeTemplate());
            row.setDescription(dto.getDescription());
            row.setType(dto.getType());
            
            if (dto.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + dto.getDepartmentId()));
                row.setDepartment(dept);
            }
            
            if (dto.getResourceType() != null && !dto.getResourceType().isBlank()) {
                String resourceTypeCode = extractResourceTypeCode(dto.getResourceType());
                row.setResourceType(resourceTypeCode);
            } else {
                row.setResourceType(null);
            }
            
            if (dto.getOwnerId() != null) {
                AppUser owner = userRepository.findById(dto.getOwnerId())
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
                row.setOwnerKey(blankToNull(dto.getOwnerKey()));
            }
            
            row.setHourRate(dto.getHourRate());
            templateRowRepository.save(row);
        }

        return getSettings(projectId);
    }
 // ==========================================
 // ✅ FORECAST BASELINE - MÉTHODES
 // ==========================================

 @Transactional(readOnly = true)
 public ForecastBaselineDto getForecastBaseline(Long projectId) {
     Long orgId = requireOrganisationId();
     Project project = projectRepository.findById(projectId)
             .orElseThrow(() -> new IllegalArgumentException("Project not found"));

     ForecastBaselineDto dto = new ForecastBaselineDto();
     dto.setProjectId(projectId);
     dto.setProjectName(project.getName());

     List<ForecastBaseline> baselines = forecastBaselineRepository
             .findAllByOrganisationIdAndProjectIdOrderByWbsCodeAsc(orgId, projectId);

     dto.setHasBaseline(!baselines.isEmpty());

     if (!baselines.isEmpty()) {
         dto.setBaselineDate(baselines.get(0).getBaselineDate());
     }

     // Récupérer les finance entries actuelles
     List<FinanceEntry> currentEntries = financeEntryRepository
             .findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);

     Map<String, FinanceEntry> currentByWbs = currentEntries.stream()
             .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e, (a, b) -> a));

     Map<String, ForecastBaseline> baselineByWbs = baselines.stream()
             .collect(Collectors.toMap(ForecastBaseline::getWbsCode, b -> b, (a, b) -> a));

     BigDecimal totalBaseline = BigDecimal.ZERO;
     BigDecimal totalCurrent = BigDecimal.ZERO;

     // Utiliser les entries courantes comme source de vérité pour les lignes
     for (FinanceEntry entry : currentEntries) {
         ForecastBaselineRowDto row = new ForecastBaselineRowDto();
         row.setWbsCode(entry.getWbsCode());
         row.setDescription(entry.getDescription());
         row.setLevel(entry.getLevel());
         row.setCurrentForecast(entry.getForecast() != null ? entry.getForecast() : BigDecimal.ZERO);

         ForecastBaseline baseline = baselineByWbs.get(entry.getWbsCode());
         if (baseline != null) {
             row.setBaselineForecast(baseline.getBaselineForecast());
             row.setBaselineDate(baseline.getBaselineDate());
             totalBaseline = totalBaseline.add(baseline.getBaselineForecast());
         } else {
             row.setBaselineForecast(BigDecimal.ZERO);
         }

         BigDecimal current = row.getCurrentForecast();
         totalCurrent = totalCurrent.add(current);

         BigDecimal variance = current.subtract(row.getBaselineForecast());
         row.setVariance(variance);

         if (row.getBaselineForecast() != null && row.getBaselineForecast().compareTo(BigDecimal.ZERO) != 0) {
             double variancePercent = variance.divide(row.getBaselineForecast(), 4, BigDecimal.ROUND_HALF_UP)
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
     Organisation organisation = organisationRepository.findById(orgId)
             .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));
     Project project = projectRepository.findById(projectId)
             .orElseThrow(() -> new IllegalArgumentException("Project not found"));

     // Supprimer l'ancienne baseline
     forecastBaselineRepository.deleteAllByProjectId(projectId);

     // Récupérer les finance entries actuelles
     List<FinanceEntry> currentEntries = financeEntryRepository
             .findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);

     LocalDateTime now = LocalDateTime.now();

     // Créer une nouvelle baseline pour chaque ligne
     for (FinanceEntry entry : currentEntries) {
         ForecastBaseline baseline = new ForecastBaseline();
         baseline.setOrganisation(organisation);
         baseline.setProject(project);
         baseline.setWbsCode(entry.getWbsCode());
         baseline.setDescription(entry.getDescription());
         baseline.setLevel(entry.getLevel());
         baseline.setBaselineForecast(entry.getForecast() != null ? entry.getForecast() : BigDecimal.ZERO);
         baseline.setBaselineDate(now);
         forecastBaselineRepository.save(baseline);
     }

     return getForecastBaseline(projectId);
 }

 public ForecastBaselineDto resetForecastBaseline(Long projectId) {
     forecastBaselineRepository.deleteAllByProjectId(projectId);
     return getForecastBaseline(projectId);
 }
    @Transactional
    public ApplyFinanceTemplateResultDto applyTemplate(Long projectId, ApplyFinanceTemplateRequest request) {
        Long orgId = requireOrganisationId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<FinanceWbsTemplateRow> templateRows = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);
        
        if (templateRows.isEmpty()) {
            throw new IllegalArgumentException("No finance WBS template configured for this specific project. Please define one first.");
        }

        List<FinanceEntry> existingRows = financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);
        Map<String, FinanceEntry> existingByWbs = existingRows.stream()
                .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e, (a, b) -> a));

        Set<String> expectedWbsCodes = new HashSet<>();
        for (FinanceWbsTemplateRow template : templateRows) {
            expectedWbsCodes.add(buildFinalWbsCode(template.getCodeTemplate(), project.getCode()));
        }

        for (FinanceEntry existing : existingRows) {
            if (!expectedWbsCodes.contains(existing.getWbsCode())) {
                financeEntryRepository.delete(existing);
            }
        }

        int generatedRows = 0;
        for (FinanceWbsTemplateRow template : templateRows) {
            String finalWbsCode = buildFinalWbsCode(template.getCodeTemplate(), project.getCode());

            FinanceEntry entry = existingByWbs.get(finalWbsCode);
            boolean isNew = false;

            if (entry == null) {
                entry = new FinanceEntry();
                entry.setOrganisation(project.getOrganisation());
                entry.setProject(project);
                isNew = true;
            }

            entry.setWbsCode(finalWbsCode);
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
            entry.setDisplayOrder(template.getSortOrder() != null ? template.getSortOrder() : 0);

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
            generatedRows++;
        }

        ApplyFinanceTemplateResultDto result = new ApplyFinanceTemplateResultDto();
        result.setProjectsProcessed(1);
        result.setRowsGenerated(generatedRows);
        return result;
    }

    public FinanceSettingsDto importWbsTemplate(Long projectId, ImportFinanceWbsTemplateRequest request) {
        Long orgId = requireOrganisationId();
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("Import file contains no WBS rows.");
        }

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
            if (request.isReplaceExisting()) {
                templateRowRepository.deleteAllByProjectId(projectId);
            }
        } else {
            if (request.isReplaceExisting()) {
                templateRowRepository.deleteAllByOrganisationId(orgId);
            }
        }

        int startSort = 0;
        if (projectId != null) {
            startSort = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId).size();
        } else {
            startSort = templateRowRepository.findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(orgId).size();
        }

        int index = 1;
        for (FinanceWbsTemplateRowDto dto : request.getRows()) {
            if (dto.getCodeTemplate() == null || dto.getCodeTemplate().isBlank()) {
                continue;
            }

            FinanceWbsTemplateRow row = new FinanceWbsTemplateRow();
            row.setOrganisation(organisation);
            row.setProject(project);
            row.setSortOrder(startSort + index);
            row.setLevel(dto.getLevel() == null ? detectLevel(dto.getCodeTemplate()) : dto.getLevel());
            row.setCodeTemplate(dto.getCodeTemplate().trim());
            row.setDescription(blankToNull(dto.getDescription()));
            
            if (dto.getType() == null) {
                throw new IllegalArgumentException("Import failed: WBS row '" + dto.getCodeTemplate() + "' is missing the required TYPE.");
            }
            row.setType(dto.getType());
            row.setResourceType(blankToNull(dto.getResourceType()));
            row.setOwnerKey(blankToNull(dto.getOwnerKey()));
            row.setHourRate(dto.getHourRate());

            templateRowRepository.save(row);
            index++;
        }

        return getSettings(projectId);
    }

    // ==========================================
    // ✅ NOUVEAUX : Méthodes pour les Payments
    // ==========================================

    @Transactional(readOnly = true)
    public PaymentScheduleDto getPaymentSchedule(Long projectId) {
        Long orgId = requireOrganisationId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

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
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

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
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
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

    private Long requireOrganisationId() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) throw new IllegalStateException("User has no organisation");
        return orgId;
    }

    private int detectLevel(String code) {
        if (code == null || code.isBlank()) return 1;
        String cleaned = code.trim();
        if (cleaned.contains(".")) return Math.max(1, cleaned.split("\\.").length);
        if (cleaned.contains("-")) return Math.max(1, cleaned.split("-").length - 1);
        return 1;
    }
}