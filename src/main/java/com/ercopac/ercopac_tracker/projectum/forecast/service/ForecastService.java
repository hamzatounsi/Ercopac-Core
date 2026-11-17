package com.ercopac.ercopac_tracker.projectum.forecast.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceWbsHierarchy;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastGridCellDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastRowDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastSummaryDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.UpsertForecastEntryRequest;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Time-phased forecast on top of the Finance WBS. The WBS rows, their order, their parents and their cost
 * rates all come from {@link FinanceService}, so Forecast can never show a different structure than Finance.
 */
@Service
@Transactional
public class ForecastService {

    private final ForecastEntryRepository forecastEntryRepository;
    private final FinanceService financeService;
    private final ProjectAccessService projectAccessService;
    private final ProjectTaskRepository projectTaskRepository;

    public ForecastService(ForecastEntryRepository forecastEntryRepository,
                           FinanceService financeService,
                           ProjectAccessService projectAccessService,
                           ProjectTaskRepository projectTaskRepository) {
        this.forecastEntryRepository = forecastEntryRepository;
        this.financeService = financeService;
        this.projectAccessService = projectAccessService;
        this.projectTaskRepository = projectTaskRepository;
    }

    @Transactional(readOnly = true)
    public List<ForecastRowDto> getForecastGrid(Long projectId, int periods, String periodType) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        String safePeriodType = periodType != null ? periodType : "month";

        // 1. Finance WBS rows, entries and rates, loaded once and shared with Finance
        List<FinanceEntry> financeRows = financeService.loadWbsRows(project);
        Map<Long, List<ForecastEntry>> entriesByRow = financeService.forecastEntriesByRow(project, financeRows);
        Map<Long, BigDecimal> factors = financeService.costFactors(project, financeRows);
        Map<Long, Long> parents = FinanceWbsHierarchy.parents(financeRows);
        Set<Long> leaves = FinanceWbsHierarchy.leafIds(financeRows.stream().map(FinanceEntry::getId).toList(), parents);

        // 2. Schedule tasks for HOUR rows
        boolean hasHourRows = financeRows.stream().anyMatch(financeService::isHourRow);
        List<ProjectTask> allTasks = hasHourRows ? projectTaskRepository.findByProjectIdOrderByDisplayOrderAsc(projectId) : List.of();
        Map<String, ProjectTask> tasksByWbs = allTasks.stream()
                .filter(t -> t.getWbsCode() != null)
                .collect(Collectors.toMap(ProjectTask::getWbsCode, t -> t, (a, b) -> a));

        List<ForecastRowDto.ScheduleTaskOption> availableTasks = allTasks.stream()
                .filter(t -> t.getWbsCode() != null)
                .map(t -> {
                    ForecastRowDto.ScheduleTaskOption opt = new ForecastRowDto.ScheduleTaskOption();
                    opt.setWbsCode(t.getWbsCode());
                    opt.setName(t.getName());
                    opt.setOutlineLevel(t.getOutlineLevel());
                    opt.setPlannedHours(t.getPlannedHours() != null ? t.getPlannedHours() : BigDecimal.ZERO);
                    return opt;
                })
                .collect(Collectors.toList());

        List<String> periodKeys = buildPeriods(periods, safePeriodType);
        List<ForecastRowDto> result = new ArrayList<>();

