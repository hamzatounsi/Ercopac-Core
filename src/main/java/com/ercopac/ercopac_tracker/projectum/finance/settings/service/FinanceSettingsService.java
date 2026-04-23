package com.ercopac.ercopac_tracker.projectum.finance.settings.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.*;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.*;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.*;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
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
    private final FinanceOwnerMappingRepository ownerMappingRepository;
    private final FinanceHourlyRateRepository hourlyRateRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final ProjectRepository projectRepository;
    private final OrganisationRepository organisationRepository;
    private final SecurityUtils securityUtils;

    public FinanceSettingsService(
            FinanceSettingsRepository financeSettingsRepository,
            FinanceWbsTemplateRowRepository templateRowRepository,
            FinanceOwnerMappingRepository ownerMappingRepository,
            FinanceHourlyRateRepository hourlyRateRepository,
            FinanceEntryRepository financeEntryRepository,
            ProjectRepository projectRepository,
            OrganisationRepository organisationRepository,
            SecurityUtils securityUtils
    ) {
        this.financeSettingsRepository = financeSettingsRepository;
        this.templateRowRepository = templateRowRepository;
        this.ownerMappingRepository = ownerMappingRepository;
        this.hourlyRateRepository = hourlyRateRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.projectRepository = projectRepository;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public FinanceSettingsDto getSettings() {
        Long orgId = requireOrganisationId();

        FinanceSettings settings = financeSettingsRepository.findByOrganisationId(orgId)
                .orElseGet(() -> {
                    FinanceSettings s = new FinanceSettings();
                    s.setDefaultHourlyRate(BigDecimal.valueOf(65));
                    return s;
                });

        FinanceSettingsDto dto = new FinanceSettingsDto();
        dto.setDefaultHourlyRate(settings.getDefaultHourlyRate());

        dto.setTemplateRows(
                templateRowRepository.findAllByOrganisationIdOrderBySortOrderAscIdAsc(orgId)
                        .stream()
                        .map(this::toDto)
                        .toList()
        );

        dto.setOwnerMappings(
                ownerMappingRepository.findAllByOrganisationIdOrderByOwnerKeyAsc(orgId)
                        .stream()
                        .map(this::toDto)
                        .toList()
        );

        dto.setHourlyRates(
                hourlyRateRepository.findAllByOrganisationIdOrderByResourceTypeAsc(orgId)
                        .stream()
                        .map(this::toDto)
                        .toList()
        );

        return dto;
    }

    public FinanceSettingsDto saveSettings(SaveFinanceSettingsRequest request) {
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

        templateRowRepository.deleteAllByOrganisationId(orgId);
        ownerMappingRepository.deleteAllByOrganisationId(orgId);
        hourlyRateRepository.deleteAllByOrganisationId(orgId);

        for (FinanceWbsTemplateRowDto dto : request.getTemplateRows()) {
            FinanceWbsTemplateRow row = new FinanceWbsTemplateRow();
            row.setOrganisation(organisation);
            row.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
            row.setLevel(dto.getLevel());
            row.setCodeTemplate(dto.getCodeTemplate());
            row.setDescription(dto.getDescription());
            row.setType(dto.getType());
            row.setOwnerKey(blankToNull(dto.getOwnerKey()));
            row.setHourRate(dto.getHourRate());
            templateRowRepository.save(row);
        }

        for (FinanceOwnerMappingDto dto : request.getOwnerMappings()) {
            FinanceOwnerMapping mapping = new FinanceOwnerMapping();
            mapping.setOrganisation(organisation);
            mapping.setOwnerKey(dto.getOwnerKey());
            mapping.setResourceType(dto.getResourceType());
            mapping.setRoleFilter(blankToNull(dto.getRoleFilter()));
            mapping.setNotes(blankToNull(dto.getNotes()));
            ownerMappingRepository.save(mapping);
        }

        for (FinanceHourlyRateDto dto : request.getHourlyRates()) {
            FinanceHourlyRate rate = new FinanceHourlyRate();
            rate.setOrganisation(organisation);
            rate.setResourceType(dto.getResourceType());
            rate.setHourlyRate(nvl(dto.getHourlyRate(), BigDecimal.ZERO));
            hourlyRateRepository.save(rate);
        }

        return getSettings();
    }

    @Transactional
    public ApplyFinanceTemplateResultDto applyTemplate(ApplyFinanceTemplateRequest request) {
        Long orgId = requireOrganisationId();

        List<Project> projects;
        if (request.getProjectIds() == null || request.getProjectIds().isEmpty()) {
            projects = projectRepository.findAllByOrganisationId(orgId);
        } else {
            Set<Long> requestedIds = new HashSet<>(request.getProjectIds());
            projects = projectRepository.findAllById(requestedIds).stream()
                    .filter(p -> p.getOrganisation() != null && Objects.equals(p.getOrganisation().getId(), orgId))
                    .toList();
        }

        List<FinanceWbsTemplateRow> templateRows =
                templateRowRepository.findAllByOrganisationIdOrderBySortOrderAscIdAsc(orgId);

        if (templateRows.isEmpty()) {
            throw new IllegalArgumentException("No finance WBS template configured");
        }

        BigDecimal defaultRate = financeSettingsRepository.findByOrganisationId(orgId)
                .map(FinanceSettings::getDefaultHourlyRate)
                .orElse(BigDecimal.valueOf(65));

        int generatedRows = 0;

        for (Project project : projects) {
            List<FinanceEntry> existingRows =
                    financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(project.getId(), orgId);

            Map<String, FinanceEntry> existingByWbs = existingRows.stream()
                    .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e, (a, b) -> a));

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

                // For now keep display owner name only
                entry.setOwnerName(resolveOwnerDisplay(template.getOwnerKey()));

                // Preserve existing values when row already exists
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
        }

        ApplyFinanceTemplateResultDto result = new ApplyFinanceTemplateResultDto();
        result.setProjectsProcessed(projects.size());
        result.setRowsGenerated(generatedRows);
        return result;
    }

    private String buildProjectCode(Project project) {
        if (project.getCode() != null && !project.getCode().isBlank()) {
            return project.getCode();
        }
        return String.valueOf(project.getId());
    }

    private Long requireOrganisationId() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }
        return orgId;
    }

    private BigDecimal nvl(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private FinanceWbsTemplateRowDto toDto(FinanceWbsTemplateRow row) {
        FinanceWbsTemplateRowDto dto = new FinanceWbsTemplateRowDto();
        dto.setId(row.getId());
        dto.setSortOrder(row.getSortOrder());
        dto.setLevel(row.getLevel());
        dto.setCodeTemplate(row.getCodeTemplate());
        dto.setDescription(row.getDescription());
        dto.setType(row.getType());
        dto.setOwnerKey(row.getOwnerKey());
        dto.setHourRate(row.getHourRate());
        return dto;
    }

    private FinanceOwnerMappingDto toDto(FinanceOwnerMapping mapping) {
        FinanceOwnerMappingDto dto = new FinanceOwnerMappingDto();
        dto.setId(mapping.getId());
        dto.setOwnerKey(mapping.getOwnerKey());
        dto.setResourceType(mapping.getResourceType());
        dto.setRoleFilter(mapping.getRoleFilter());
        dto.setNotes(mapping.getNotes());
        return dto;
    }

    private FinanceHourlyRateDto toDto(FinanceHourlyRate rate) {
        FinanceHourlyRateDto dto = new FinanceHourlyRateDto();
        dto.setId(rate.getId());
        dto.setResourceType(rate.getResourceType());
        dto.setHourlyRate(rate.getHourlyRate());
        return dto;
    }

    private String buildFinalWbsCode(String codeTemplate, String projectCode) {
    if (codeTemplate == null || codeTemplate.isBlank()) {
        throw new IllegalArgumentException("Template WBS code cannot be blank");
    }
    if (projectCode == null || projectCode.isBlank()) {
        throw new IllegalArgumentException("Project code cannot be blank");
    }

    return codeTemplate.replace("xxx25", projectCode.trim());
    }

    private String resolveOwnerDisplay(String ownerKey) {
        if (ownerKey == null || ownerKey.isBlank()) {
            return "—";
        }

        return ownerKey;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    
}