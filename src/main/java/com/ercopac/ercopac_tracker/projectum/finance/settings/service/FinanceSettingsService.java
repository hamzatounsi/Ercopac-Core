package com.ercopac.ercopac_tracker.projectum.finance.settings.service;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.*;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceWbsTemplateRowRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    public FinanceSettingsService(
            FinanceSettingsRepository financeSettingsRepository,
            FinanceWbsTemplateRowRepository templateRowRepository,
            FinanceEntryRepository financeEntryRepository,
            ProjectRepository projectRepository,
            OrganisationRepository organisationRepository,
            SecurityUtils securityUtils,
            DepartmentRepository departmentRepository,
            UserRepository userRepository
    ) {
        this.financeSettingsRepository = financeSettingsRepository;
        this.templateRowRepository = templateRowRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.projectRepository = projectRepository;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
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
            // Fallback sur le global si le projet n'a pas encore de template
            if (rows.isEmpty()) {
                rows = templateRowRepository.findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(orgId);
            }
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
            row.setProject(project); // ✅ Lien avec le projet
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
            
            row.setResourceType(blankToNull(dto.getResourceType()));
            
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

    @Transactional
    public ApplyFinanceTemplateResultDto applyTemplate(Long projectId, ApplyFinanceTemplateRequest request) {
        Long orgId = requireOrganisationId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // ✅ Utiliser le template spécifique au projet
        List<FinanceWbsTemplateRow> templateRows = templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);
        
        // Fallback sur le global si vide
        if (templateRows.isEmpty()) {
            templateRows = templateRowRepository.findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(orgId);
        }

        if (templateRows.isEmpty()) {
            throw new IllegalArgumentException("No finance WBS template configured for this project or globally.");
        }

        List<FinanceEntry> existingRows = financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);
        Map<String, FinanceEntry> existingByWbs = existingRows.stream()
                .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e, (a, b) -> a));

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
            entry.setOwnerName(resolveOwnerDisplay(template.getOwnerKey()));
            entry.setResourceTypeCode(template.getResourceType()); // ✅ Lien avec le Resource Type

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
            row.setProject(project); // ✅ Lien avec le projet
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

    // ─── Helper Methods ──────────────────────────────────────────────────────

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