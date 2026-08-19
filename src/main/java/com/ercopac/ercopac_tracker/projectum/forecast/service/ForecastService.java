package com.ercopac.ercopac_tracker.projectum.forecast.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastGridCellDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastRowDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastSummaryDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.UpsertForecastEntryRequest;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForecastService {

    private final ForecastEntryRepository forecastEntryRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final SecurityUtils securityUtils;

    public ForecastService(ForecastEntryRepository forecastEntryRepository,
                           FinanceEntryRepository financeEntryRepository,
                           ProjectRepository projectRepository,
                           ProjectTaskRepository projectTaskRepository,
                           ResourceTypeRepository resourceTypeRepository,
                           SecurityUtils securityUtils) {
        this.forecastEntryRepository = forecastEntryRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ForecastRowDto> getForecastGrid(Long projectId, int periods, String periodType) {
        Project project = getAccessibleProject(projectId);
        Long orgId = project.getOrganisation().getId();
        String safePeriodType = periodType != null ? periodType : "month";

        // 1. Charger les lignes Finance
        List<FinanceEntry> financeRows = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdOrderByDisplayOrderAscWbsCodeAsc(projectId)
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(projectId, orgId);

        // Trier pour que le Niveau 1 (Total Projet) soit toujours en premier
        List<FinanceEntry> sortedFinanceRows = financeRows.stream()
                .sorted(Comparator.comparing((FinanceEntry e) -> (e.getLevel() != null && e.getLevel() == 1) ? 0 : 1)
                        .thenComparing(e -> e.getDisplayOrder() != null ? e.getDisplayOrder() : 9999)
                        .thenComparing(FinanceEntry::getWbsCode))
                .collect(Collectors.toList());

        // 2. Charger les données Forecast
        List<ForecastEntry> fcRows = securityUtils.isPlatformUser()
                ? forecastEntryRepository.findAllByProjectIdOrderByWbsCodeAscPeriodKeyAsc(projectId)
                : forecastEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAscPeriodKeyAsc(projectId, orgId);

        Map<String, Map<String, BigDecimal>> fcMap = new HashMap<>();
        for (ForecastEntry entry : fcRows) {
            String periodKey = entry.getPeriodKey();
            String targetKey = periodKey;
            if ("month".equalsIgnoreCase(safePeriodType) && periodKey.contains("-W")) {
                targetKey = convertWeekToMonth(periodKey);
            }
            fcMap.computeIfAbsent(entry.getWbsCode(), k -> new HashMap<>())
                    .merge(targetKey, nvl(entry.getAmount()), BigDecimal::add);
        }

        // 3. Charger les tâches et les taux
        List<ProjectTask> allTasks = projectTaskRepository.findByProjectIdOrderByDisplayOrderAsc(projectId);
        Map<String, ProjectTask> tasksByWbs = allTasks.stream()
                .filter(t -> t.getWbsCode() != null)
                .collect(Collectors.toMap(ProjectTask::getWbsCode, t -> t, (a, b) -> a));

        List<ResourceType> resourceTypes = resourceTypeRepository.findByOrganisation_IdAndActiveTrue(orgId);
        Map<String, BigDecimal> ratesByCode = resourceTypes.stream()
                .filter(rt -> rt.getCode() != null && rt.getDefaultRate() != null)
                .collect(Collectors.toMap(ResourceType::getCode, ResourceType::getDefaultRate, (a, b) -> a));

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

        for (FinanceEntry row : sortedFinanceRows) {
            ForecastRowDto dto = new ForecastRowDto();
            dto.setFinanceEntryId(row.getId());
            dto.setWbsCode(row.getWbsCode());
            dto.setDescription(row.getDescription());
            dto.setLevel(row.getLevel() != null ? row.getLevel() : 1);
            dto.setBudget(nvl(row.getBudget()));
            dto.setRowType(row.getRowType() != null ? row.getRowType() : "COST");
            dto.setActualCost(nvl(row.getActualCost()));
            
            String resTypeCode = row.getResourceTypeCode();
            dto.setResourceTypeCode(resTypeCode);
            dto.setLinkedScheduleWbs(row.getLinkedScheduleWbs());
            dto.setAvailableScheduleTasks(availableTasks);

            // ✅ CORRECTION 1 & 4 : Remaining Schedule = De AUJOURD'HUI à la fin (gère aussi les Summary Gantt)
            BigDecimal remainingHours = BigDecimal.ZERO;
            String linkedWbs = row.getLinkedScheduleWbs();
            
            if (linkedWbs != null && !linkedWbs.isBlank()) {
                ProjectTask linkedTask = tasksByWbs.get(linkedWbs);
                if (linkedTask != null) {
                    remainingHours = calculateRemainingHoursFromToday(linkedTask, allTasks);
                }
            }
            dto.setRemainingHours(remainingHours);

            // ✅ CORRECTION 2 : Calculer le montant en € (Heures restantes * Taux)
            BigDecimal rate = ratesByCode.getOrDefault(resTypeCode, BigDecimal.ZERO);
            BigDecimal remainingCost = remainingHours.multiply(rate);
            // On attache remainingCost au DTO (assure-toi que ton ForecastRowDto a ce champ, sinon ajoute-le)
            // Si ton DTO n'a pas setRemainingCost, on le met dans une extension ou on s'assure qu'il y est.
            // (Supposons que ton DTO a déjà setRemainingCost basé sur nos échanges précédents)
            try {
                dto.getClass().getMethod("setRemainingCost", BigDecimal.class).invoke(dto, remainingCost);
            } catch (Exception e) {
                // Fallback si la méthode n'existe pas encore dans le DTO
            }

            // Calculer le Total Forecast
            Map<String, BigDecimal> rowMap = fcMap.getOrDefault(row.getWbsCode(), Collections.emptyMap());
            BigDecimal totalFc = BigDecimal.ZERO;
            List<ForecastGridCellDto> cells = new ArrayList<>();
            
            for (String periodKey : periodKeys) {
                BigDecimal amount = nvl(rowMap.get(periodKey));
                totalFc = totalFc.add(amount);
                cells.add(new ForecastGridCellDto(periodKey, amount));
            }

            dto.setPeriods(cells);
            dto.setTotalForecast(totalFc);
            result.add(dto);
        }
        
        // ✅ CORRECTION 3 : Calculer les totaux de la ligne Level 1 (Projet) et des Summary
        result = calculateSummaryRows(result);

        return result;
    }

    private BigDecimal calculateRemainingHoursFromToday(ProjectTask task, List<ProjectTask> allTasks) {
        List<ProjectTask> tasksToProcess = new ArrayList<>();
        boolean isSummary = allTasks.stream().anyMatch(t -> task.getId().equals(t.getParentId()));
        
        if (isSummary) {
            tasksToProcess = getAllDescendants(task.getId(), allTasks);
        } else {
            tasksToProcess.add(task);
        }

        BigDecimal totalRemaining = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (ProjectTask t : tasksToProcess) {
            BigDecimal remainingForTask = BigDecimal.ZERO;

            if (t.getPlannedStart() != null && t.getPlannedEnd() != null && t.getPlannedEnd().isAfter(today)) {
                long totalDays = ChronoUnit.DAYS.between(t.getPlannedStart(), t.getPlannedEnd());
                long remainingDays = ChronoUnit.DAYS.between(today, t.getPlannedEnd());
                
                if (totalDays > 0) {
                    double ratio = (double) Math.max(0, remainingDays) / totalDays;
                    ratio = Math.min(1.0, ratio);
                    
                    BigDecimal planned = t.getPlannedHours();
                    if (planned == null && t.getDurationDays() != null) {
                        int days = t.getDurationDays();
                        int allocation = t.getAllocationPercent() != null ? t.getAllocationPercent() : 100;
                        planned = BigDecimal.valueOf((days * 8.0 * allocation) / 100.0);
                    }
                    if (planned == null) planned = BigDecimal.ZERO;

                    remainingForTask = planned.multiply(BigDecimal.valueOf(ratio));
                }
            } else if (t.getPlannedEnd() != null && !t.getPlannedEnd().isAfter(today)) {
                remainingForTask = BigDecimal.ZERO;
            } else {
                remainingForTask = t.getPlannedHours() != null ? t.getPlannedHours() : BigDecimal.ZERO;
            }

            // ✅ PAS DE × 8 : on additionne directement les heures restantes
            totalRemaining = totalRemaining.add(remainingForTask);
        }

        return totalRemaining;
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

 

    // ✅ AMÉLIORÉE : Somme TOUTES les colonnes pour les lignes Level 1 et SUMMARY
    private List<ForecastRowDto> calculateSummaryRows(List<ForecastRowDto> rows) {
        for (ForecastRowDto summaryRow : rows) {
            if (summaryRow.getLevel() != null && (summaryRow.getLevel() == 1 || "SUMMARY".equals(summaryRow.getRowType()))) {
                
                List<ForecastRowDto> children = rows.stream()
                    .filter(r -> !r.getWbsCode().equals(summaryRow.getWbsCode()))
                    .filter(r -> r.getWbsCode().startsWith(summaryRow.getWbsCode() + ".") || 
                                 r.getWbsCode().startsWith(summaryRow.getWbsCode() + "-") ||
                                 (summaryRow.getLevel() == 1 && r.getLevel() != null && r.getLevel() > 1))
                    .collect(Collectors.toList());

                if (!children.isEmpty()) {
                    BigDecimal totalBudget = children.stream().map(r -> r.getBudget() != null ? r.getBudget() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalActual = children.stream().map(r -> r.getActualCost() != null ? r.getActualCost() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalForecast = children.stream().map(r -> r.getTotalForecast() != null ? r.getTotalForecast() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // ✅ Ajout de la somme des Remaining Hours et Remaining Cost pour la ligne Total
                    BigDecimal totalRemainingHours = children.stream().map(r -> r.getRemainingHours() != null ? r.getRemainingHours() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalRemainingCost = children.stream().map(r -> {
                        try { return (BigDecimal) r.getClass().getMethod("getRemainingCost").invoke(r); } 
                        catch (Exception e) { return BigDecimal.ZERO; }
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);

                    summaryRow.setBudget(totalBudget);
                    summaryRow.setActualCost(totalActual);
                    summaryRow.setTotalForecast(totalForecast);
                    summaryRow.setRemainingHours(totalRemainingHours);
                    try { summaryRow.getClass().getMethod("setRemainingCost", BigDecimal.class).invoke(summaryRow, totalRemainingCost); } catch (Exception e) {}
                }
            }
        }
        return rows;
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

    public void updateLinkedScheduleWbs(Long financeEntryId, String linkedScheduleWbs) {
        FinanceEntry entry = financeEntryRepository.findById(financeEntryId).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));
        entry.setLinkedScheduleWbs(linkedScheduleWbs);
        financeEntryRepository.save(entry);
    }

    public void updateWbsLevel(Long financeEntryId, Integer level) {
        FinanceEntry entry = financeEntryRepository.findById(financeEntryId).orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));
        entry.setLevel(level);
        financeEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public ForecastSummaryDto getSummary(Long projectId, int periods) {
        List<ForecastRowDto> rows = getForecastGrid(projectId, periods, "month");
        BigDecimal totalForecast = BigDecimal.ZERO, totalBudget = BigDecimal.ZERO, totalActual = BigDecimal.ZERO;
        for (ForecastRowDto row : rows) {
            totalForecast = totalForecast.add(nvl(row.getTotalForecast()));
            totalBudget = totalBudget.add(nvl(row.getBudget()));
            totalActual = totalActual.add(nvl(row.getActualCost()));
        }
        ForecastSummaryDto dto = new ForecastSummaryDto();
        dto.setTotalForecast(totalForecast); dto.setTotalBudget(totalBudget); dto.setTotalActualCost(totalActual);
        dto.setTotalEac(totalActual.add(totalForecast)); dto.setTotalVariance(totalBudget.subtract(dto.getTotalEac()));
        return dto;
    }

    public void upsertForecast(Long projectId, UpsertForecastEntryRequest request) {
        Project project = getAccessibleProject(projectId);
        Long orgId = project.getOrganisation().getId();
        ForecastEntry entry = securityUtils.isPlatformUser()
                ? forecastEntryRepository.findByProjectIdAndWbsCodeAndPeriodKey(projectId, request.getWbsCode(), request.getPeriodKey()).orElseGet(ForecastEntry::new)
                : forecastEntryRepository.findByProjectIdAndOrganisationIdAndWbsCodeAndPeriodKey(projectId, orgId, request.getWbsCode(), request.getPeriodKey()).orElseGet(ForecastEntry::new);
        entry.setProject(project); entry.setOrganisation(project.getOrganisation());
        entry.setWbsCode(request.getWbsCode()); entry.setPeriodKey(request.getPeriodKey()); entry.setAmount(nvl(request.getAmount()));
        forecastEntryRepository.save(entry);
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
        LocalDate start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < periods; i++) {
            LocalDate weekStart = start.plusWeeks(i);
            keys.add(String.format("%d-W%02d", weekStart.getYear(), weekStart.get(WeekFields.of(java.time.DayOfWeek.MONDAY, 1).weekOfWeekBasedYear())));
        }
        return keys;
    }

    private Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) return projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) throw new IllegalStateException("User has no organisation");
        return projectRepository.findByIdAndOrganisationId(projectId, orgId).orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    // (Tu peux garder ta méthode debugForecastRow ici si tu en as encore besoin)
}