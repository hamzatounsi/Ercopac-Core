package com.ercopac.ercopac_tracker.projectum.finance.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceCostBreakdownDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDetailDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceProjectChartDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceSummaryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.UpsertFinanceEntryRequest;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceHourlyRate;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceHourlyRateRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinanceService {

    private final FinanceEntryRepository financeEntryRepository;
    private final ForecastEntryRepository forecastEntryRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;
    private final ProjectTaskRepository projectTaskRepository;
    private final FinanceSettingsRepository financeSettingsRepository;
    private final FinanceHourlyRateRepository hourlyRateRepository;

    public FinanceService(FinanceEntryRepository financeEntryRepository,
                          ForecastEntryRepository forecastEntryRepository,
                          ProjectRepository projectRepository,
                          SecurityUtils securityUtils,
                          ProjectTaskRepository projectTaskRepository,
                          FinanceSettingsRepository financeSettingsRepository,
                          FinanceHourlyRateRepository hourlyRateRepository) {
        this.financeEntryRepository = financeEntryRepository;
        this.forecastEntryRepository = forecastEntryRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
        this.projectTaskRepository = projectTaskRepository;
        this.financeSettingsRepository = financeSettingsRepository;
        this.hourlyRateRepository = hourlyRateRepository;
    }

    private Map<String, BigDecimal> getForecastTotalsByWbs(Long projectId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        List<ForecastEntry> entries;
        
        if (securityUtils.isPlatformUser()) {
            entries = forecastEntryRepository.findAllByProjectIdOrderByWbsCodeAscPeriodKeyAsc(projectId);
        } else {
            entries = forecastEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAscPeriodKeyAsc(projectId, orgId);
        }

        Map<String, BigDecimal> totals = new HashMap<>();
        for (ForecastEntry entry : entries) {
            totals.merge(entry.getWbsCode(), nvl(entry.getAmount()), BigDecimal::add);
        }
        return totals;
    }

    @Transactional(readOnly = true)
    public List<FinanceEntryDto> getProjectFinance(Long projectId) {
        Project project = getAccessibleProject(projectId);

        // ✅ MODIFIÉ : Trier par displayOrder (ordre du template) puis WBS Code
        List<FinanceEntry> rows = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdOrderByDisplayOrderAscWbsCodeAsc(project.getId())
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(
                        project.getId(), project.getOrganisation().getId());

        Map<String, BigDecimal> forecastTotals = getForecastTotalsByWbs(projectId);
        return rows.stream().map(row -> toDto(row, forecastTotals)).toList();
    }

    @Transactional(readOnly = true)
    public FinanceSummaryDto getProjectFinanceSummary(Long projectId) {
        List<FinanceEntryDto> rows = getProjectFinance(projectId);

        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal budget = BigDecimal.ZERO;
        BigDecimal commitment = BigDecimal.ZERO;
        BigDecimal actualCost = BigDecimal.ZERO;
        BigDecimal forecast = BigDecimal.ZERO;

        for (FinanceEntryDto row : rows) {
            if (row.getLevel() == null || row.getLevel() != 1) {
                continue;
            }
            sales = sales.add(nvl(row.getSales()));
            budget = budget.add(nvl(row.getBudget()));
            commitment = commitment.add(nvl(row.getCommitment()));
            actualCost = actualCost.add(nvl(row.getActualCost()));
            forecast = forecast.add(nvl(row.getForecast()));
        }

        BigDecimal eac = actualCost.add(forecast);
        BigDecimal variance = budget.subtract(eac);

        FinanceSummaryDto dto = new FinanceSummaryDto();
        dto.setTotalSales(sales);
        dto.setTotalBudget(budget);
        dto.setTotalCommitment(commitment);
        dto.setTotalActualCost(actualCost);
        dto.setTotalForecast(forecast);
        dto.setTotalEac(eac);
        dto.setTotalVariance(variance);
        return dto;
    }

    @Transactional
    public void importFinanceDataFromExcel(Long projectId, List<UpsertFinanceEntryRequest> rows) {
        Project project = getAccessibleProject(projectId);
        
        // Charger toutes les entrées Finance existantes pour ce projet
        List<FinanceEntry> existingEntries = financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(
            projectId, project.getOrganisation().getId());
        
        Map<String, FinanceEntry> entriesByWbs = existingEntries.stream()
            .collect(Collectors.toMap(FinanceEntry::getWbsCode, e -> e));
        
        int updatedCount = 0;
        int skippedCount = 0;
        
        for (UpsertFinanceEntryRequest rowData : rows) {
            String wbsCode = rowData.getWbsCode();
            
            // ✅ Chercher l'entrée existante
            FinanceEntry entry = entriesByWbs.get(wbsCode);
            
            if (entry != null) {
                // ✅ METTRE À JOUR uniquement les valeurs financières
                if (rowData.getBudget() != null) {
                    entry.setBudget(rowData.getBudget());
                }
                if (rowData.getActualCost() != null) {
                    entry.setActualCost(rowData.getActualCost());
                }
                if (rowData.getCommitment() != null) {
                    entry.setCommitment(rowData.getCommitment());
                }
                if (rowData.getSales() != null) {
                    entry.setSales(rowData.getSales());
                }
                if (rowData.getForecast() != null) {
                    entry.setForecast(rowData.getForecast());
                }
                
                financeEntryRepository.save(entry);
                updatedCount++;
            } else {
                // ️ WBS n'existe pas - on ignore (ou on pourrait lancer une erreur)
                skippedCount++;
                System.out.println("WBS " + wbsCode + " not found - skipped");
            }
        }
        
        // Recalculer les lignes SUMMARY
        recomputeSummaryRows(projectId, project.getOrganisation().getId());
        
        System.out.println("Import completed: " + updatedCount + " updated, " + skippedCount + " skipped");
    }
    public FinanceEntryDto createEntry(Long projectId, UpsertFinanceEntryRequest request) {
        Project project = getAccessibleProject(projectId);
        FinanceEntry entry = new FinanceEntry();
        entry.setProject(project);
        entry.setOrganisation(project.getOrganisation());
        apply(entry, request);

        Map<String, BigDecimal> forecastTotals = getForecastTotalsByWbs(projectId);
        return toDto(financeEntryRepository.save(entry), forecastTotals);
    }

    public FinanceEntryDto updateEntry(Long projectId, Long entryId, UpsertFinanceEntryRequest request) {
        Project project = getAccessibleProject(projectId);
        FinanceEntry entry = securityUtils.isPlatformUser()
                ? financeEntryRepository.findById(entryId).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"))
                : financeEntryRepository.findByIdAndProjectIdAndOrganisationId(entryId, projectId, project.getOrganisation().getId()).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));

        apply(entry, request);
        Map<String, BigDecimal> forecastTotals = getForecastTotalsByWbs(projectId);
        return toDto(financeEntryRepository.save(entry), forecastTotals);
    }

    public void deleteEntry(Long projectId, Long entryId) {
        Project project = getAccessibleProject(projectId);
        if (securityUtils.isPlatformUser()) {
            financeEntryRepository.deleteById(entryId);
            return;
        }
        financeEntryRepository.deleteByIdAndProjectIdAndOrganisationId(entryId, projectId, project.getOrganisation().getId());
    }

    private void apply(FinanceEntry entry, UpsertFinanceEntryRequest request) {
        entry.setWbsCode(request.getWbsCode());
        entry.setDescription(request.getDescription());
        entry.setLevel(request.getLevel());
        entry.setSales(nvl(request.getSales()));
        entry.setBudget(nvl(request.getBudget()));
        entry.setCommitment(nvl(request.getCommitment()));
        entry.setActualCost(nvl(request.getActualCost()));
        entry.setForecast(nvl(request.getForecast()));
        entry.setCostReserve(nvl(request.getCostReserve()));
        entry.setUpdatedBudget(nvl(request.getUpdatedBudget()));
        entry.setOwnerName(request.getOwnerName());
    }

    private FinanceEntryDto toDto(FinanceEntry entry, Map<String, BigDecimal> forecastTotals) {
        FinanceEntryDto dto = new FinanceEntryDto();
        dto.setId(entry.getId());
        dto.setWbsCode(entry.getWbsCode());
        dto.setDescription(entry.getDescription());
        dto.setLevel(entry.getLevel());
        dto.setOwner(entry.getOwnerName() != null ? entry.getOwnerName() : "—");

        BigDecimal sales = nvl(entry.getSales());
        BigDecimal budget = nvl(entry.getBudget());
        BigDecimal commitment = nvl(entry.getCommitment());
        BigDecimal actualCost = nvl(entry.getActualCost());
        BigDecimal forecast = forecastTotals.getOrDefault(entry.getWbsCode(), BigDecimal.ZERO);
        BigDecimal costReserve = nvl(entry.getCostReserve());
        BigDecimal updatedBudget = nvl(entry.getUpdatedBudget());

        dto.setSales(sales);
        dto.setBudget(budget);
        dto.setCommitment(commitment);
        dto.setActualCost(actualCost);
        dto.setForecast(forecast);
        dto.setCostReserve(costReserve);
        dto.setUpdatedBudget(updatedBudget);

        BigDecimal eac = actualCost.add(forecast);
        BigDecimal variance = budget.subtract(eac);
        dto.setEac(eac);
        dto.setVariance(variance);

        if (eac.compareTo(BigDecimal.ZERO) > 0) {
            dto.setCpi(budget.divide(eac, 4, RoundingMode.HALF_UP).doubleValue());
        } else {
            dto.setCpi(null);
        }

        if (budget.compareTo(BigDecimal.ZERO) > 0) {
            dto.setPercentAc(actualCost.divide(budget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
        } else {
            dto.setPercentAc(0.0);
        }
        return dto;
    }

    private Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) throw new IllegalStateException("User has no organisation");
        return projectRepository.findByIdAndOrganisationId(projectId, orgId).orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Transactional
    public void importEntries(Long projectId, List<UpsertFinanceEntryRequest> rows) {
        Project project = getAccessibleProject(projectId);
        for (UpsertFinanceEntryRequest r : rows) {
            FinanceEntry entry = new FinanceEntry();
            entry.setProject(project);
            entry.setOrganisation(project.getOrganisation());
            apply(entry, r);
            financeEntryRepository.save(entry);
        }
    }

    @Transactional(readOnly = true)
    public FinanceEntryDetailDto getEntryDetail(Long projectId, Long entryId) {
        Project project = getAccessibleProject(projectId);
        FinanceEntry entry = securityUtils.isPlatformUser()
                ? financeEntryRepository.findByIdAndProjectId(entryId, projectId).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"))
                : financeEntryRepository.findByIdAndProjectIdAndOrganisationId(entryId, projectId, project.getOrganisation().getId()).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));

        Map<String, BigDecimal> forecastTotals = getForecastTotalsByWbs(projectId);
        FinanceEntryDto rowDto = toDto(entry, forecastTotals);

        BigDecimal budget = nvl(entry.getBudget());
        BigDecimal commitment = nvl(entry.getCommitment());
        BigDecimal actualCost = nvl(entry.getActualCost());
        BigDecimal forecast = nvl(rowDto.getForecast());
        BigDecimal eac = actualCost.add(forecast);
        BigDecimal sales = nvl(entry.getSales());

        FinanceEntryDetailDto dto = new FinanceEntryDetailDto();
        dto.setRow(rowDto);
        dto.setPercentCommitment(percent(commitment, budget));
        dto.setPercentForecast(percent(forecast, budget));
        dto.setPercentEac(percent(eac, budget));

        if (sales.compareTo(BigDecimal.ZERO) > 0) {
            dto.setMarginPercent(sales.subtract(eac).divide(sales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        String prefix = entry.getWbsCode() + ".";
        List<FinanceEntry> childEntities = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdAndWbsCodeStartingWithOrderByWbsCodeAsc(projectId, prefix)
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdAndWbsCodeStartingWithOrderByWbsCodeAsc(projectId, project.getOrganisation().getId(), prefix);

        List<FinanceEntryDto> directChildren = childEntities.stream()
                .filter(e -> e.getLevel() != null && entry.getLevel() != null && e.getLevel() == entry.getLevel() + 1)
                .map(e -> toDto(e, forecastTotals))
                .toList();

        dto.setChildren(directChildren);
        return dto;
    }

    private Double percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Transactional(readOnly = true)
    public FinanceCostBreakdownDto getCostBreakdown(Long projectId) {
        List<FinanceEntryDto> rows = getProjectFinance(projectId);
        BigDecimal budget = BigDecimal.ZERO;
        BigDecimal actualCost = BigDecimal.ZERO;
        BigDecimal forecast = BigDecimal.ZERO;

        for (FinanceEntryDto row : rows) {
            if (row.getLevel() == null || row.getLevel() != 1) continue;
            budget = budget.add(nvl(row.getBudget()));
            actualCost = actualCost.add(nvl(row.getActualCost()));
            forecast = forecast.add(nvl(row.getForecast()));
        }

        BigDecimal remaining = budget.subtract(actualCost.add(forecast));
        FinanceCostBreakdownDto dto = new FinanceCostBreakdownDto();
        dto.setActualCost(actualCost);
        dto.setForecast(forecast);
        dto.setRemainingBudget(remaining);
        return dto;
    }

    @Transactional(readOnly = true)
    public FinanceProjectChartDto getProjectOverview(Long projectId) {
        Project project = getAccessibleProject(projectId);
        List<FinanceEntryDto> rows = getProjectFinance(projectId);

        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal budget = BigDecimal.ZERO;
        BigDecimal actualCost = BigDecimal.ZERO;
        BigDecimal forecast = BigDecimal.ZERO;

        for (FinanceEntryDto row : rows) {
            if (row.getLevel() == null || row.getLevel() != 1) continue;
            sales = sales.add(nvl(row.getSales()));
            budget = budget.add(nvl(row.getBudget()));
            actualCost = actualCost.add(nvl(row.getActualCost()));
            forecast = forecast.add(nvl(row.getForecast()));
        }

        BigDecimal eac = actualCost.add(forecast);
        BigDecimal variance = budget.subtract(eac);
        BigDecimal marginPercent = BigDecimal.ZERO;
        if (sales.compareTo(BigDecimal.ZERO) > 0) {
            marginPercent = sales.subtract(eac).divide(sales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        FinanceProjectChartDto dto = new FinanceProjectChartDto();
        dto.setProjectId(project.getId());
        dto.setProjectName(project.getName());
        dto.setBudget(budget);
        dto.setEac(eac);
        dto.setSales(sales);
        dto.setActualCost(actualCost);
        dto.setForecast(forecast);
        dto.setVariance(variance);
        dto.setMarginPercent(marginPercent);
        return dto;
    }

    @Transactional
    public void recalculateLabourRowsFromTasks(Long projectId) {
        Project project = getAccessibleProject(projectId);

        // ✅ MODIFIÉ : Utiliser le nouveau tri
        List<FinanceEntry> financeRows = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdOrderByDisplayOrderAscWbsCodeAsc(projectId)
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(projectId, project.getOrganisation().getId());

        List<ProjectTask> tasks = projectTaskRepository.findByProjectId(projectId);
        Map<String, List<ProjectTask>> tasksByWbs = tasks.stream()
                .filter(t -> t.getWbsCode() != null && !t.getWbsCode().isBlank())
                .collect(Collectors.groupingBy(ProjectTask::getWbsCode));

        BigDecimal defaultRate = financeSettingsRepository.findByOrganisationId(project.getOrganisation().getId())
                .map(FinanceSettings::getDefaultHourlyRate).orElse(BigDecimal.valueOf(65));

        Map<String, BigDecimal> rateByResourceType = hourlyRateRepository.findAllByOrganisationIdOrderByResourceTypeAsc(project.getOrganisation().getId())
                .stream().collect(Collectors.toMap(FinanceHourlyRate::getResourceType, FinanceHourlyRate::getHourlyRate, (a, b) -> a));

        for (FinanceEntry row : financeRows) {
            if ("HOUR".equals(row.getRowType())) {
                continue;
            }

            List<ProjectTask> matchingTasks = tasksByWbs.getOrDefault(row.getWbsCode(), List.of());
            BigDecimal plannedCost = BigDecimal.ZERO;
            BigDecimal actualCost = BigDecimal.ZERO;
            BigDecimal forecastCost = BigDecimal.ZERO;

            for (ProjectTask task : matchingTasks) {
                BigDecimal taskRate = resolveTaskRate(task, row, rateByResourceType, defaultRate);
                BigDecimal plannedHours = nvl(task.getPlannedHours());
                BigDecimal actualHours = nvl(task.getActualHours());

                plannedCost = plannedCost.add(plannedHours.multiply(taskRate));
                actualCost = actualCost.add(actualHours.multiply(taskRate));

                BigDecimal remainingHours = plannedHours.subtract(actualHours);
                if (remainingHours.compareTo(BigDecimal.ZERO) < 0) remainingHours = BigDecimal.ZERO;
                forecastCost = forecastCost.add(remainingHours.multiply(taskRate));
            }

            row.setBudget(plannedCost);
            row.setActualCost(actualCost);
            row.setForecast(forecastCost);
            financeEntryRepository.save(row);
        }
        recomputeSummaryRows(projectId, project.getOrganisation().getId());
    }

    private BigDecimal resolveTaskRate(ProjectTask task, FinanceEntry financeRow, Map<String, BigDecimal> rateByResourceType, BigDecimal defaultRate) {
        if (financeRow.getHourRate() != null && financeRow.getHourRate().compareTo(BigDecimal.ZERO) > 0) {
            return financeRow.getHourRate();
        }
        if (task.getAssignedUser() != null && task.getAssignedUser().getDefaultRate() != null && task.getAssignedUser().getDefaultRate().compareTo(BigDecimal.ZERO) > 0) {
            return task.getAssignedUser().getDefaultRate();
        }
        String resourceType = task.getResourceTypeCode();
        if ((resourceType == null || resourceType.isBlank()) && task.getAssignedUser() != null) {
            resourceType = task.getAssignedUser().getResourceType() != null ? task.getAssignedUser().getResourceType().getCode() : null;
        }
        if (resourceType != null && rateByResourceType.containsKey(resourceType)) {
            return rateByResourceType.get(resourceType);
        }
        return defaultRate;
    }

    private void recomputeSummaryRows(Long projectId, Long organisationId) {
        // ✅ MODIFIÉ : Utiliser le nouveau tri
        List<FinanceEntry> rows = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdOrderByDisplayOrderAscWbsCodeAsc(projectId)
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(projectId, organisationId);

        List<FinanceEntry> summaryRows = rows.stream()
                .filter(r -> "SUMMARY".equals(r.getRowType()))
                .sorted((a, b) -> Integer.compare(
                        b.getLevel() == null ? 0 : b.getLevel(),
                        a.getLevel() == null ? 0 : a.getLevel()
                ))
                .toList();

        for (FinanceEntry summary : summaryRows) {
            String prefix = summary.getWbsCode() + ".";
            List<FinanceEntry> children = rows.stream()
                    .filter(r -> !Objects.equals(r.getId(), summary.getId()))
                    .filter(r -> r.getWbsCode() != null && r.getWbsCode().startsWith(prefix))
                    .filter(r -> r.getLevel() != null && summary.getLevel() != null && r.getLevel() == summary.getLevel() + 1)
                    .toList();

            BigDecimal sales = children.stream().map(FinanceEntry::getSales).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal budget = children.stream().map(FinanceEntry::getBudget).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal commitment = children.stream().map(FinanceEntry::getCommitment).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal actualCost = children.stream().map(FinanceEntry::getActualCost).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal forecast = children.stream().map(FinanceEntry::getForecast).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);

            summary.setSales(sales);
            summary.setBudget(budget);
            summary.setCommitment(commitment);
            summary.setActualCost(actualCost);
            summary.setForecast(forecast);
            financeEntryRepository.save(summary);
        }
    }
}