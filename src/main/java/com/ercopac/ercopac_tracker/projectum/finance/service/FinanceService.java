package com.ercopac.ercopac_tracker.projectum.finance.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceCostBreakdownDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDetailDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceImportResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceProjectChartDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceSummaryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.UpsertFinanceEntryRequest;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceHourlyRate;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceHourlyRateRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceWbsTemplateRowRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinanceService {

    private static final BigDecimal DEFAULT_HOURLY_RATE = BigDecimal.valueOf(65);
    private static final int MAX_REPORTED_ROWS = 20;

    private final FinanceEntryRepository financeEntryRepository;
    private final ForecastEntryRepository forecastEntryRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectTaskRepository projectTaskRepository;
    private final FinanceSettingsRepository financeSettingsRepository;
    private final FinanceHourlyRateRepository hourlyRateRepository;
    private final FinanceWbsTemplateRowRepository templateRowRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public FinanceService(FinanceEntryRepository financeEntryRepository,
                          ForecastEntryRepository forecastEntryRepository,
                          ProjectAccessService projectAccessService,
                          ProjectTaskRepository projectTaskRepository,
                          FinanceSettingsRepository financeSettingsRepository,
                          FinanceHourlyRateRepository hourlyRateRepository,
                          FinanceWbsTemplateRowRepository templateRowRepository,
                          ResourceTypeRepository resourceTypeRepository) {
        this.financeEntryRepository = financeEntryRepository;
        this.forecastEntryRepository = forecastEntryRepository;
        this.projectAccessService = projectAccessService;
        this.projectTaskRepository = projectTaskRepository;
        this.financeSettingsRepository = financeSettingsRepository;
        this.hourlyRateRepository = hourlyRateRepository;
        this.templateRowRepository = templateRowRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    // ==========================================
    // Shared WBS loading (also used by Forecast and Finance Settings)
    // ==========================================

    /** The project's WBS rows in the one canonical order used by Finance and Forecast. */
    public List<FinanceEntry> loadWbsRows(Project project) {
        return financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(
                project.getId(), project.getOrganisation().getId());
    }

    /** Forecast entries of the project grouped by WBS row id; legacy entries without a row link are matched by code. */
    public Map<Long, List<ForecastEntry>> forecastEntriesByRow(Project project, List<FinanceEntry> rows) {
        Map<Long, FinanceEntry> byId = rows.stream().collect(Collectors.toMap(FinanceEntry::getId, row -> row));
        Map<String, FinanceEntry> byCode = rows.stream()
                .collect(Collectors.toMap(FinanceEntry::getWbsCode, row -> row, (a, b) -> a));
        Map<Long, List<ForecastEntry>> result = new HashMap<>();
        for (ForecastEntry entry : forecastEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAscPeriodKeyAsc(
                project.getId(), project.getOrganisation().getId())) {
            FinanceEntry target = entry.getFinanceEntry() != null
                    ? byId.get(entry.getFinanceEntry().getId())
                    : byCode.get(entry.getWbsCode());
            if (target != null) result.computeIfAbsent(target.getId(), ignored -> new ArrayList<>()).add(entry);
        }
        return result;
    }

    /**
     * Converts forecast quantities to cost: HOUR rows are forecast in hours and use the row rate, then the
     * resource type default rate, then the organisation default rate. Every other row type is already in money.
     */
    public Map<Long, BigDecimal> costFactors(Project project, List<FinanceEntry> rows) {
        Map<Long, BigDecimal> factors = new HashMap<>();
        boolean hasHourRows = rows.stream().anyMatch(this::isHourRow);
        Map<String, BigDecimal> ratesByCode = Map.of();
        BigDecimal defaultRate = DEFAULT_HOURLY_RATE;
        if (hasHourRows) {
            Long orgId = project.getOrganisation().getId();
            ratesByCode = resourceTypeRepository.findByOrganisation_IdAndActiveTrue(orgId).stream()
                    .filter(rt -> rt.getCode() != null && rt.getDefaultRate() != null)
                    .collect(Collectors.toMap(ResourceType::getCode, ResourceType::getDefaultRate, (a, b) -> a));
            defaultRate = financeSettingsRepository.findByOrganisationId(orgId)
                    .map(FinanceSettings::getDefaultHourlyRate).filter(Objects::nonNull).orElse(DEFAULT_HOURLY_RATE);
        }
        for (FinanceEntry row : rows) {
            if (!isHourRow(row)) factors.put(row.getId(), BigDecimal.ONE);
            else if (row.getHourRate() != null && row.getHourRate().signum() > 0) factors.put(row.getId(), row.getHourRate());
            else factors.put(row.getId(), ratesByCode.getOrDefault(row.getResourceTypeCode(), defaultRate));
        }
        return factors;
    }

    /** Weekly forecast period key, identical to the keys of the Forecast week grid. */
    public static String weekKey(LocalDate day) {
        LocalDate monday = day.with(java.time.DayOfWeek.MONDAY);
        return String.format("%d-W%02d", monday.getYear(),
                monday.get(WeekFields.of(java.time.DayOfWeek.MONDAY, 1).weekOfWeekBasedYear()));
    }

    public boolean isHourRow(FinanceEntry row) {
        return "HOUR".equalsIgnoreCase(row.getRowType());
    }

    // ==========================================
    // Read model
    // ==========================================

    @Transactional(readOnly = true)
    public List<FinanceEntryDto> getProjectFinance(Long projectId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        return buildRows(project, loadWbsRows(project));
    }

    private List<FinanceEntryDto> buildRows(Project project, List<FinanceEntry> rows) {
        Map<Long, Long> parents = FinanceWbsHierarchy.parents(rows);
        List<Long> ids = rows.stream().map(FinanceEntry::getId).toList();
        Set<Long> leaves = FinanceWbsHierarchy.leafIds(ids, parents);

        Map<Long, BigDecimal> factors = costFactors(project, rows);
        Map<Long, BigDecimal> leafForecast = new HashMap<>();
        forecastEntriesByRow(project, rows).forEach((rowId, entries) -> leafForecast.put(rowId, entries.stream()
                .map(entry -> nvl(entry.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(factors.getOrDefault(rowId, BigDecimal.ONE)).setScale(2, RoundingMode.HALF_UP)));

        Map<Long, BigDecimal> sales = rollup(rows, ids, parents, FinanceEntry::getSales);
        Map<Long, BigDecimal> budget = rollup(rows, ids, parents, FinanceEntry::getBudget);
        Map<Long, BigDecimal> commitment = rollup(rows, ids, parents, FinanceEntry::getCommitment);
        Map<Long, BigDecimal> actual = rollup(rows, ids, parents, FinanceEntry::getActualCost);
        Map<Long, BigDecimal> costReserve = rollup(rows, ids, parents, FinanceEntry::getCostReserve);
        Map<Long, BigDecimal> updatedBudget = rollup(rows, ids, parents, FinanceEntry::getUpdatedBudget);
        Map<Long, BigDecimal> forecast = FinanceWbsHierarchy.rollup(ids, parents, leafForecast);

        List<FinanceEntryDto> result = new ArrayList<>();
        for (FinanceEntry row : rows) {
            Long id = row.getId();
            FinanceEntryDto dto = new FinanceEntryDto();
            dto.setId(id);
            dto.setParentId(parents.get(id));
            dto.setWbsCode(row.getWbsCode());
            dto.setDescription(row.getDescription());
            dto.setLevel(row.getLevel());
            dto.setRowType(row.getRowType());
            dto.setLeaf(leaves.contains(id));
            dto.setOwner(row.getOwnerName() != null ? row.getOwnerName() : "—");
            dto.setSales(sales.get(id));
            dto.setBudget(budget.get(id));
            dto.setCommitment(commitment.get(id));
            dto.setActualCost(actual.get(id));
            dto.setForecast(forecast.get(id));
            dto.setCostReserve(costReserve.get(id));
            dto.setUpdatedBudget(updatedBudget.get(id));
            applyEarnedValue(dto);
            result.add(dto);
        }
        return result;
    }

    /** EAC = actual cost + remaining forecast; VAC = budget - EAC. Commitments are reported but never added to EAC. */
    private void applyEarnedValue(FinanceEntryDto dto) {
        BigDecimal budget = nvl(dto.getBudget());
        BigDecimal actualCost = nvl(dto.getActualCost());
        BigDecimal eac = actualCost.add(nvl(dto.getForecast()));
        dto.setEac(eac);
        dto.setVariance(budget.subtract(eac));
        dto.setCpi(eac.signum() > 0 ? budget.divide(eac, 4, RoundingMode.HALF_UP).doubleValue() : null);
        dto.setPercentAc(budget.signum() > 0
                ? actualCost.divide(budget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0);
    }

    private Map<Long, BigDecimal> rollup(List<FinanceEntry> rows, List<Long> ids, Map<Long, Long> parents,
                                         Function<FinanceEntry, BigDecimal> value) {
        Map<Long, BigDecimal> leafValues = new HashMap<>();
        rows.forEach(row -> leafValues.put(row.getId(), nvl(value.apply(row))));
        return FinanceWbsHierarchy.rollup(ids, parents, leafValues);
    }

    @Transactional(readOnly = true)
    public FinanceSummaryDto getProjectFinanceSummary(Long projectId) {
        List<FinanceEntryDto> roots = rootRows(getProjectFinance(projectId));
        FinanceSummaryDto dto = new FinanceSummaryDto();
        dto.setTotalSales(sum(roots, FinanceEntryDto::getSales));
        dto.setTotalBudget(sum(roots, FinanceEntryDto::getBudget));
        dto.setTotalCommitment(sum(roots, FinanceEntryDto::getCommitment));
        dto.setTotalActualCost(sum(roots, FinanceEntryDto::getActualCost));
        dto.setTotalForecast(sum(roots, FinanceEntryDto::getForecast));
        dto.setTotalEac(dto.getTotalActualCost().add(dto.getTotalForecast()));
        dto.setTotalVariance(dto.getTotalBudget().subtract(dto.getTotalEac()));
        return dto;
    }

    @Transactional(readOnly = true)
    public FinanceEntryDetailDto getEntryDetail(Long projectId, Long entryId) {
        List<FinanceEntryDto> rows = getProjectFinance(projectId);
        FinanceEntryDto row = rows.stream().filter(candidate -> candidate.getId().equals(entryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));

        BigDecimal budget = nvl(row.getBudget());
        BigDecimal sales = nvl(row.getSales());
        FinanceEntryDetailDto dto = new FinanceEntryDetailDto();
        dto.setRow(row);
        dto.setPercentCommitment(percent(nvl(row.getCommitment()), budget));
        dto.setPercentForecast(percent(nvl(row.getForecast()), budget));
        dto.setPercentEac(percent(nvl(row.getEac()), budget));
        if (sales.signum() > 0) {
            dto.setMarginPercent(sales.subtract(nvl(row.getEac())).divide(sales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }
        dto.setChildren(rows.stream().filter(candidate -> entryId.equals(candidate.getParentId())).toList());
        return dto;
    }

    @Transactional(readOnly = true)
    public FinanceCostBreakdownDto getCostBreakdown(Long projectId) {
        FinanceSummaryDto summary = getProjectFinanceSummary(projectId);
        FinanceCostBreakdownDto dto = new FinanceCostBreakdownDto();
        dto.setActualCost(summary.getTotalActualCost());
        dto.setForecast(summary.getTotalForecast());
        dto.setRemainingBudget(summary.getTotalVariance());
        return dto;
    }

    @Transactional(readOnly = true)
    public FinanceProjectChartDto getProjectOverview(Long projectId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        FinanceSummaryDto summary = getProjectFinanceSummary(projectId);
        BigDecimal sales = summary.getTotalSales();
        FinanceProjectChartDto dto = new FinanceProjectChartDto();
        dto.setProjectId(project.getId());
        dto.setProjectName(project.getName());
        dto.setBudget(summary.getTotalBudget());
        dto.setEac(summary.getTotalEac());
        dto.setSales(sales);
        dto.setActualCost(summary.getTotalActualCost());
        dto.setForecast(summary.getTotalForecast());
        dto.setVariance(summary.getTotalVariance());
        dto.setMarginPercent(sales.signum() > 0
                ? sales.subtract(summary.getTotalEac()).divide(sales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        return dto;
    }

    // ==========================================
    // Writes
    // ==========================================

    public FinanceEntryDto createEntry(Long projectId, UpsertFinanceEntryRequest request) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        if (financeEntryRepository.existsByProjectIdAndWbsCode(projectId, request.getWbsCode()))
            throw new IllegalArgumentException("Duplicate WBS code: " + request.getWbsCode());
        FinanceEntry entry = new FinanceEntry();
        entry.setProject(project);
        entry.setOrganisation(project.getOrganisation());
        entry.setWbsCode(request.getWbsCode());
        entry.setDescription(request.getDescription());
        entry.setLevel(request.getLevel());
        applyValues(entry, request);
        Long id = financeEntryRepository.save(entry).getId();
        return findRow(project, id);
    }

    public FinanceEntryDto updateEntry(Long projectId, Long entryId, UpsertFinanceEntryRequest request) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        List<FinanceEntry> rows = loadWbsRows(project);
        FinanceEntry entry = findInProject(rows, entryId);
        boolean structureChanged = !Objects.equals(entry.getWbsCode(), request.getWbsCode())
                || !Objects.equals(entry.getLevel(), request.getLevel());
        if (structureChanged && entry.getTemplateRowId() != null)
            throw new IllegalArgumentException("WBS " + entry.getWbsCode() + " is managed in Finance Settings; edit its code or level there.");
        if (!isLeaf(rows, entryId) && hasValues(request))
            throw new IllegalArgumentException("WBS " + entry.getWbsCode() + " has child rows; its values are calculated from its leaves.");
        String previousCode = entry.getWbsCode();
        if (!previousCode.equals(request.getWbsCode()) && financeEntryRepository.existsByProjectIdAndWbsCode(projectId, request.getWbsCode()))
            throw new IllegalArgumentException("Duplicate WBS code: " + request.getWbsCode());
        entry.setWbsCode(request.getWbsCode());
        entry.setDescription(request.getDescription());
        entry.setLevel(request.getLevel());
        applyValues(entry, request);
        if (!previousCode.equals(entry.getWbsCode())) {
            for (ForecastEntry forecast : forecastEntryRepository.findAllByProjectIdAndWbsCode(projectId, previousCode)) {
                forecast.setFinanceEntry(entry);
                forecast.setWbsCode(entry.getWbsCode());
            }
        }
        financeEntryRepository.save(entry);
        return findRow(project, entryId);
    }

    /**
     * Deletes a WBS node, together with its Finance Settings row so that it does not come back on the next
     * template apply. Nodes with children or with any financial or forecast data are never deleted.
     */
    public void deleteEntry(Long projectId, Long entryId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        List<FinanceEntry> rows = loadWbsRows(project);
        FinanceEntry target = findInProject(rows, entryId);
        if (!isLeaf(rows, entryId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete WBS node " + target.getWbsCode()
                    + " because it has child nodes. Delete or move its children first.");
        List<String> data = dataDescription(target);
        if (forecastEntriesByRow(project, rows).containsKey(entryId)) data.add("forecast entries");
        if (!data.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete WBS node " + target.getWbsCode()
                    + " because it holds " + String.join(", ", data)
                    + ". Nothing was deleted; clear that data first (forecast entries are deleted in Forecast).");
        if (target.getTemplateRowId() != null) {
            templateRowRepository.findById(target.getTemplateRowId())
                    .filter(template -> template.getProject() != null && projectId.equals(template.getProject().getId()))
                    .ifPresent(templateRowRepository::delete);
        }
        financeEntryRepository.delete(target);
    }

    /** Sets the remaining forecast of a COST/EXPENSES leaf by adjusting the current week; other weeks are untouched. */
    public FinanceEntryDto updateForecastValue(Long projectId, Long entryId, BigDecimal amount) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        List<FinanceEntry> rows = loadWbsRows(project);
        FinanceEntry entry = findInProject(rows, entryId);
        if (!isLeaf(rows, entryId))
            throw new IllegalArgumentException("Forecast amounts can only be edited on leaf WBS rows");
        if (isHourRow(entry))
            throw new IllegalArgumentException("HOUR rows are forecast in hours; edit them on the Forecast page");
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("Forecast amount must be non-negative");
        List<ForecastEntry> periods = forecastEntriesByRow(project, rows).getOrDefault(entryId, List.of());
        String currentWeek = weekKey(LocalDate.now());
        ForecastEntry target = periods.stream().filter(period -> currentWeek.equals(period.getPeriodKey()))
                .findFirst().orElseGet(ForecastEntry::new);
        BigDecimal otherPeriods = periods.stream().filter(period -> period != target)
                .map(period -> nvl(period.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal periodAmount = amount.subtract(otherPeriods);
        if (periodAmount.signum() < 0)
            throw new IllegalArgumentException("Total cannot be below amounts in other forecast periods; edit those periods in Forecast");
        if (periodAmount.signum() == 0) {
            if (target.getId() != null) forecastEntryRepository.delete(target);
        } else {
            target.setOrganisation(project.getOrganisation());
            target.setProject(project);
            target.setFinanceEntry(entry);
            target.setWbsCode(entry.getWbsCode());
            target.setPeriodKey(currentWeek);
            target.setAmount(periodAmount);
            forecastEntryRepository.save(target);
        }
        return findRow(project, entryId);
    }

    /**
     * Imports budget/actual/committed values from Excel onto existing leaf rows. Unknown WBS codes are rejected
     * with their row number, parent subtotals are ignored, and existing non-zero values are only replaced
     * when the caller confirms with {@code overwrite=true}.
     */
    public FinanceImportResultDto importEntries(Long projectId, List<UpsertFinanceEntryRequest> rows, boolean overwrite) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        if (rows == null || rows.isEmpty()) throw new IllegalArgumentException("The finance file contains no rows.");
        List<FinanceEntry> wbsRows = loadWbsRows(project);
        Map<String, FinanceEntry> byCode = wbsRows.stream()
                .collect(Collectors.toMap(row -> normalise(row.getWbsCode()), row -> row, (a, b) -> a));
        Set<Long> leaves = FinanceWbsHierarchy.leafIds(byCode.values().stream().map(FinanceEntry::getId).toList(),
                FinanceWbsHierarchy.parents(wbsRows));

        List<String> errors = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        Map<FinanceEntry, UpsertFinanceEntryRequest> updates = new LinkedHashMap<>();
        int ignoredParents = 0;
        boolean forecastIgnored = false;
        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 1;
            UpsertFinanceEntryRequest source = rows.get(i);
            String code = source.getWbsCode() == null ? "" : source.getWbsCode().trim();
            if (code.isEmpty()) { errors.add("Row " + rowNumber + ": missing WBS code"); continue; }
            Integer firstRow = seen.putIfAbsent(normalise(code), rowNumber);
            if (firstRow != null) { errors.add("Row " + rowNumber + ": duplicate WBS code " + code + " (already on row " + firstRow + ")"); continue; }
            FinanceEntry target = byCode.get(normalise(code));
            if (target == null) { errors.add("Row " + rowNumber + ": WBS " + code + " does not exist in Finance Settings"); continue; }
            if (nvl(source.getForecast()).signum() != 0) forecastIgnored = true;
            if (!leaves.contains(target.getId())) {
                if (hasValues(source)) ignoredParents++;
                continue;
            }
            List<String> changed = changedValues(target, source);
            if (!changed.isEmpty()) conflicts.add("Row " + rowNumber + " (" + code + "): " + String.join(", ", changed));
            updates.put(target, source);
        }
        if (!errors.isEmpty()) throw new IllegalArgumentException("Import rejected, nothing was saved. " + limit(errors));
        if (!conflicts.isEmpty() && !overwrite)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The import would overwrite existing values. " + limit(conflicts));

        FinanceImportResultDto result = new FinanceImportResultDto();
        for (Map.Entry<FinanceEntry, UpsertFinanceEntryRequest> update : updates.entrySet()) {
            if (sameValues(update.getKey(), update.getValue())) { result.setUnchangedRows(result.getUnchangedRows() + 1); continue; }
            applyValues(update.getKey(), update.getValue());
            result.setUpdatedRows(result.getUpdatedRows() + 1);
        }
        if (ignoredParents > 0)
            result.getMessages().add(ignoredParents + " parent row(s) ignored: their totals are calculated from leaf rows.");
        if (forecastIgnored)
            result.getMessages().add("The Forecast column was ignored: forecasts are managed on the Forecast page.");
        return result;
    }

    @Transactional
    public void recalculateLabourRowsFromTasks(Long projectId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        List<FinanceEntry> financeRows = loadWbsRows(project);

        List<ProjectTask> tasks = projectTaskRepository.findByProjectId(projectId);
        Map<String, List<ProjectTask>> tasksByWbs = tasks.stream()
                .filter(t -> t.getWbsCode() != null && !t.getWbsCode().isBlank())
                .collect(Collectors.groupingBy(ProjectTask::getWbsCode));

        BigDecimal defaultRate = financeSettingsRepository.findByOrganisationId(project.getOrganisation().getId())
                .map(FinanceSettings::getDefaultHourlyRate).orElse(DEFAULT_HOURLY_RATE);

        Map<String, BigDecimal> rateByResourceType = hourlyRateRepository.findAllByOrganisationIdOrderByResourceTypeAsc(project.getOrganisation().getId())
                .stream().collect(Collectors.toMap(FinanceHourlyRate::getResourceType, FinanceHourlyRate::getHourlyRate, (a, b) -> a));

        for (FinanceEntry row : financeRows) {
            if (!"HOUR".equals(row.getRowType())) {
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

    // ==========================================
    // Helpers
    // ==========================================

    /** Names the kinds of financial data a row holds itself (forecast entries are checked separately). */
    public List<String> dataDescription(FinanceEntry row) {
        List<String> data = new ArrayList<>();
        if (nvl(row.getBudget()).signum() != 0 || nvl(row.getUpdatedBudget()).signum() != 0) data.add("budget");
        if (nvl(row.getActualCost()).signum() != 0) data.add("actual cost");
        if (nvl(row.getCommitment()).signum() != 0) data.add("committed cost");
        if (nvl(row.getSales()).signum() != 0 || nvl(row.getCostReserve()).signum() != 0) data.add("sales/cost reserve");
        return data;
    }

    private FinanceEntryDto findRow(Project project, Long entryId) {
        return buildRows(project, loadWbsRows(project)).stream()
                .filter(row -> row.getId().equals(entryId)).findFirst().orElseThrow();
    }

    private FinanceEntry findInProject(List<FinanceEntry> rows, Long entryId) {
        return rows.stream().filter(row -> row.getId().equals(entryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));
    }

    private boolean isLeaf(List<FinanceEntry> rows, Long entryId) {
        return !FinanceWbsHierarchy.parents(rows).containsValue(entryId);
    }

    private void applyValues(FinanceEntry entry, UpsertFinanceEntryRequest request) {
        entry.setSales(nvl(request.getSales()));
        entry.setBudget(nvl(request.getBudget()));
        entry.setCommitment(nvl(request.getCommitment()));
        entry.setActualCost(nvl(request.getActualCost()));
        entry.setCostReserve(nvl(request.getCostReserve()));
        entry.setUpdatedBudget(nvl(request.getUpdatedBudget()));
        if (request.getOwnerName() != null && !request.getOwnerName().isBlank()) entry.setOwnerName(request.getOwnerName());
    }

    private boolean hasValues(UpsertFinanceEntryRequest request) {
        return nvl(request.getSales()).signum() != 0 || nvl(request.getBudget()).signum() != 0
                || nvl(request.getCommitment()).signum() != 0 || nvl(request.getActualCost()).signum() != 0
                || nvl(request.getCostReserve()).signum() != 0 || nvl(request.getUpdatedBudget()).signum() != 0;
    }

    /** Fields where an existing non-zero value would be replaced by a different one. */
    private List<String> changedValues(FinanceEntry entry, UpsertFinanceEntryRequest request) {
        List<String> changed = new ArrayList<>();
        BiConsumer<String, BigDecimal[]> check = (name, pair) -> {
            if (nvl(pair[0]).signum() != 0 && nvl(pair[0]).compareTo(nvl(pair[1])) != 0)
                changed.add(name + " " + nvl(pair[0]).stripTrailingZeros().toPlainString() + " -> " + nvl(pair[1]).stripTrailingZeros().toPlainString());
        };
        check.accept("sales", new BigDecimal[]{entry.getSales(), request.getSales()});
        check.accept("budget", new BigDecimal[]{entry.getBudget(), request.getBudget()});
        check.accept("cost reserve", new BigDecimal[]{entry.getCostReserve(), request.getCostReserve()});
        check.accept("updated budget", new BigDecimal[]{entry.getUpdatedBudget(), request.getUpdatedBudget()});
        check.accept("committed", new BigDecimal[]{entry.getCommitment(), request.getCommitment()});
        check.accept("actual", new BigDecimal[]{entry.getActualCost(), request.getActualCost()});
        return changed;
    }

    private boolean sameValues(FinanceEntry entry, UpsertFinanceEntryRequest request) {
        return nvl(entry.getSales()).compareTo(nvl(request.getSales())) == 0
                && nvl(entry.getBudget()).compareTo(nvl(request.getBudget())) == 0
                && nvl(entry.getCommitment()).compareTo(nvl(request.getCommitment())) == 0
                && nvl(entry.getActualCost()).compareTo(nvl(request.getActualCost())) == 0
                && nvl(entry.getCostReserve()).compareTo(nvl(request.getCostReserve())) == 0
                && nvl(entry.getUpdatedBudget()).compareTo(nvl(request.getUpdatedBudget())) == 0;
    }

    private List<FinanceEntryDto> rootRows(List<FinanceEntryDto> rows) {
        Set<Long> ids = new HashSet<>();
        rows.forEach(row -> ids.add(row.getId()));
        return rows.stream().filter(row -> row.getParentId() == null || !ids.contains(row.getParentId())).toList();
    }

    private BigDecimal sum(List<FinanceEntryDto> rows, Function<FinanceEntryDto, BigDecimal> value) {
        return rows.stream().map(value).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static String limit(List<String> messages) {
        String joined = String.join("; ", messages.subList(0, Math.min(MAX_REPORTED_ROWS, messages.size())));
        return messages.size() > MAX_REPORTED_ROWS ? joined + "; and " + (messages.size() - MAX_REPORTED_ROWS) + " more" : joined;
    }

    private String normalise(String code) {
        return code == null ? "" : code.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private Double percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