        for (FinanceEntry row : financeRows) {
            boolean hourRow = financeService.isHourRow(row);
            BigDecimal factor = factors.getOrDefault(row.getId(), BigDecimal.ONE);
            ForecastRowDto dto = new ForecastRowDto();
            dto.setFinanceEntryId(row.getId());
            dto.setParentId(parents.get(row.getId()));
            dto.setLeaf(leaves.contains(row.getId()));
            dto.setWbsCode(row.getWbsCode());
            dto.setDescription(row.getDescription());
            dto.setLevel(row.getLevel() != null ? row.getLevel() : 1);
            dto.setBudget(nvl(row.getBudget()));
            dto.setRowType(row.getRowType() != null ? row.getRowType() : "COST");
            dto.setActualCost(nvl(row.getActualCost()));
            dto.setHourRate(hourRow ? factor : null);
            dto.setResourceTypeCode(row.getResourceTypeCode());
            dto.setLinkedScheduleWbs(row.getLinkedScheduleWbs());
            dto.setAvailableScheduleTasks(hourRow ? availableTasks : List.of());

            // Remaining Schedule = outstanding hours of the linked Gantt task (or its leaves) times the row rate
            BigDecimal remainingHours = BigDecimal.ZERO;
            String linkedWbs = row.getLinkedScheduleWbs();
            if (hourRow && linkedWbs != null && !linkedWbs.isBlank()) {
                ProjectTask linkedTask = tasksByWbs.get(linkedWbs);
                if (linkedTask != null) remainingHours = calculateRemainingHoursFromToday(linkedTask, allTasks);
            }
            dto.setRemainingHours(remainingHours);
            dto.setRemainingCost(hourRow ? remainingHours.multiply(factor).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            // Visible period cells plus the total over every period, in the row unit (hours for HOUR rows)
            Map<String, BigDecimal> cellAmounts = new HashMap<>();
            BigDecimal totalFc = BigDecimal.ZERO;
            for (ForecastEntry entry : entriesByRow.getOrDefault(row.getId(), List.of())) {
                totalFc = totalFc.add(nvl(entry.getAmount()));
                String key = entry.getPeriodKey();
                if ("month".equalsIgnoreCase(safePeriodType) && key.contains("-W")) key = convertWeekToMonth(key);
                cellAmounts.merge(key, nvl(entry.getAmount()), BigDecimal::add);
            }
            List<ForecastGridCellDto> cells = new ArrayList<>();
            for (String periodKey : periodKeys) cells.add(new ForecastGridCellDto(periodKey, nvl(cellAmounts.get(periodKey))));
            dto.setPeriods(cells);
            dto.setTotalForecast(totalFc);
            result.add(dto);
        }

        return rollupRows(result);
    }

    BigDecimal calculateRemainingHoursFromToday(ProjectTask task, List<ProjectTask> allTasks) {
        List<ProjectTask> tasksToProcess = new ArrayList<>();
        boolean isSummary = allTasks.stream().anyMatch(t -> task.getId().equals(t.getParentId()));

        if (isSummary) {
            tasksToProcess = getAllDescendants(task.getId(), allTasks);
        } else {
            tasksToProcess.add(task);
        }

        BigDecimal totalRemaining = BigDecimal.ZERO;

        for (ProjectTask t : tasksToProcess) {
            if (allTasks.stream().anyMatch(child -> t.getId().equals(child.getParentId()))) continue;
            BigDecimal taskHours = taskHours(t);
            if (t.getPlannedHours() != null && t.getActualHours() != null) {
                totalRemaining = totalRemaining.add(t.getPlannedHours().subtract(t.getActualHours()).max(BigDecimal.ZERO));
                continue;
            }
            if (t.getPercentComplete() != null) {
                int remainingPercent = 100 - Math.max(0, Math.min(100, t.getPercentComplete()));
                totalRemaining = totalRemaining.add(taskHours
                        .multiply(BigDecimal.valueOf(remainingPercent)).divide(BigDecimal.valueOf(100)));
                continue;
            }
            if (!"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"DONE".equalsIgnoreCase(t.getStatus()))
                totalRemaining = totalRemaining.add(taskHours);
        }

        return totalRemaining;
    }

    /** Hours of a schedule task: its planned hours, or its duration × 8h × allocation when no hours are entered. */
    private BigDecimal taskHours(ProjectTask task) {
        if (task.getPlannedHours() != null) return task.getPlannedHours();
        if (task.getDurationDays() == null || "MILESTONE".equalsIgnoreCase(task.getTaskType())) return BigDecimal.ZERO;
        int allocation = task.getAllocationPercent() != null ? task.getAllocationPercent() : 100;
        return BigDecimal.valueOf(task.getDurationDays() * 8L * allocation).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private List<ProjectTask> getAllDescendants(Long parentId, List<ProjectTask> allTasks) {
        List<ProjectTask> descendants = new ArrayList<>();
        for (ProjectTask t : allTasks) {
            if (parentId.equals(t.getParentId())) {
                descendants.add(t);
                descendants.addAll(getAllDescendants(t.getId(), allTasks));
            }
        }
        return descendants;
    }

    /**
     * Parents show the sum of their leaves only. Leaf hours are converted to cost before they reach a parent,
     * so parent period cells and totals are always money and never mix hours with euros.
     */
    List<ForecastRowDto> rollupRows(List<ForecastRowDto> rows) {
        Map<Long, Long> parents = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        for (ForecastRowDto row : rows) {
            ids.add(row.getFinanceEntryId());
            parents.put(row.getFinanceEntryId(), row.getParentId());
        }
        Set<Long> leaves = FinanceWbsHierarchy.leafIds(ids, parents);
        Map<Long, BigDecimal> factor = new HashMap<>();
        rows.forEach(row -> factor.put(row.getFinanceEntryId(),
                "HOUR".equalsIgnoreCase(row.getRowType()) && row.getHourRate() != null ? row.getHourRate() : BigDecimal.ONE));

        Map<Long, BigDecimal> budget = rollup(rows, ids, parents, r -> nvl(r.getBudget()));
        Map<Long, BigDecimal> actual = rollup(rows, ids, parents, r -> nvl(r.getActualCost()));
        Map<Long, BigDecimal> forecastCost = rollup(rows, ids, parents,
                r -> cost(r.getTotalForecast(), factor.get(r.getFinanceEntryId())));
        Map<Long, BigDecimal> remainingHours = rollup(rows, ids, parents, r -> nvl(r.getRemainingHours()));
        Map<Long, BigDecimal> remainingCost = rollup(rows, ids, parents, r -> nvl(r.getRemainingCost()));
        int periodCount = rows.stream().mapToInt(r -> r.getPeriods().size()).max().orElse(0);
        List<Map<Long, BigDecimal>> periodCost = new ArrayList<>();
        for (int i = 0; i < periodCount; i++) {
            final int index = i;
            periodCost.add(rollup(rows, ids, parents, r -> index < r.getPeriods().size()
                    ? cost(r.getPeriods().get(index).getAmount(), factor.get(r.getFinanceEntryId())) : BigDecimal.ZERO));
        }

        for (ForecastRowDto row : rows) {
            Long id = row.getFinanceEntryId();
            row.setLeaf(leaves.contains(id));
            row.setRemainingForecastCost(forecastCost.get(id));
            if (leaves.contains(id)) continue;
            row.setBudget(budget.get(id));
            row.setActualCost(actual.get(id));
            row.setTotalForecast(forecastCost.get(id));
            row.setRemainingHours(remainingHours.get(id));
            row.setRemainingCost(remainingCost.get(id));
            for (int i = 0; i < row.getPeriods().size(); i++) row.getPeriods().get(i).setAmount(periodCost.get(i).get(id));
        }
        return rows;
    }

    private Map<Long, BigDecimal> rollup(List<ForecastRowDto> rows, List<Long> ids, Map<Long, Long> parents,
                                         Function<ForecastRowDto, BigDecimal> value) {
        Map<Long, BigDecimal> leafValues = new HashMap<>();
        rows.forEach(row -> leafValues.put(row.getFinanceEntryId(), value.apply(row)));
        return FinanceWbsHierarchy.rollup(ids, parents, leafValues);
    }

    private BigDecimal cost(BigDecimal quantity, BigDecimal factor) {
        return nvl(quantity).multiply(factor == null ? BigDecimal.ONE : factor).setScale(2, RoundingMode.HALF_UP);
    }

    private String convertWeekToMonth(String weekKey) {
        try {
            String[] parts = weekKey.split("-W");
            int year = Integer.parseInt(parts[0]);
            int week = Integer.parseInt(parts[1]);
            LocalDate date = LocalDate.ofYearDay(year, 1).with(WeekFields.of(java.time.DayOfWeek.MONDAY, 1).weekOfWeekBasedYear(), week);
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        } catch (Exception e) {
            return weekKey;
        }
    }

    public void updateLinkedScheduleWbs(Long projectId, Long financeEntryId, String linkedScheduleWbs) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        FinanceEntry entry = findRow(financeService.loadWbsRows(project), financeEntryId);
        if (!financeService.isHourRow(entry)) {
            throw new IllegalArgumentException("Only hour forecasts can link to a schedule task");
        }
        entry.setLinkedScheduleWbs(linkedScheduleWbs);
    }

    @Transactional(readOnly = true)
    public ForecastSummaryDto getSummary(Long projectId, int periods) {
        List<ForecastRowDto> rows = getForecastGrid(projectId, periods, "month");
        BigDecimal totalForecast = BigDecimal.ZERO, totalBudget = BigDecimal.ZERO, totalActual = BigDecimal.ZERO;
        for (ForecastRowDto row : rows) {
            if (row.getParentId() != null) continue;
            totalForecast = totalForecast.add(nvl(row.getRemainingForecastCost()));
            totalBudget = totalBudget.add(nvl(row.getBudget()));
            totalActual = totalActual.add(nvl(row.getActualCost()));
        }
        ForecastSummaryDto dto = new ForecastSummaryDto();
        dto.setTotalForecast(totalForecast); dto.setTotalBudget(totalBudget); dto.setTotalActualCost(totalActual);
        dto.setTotalEac(totalActual.add(totalForecast)); dto.setTotalVariance(totalBudget.subtract(dto.getTotalEac()));
        return dto;
    }

    /** Creates, changes or (with 0) removes the forecast of one leaf for one period; other periods are never touched. */
    public void upsertForecast(Long projectId, UpsertForecastEntryRequest request) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        Long orgId = project.getOrganisation().getId();
        List<FinanceEntry> projectRows = financeService.loadWbsRows(project);
        FinanceEntry financeRow = projectRows.stream()
                .filter(row -> request.getFinanceEntryId() != null
                        ? row.getId().equals(request.getFinanceEntryId())
                        : row.getWbsCode().equals(request.getWbsCode()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Finance WBS row not found in this project"));
        if (FinanceWbsHierarchy.parents(projectRows).containsValue(financeRow.getId()))
            throw new IllegalArgumentException("Select a leaf Finance WBS row for the forecast amount");
        if (request.getAmount() != null && request.getAmount().signum() < 0)
            throw new IllegalArgumentException("Forecast amount must be non-negative");
        ForecastEntry entry = forecastEntryRepository
                .findByProjectIdAndOrganisationIdAndFinanceEntryIdAndPeriodKey(projectId, orgId, financeRow.getId(), request.getPeriodKey())
                .or(() -> forecastEntryRepository.findByProjectIdAndOrganisationIdAndWbsCodeAndPeriodKey(
                        projectId, orgId, financeRow.getWbsCode(), request.getPeriodKey()))
                .orElseGet(ForecastEntry::new);
        if (nvl(request.getAmount()).signum() == 0) {
            if (entry.getId() != null) forecastEntryRepository.delete(entry);
            return;
        }
        entry.setProject(project); entry.setOrganisation(project.getOrganisation());
        entry.setFinanceEntry(financeRow);
        entry.setWbsCode(financeRow.getWbsCode()); entry.setPeriodKey(request.getPeriodKey()); entry.setAmount(nvl(request.getAmount()));
        forecastEntryRepository.save(entry);
    }

    /** Deletes every forecast value of one WBS row. The Finance WBS node itself is never deleted here. */
    public int deleteForecastEntries(Long projectId, Long financeEntryId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        List<FinanceEntry> rows = financeService.loadWbsRows(project);
        findRow(rows, financeEntryId);
        List<ForecastEntry> entries = financeService.forecastEntriesByRow(project, rows).getOrDefault(financeEntryId, List.of());
        forecastEntryRepository.deleteAll(entries);
        return entries.size();
    }

    @Transactional(readOnly = true)
    public List<String> getPeriods(int periods, String periodType) { return buildPeriods(periods, periodType != null ? periodType : "month"); }

    private List<String> buildPeriods(int periods, String periodType) {
        int safePeriods = Math.max(1, Math.min(periods, 24));
        if ("week".equalsIgnoreCase(periodType)) return buildWeekPeriods(safePeriods);
        YearMonth start = YearMonth.now();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < safePeriods; i++) keys.add(start.plusMonths(i).toString());
        return keys;
    }

    private List<String> buildWeekPeriods(int periods) {
        LocalDate start = LocalDate.now();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < periods; i++) keys.add(FinanceService.weekKey(start.plusWeeks(i)));
        return keys;
    }

    private FinanceEntry findRow(List<FinanceEntry> rows, Long financeEntryId) {
        return rows.stream().filter(row -> row.getId().equals(financeEntryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));
    }

    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
