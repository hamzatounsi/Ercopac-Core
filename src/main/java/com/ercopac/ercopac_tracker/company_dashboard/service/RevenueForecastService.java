package com.ercopac.ercopac_tracker.company_dashboard.service;

import com.ercopac.ercopac_tracker.company_dashboard.dto.RevenueForecastDto;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.repository.ForecastEntryRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class RevenueForecastService {
    private final SecurityUtils securityUtils; private final ProjectRepository projects;
    private final FinanceEntryRepository finance; private final ForecastEntryRepository forecasts;
    public RevenueForecastService(SecurityUtils securityUtils, ProjectRepository projects, FinanceEntryRepository finance, ForecastEntryRepository forecasts) {
        this.securityUtils = securityUtils; this.projects = projects; this.finance = finance; this.forecasts = forecasts;
    }
    public RevenueForecastDto get(int year) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        List<Project> orgProjects = projects.findAllByOrganisationId(orgId);
        List<FinanceEntry> rows = finance.findAllByOrganisationId(orgId);
        List<ForecastEntry> entries = forecasts.findAllByOrganisationIdAndPeriodKeyStartingWith(orgId, year + "-");
        List<YearMonth> periods = new ArrayList<>(); for (int m=1;m<=12;m++) periods.add(YearMonth.of(year,m));
        Map<Long,List<FinanceEntry>> financeByProject = new HashMap<>();
        for (FinanceEntry row: rows) financeByProject.computeIfAbsent(row.getProject().getId(), k -> new ArrayList<>()).add(row);
        Map<Long,Map<String,BigDecimal>> forecastByProject = new HashMap<>();
        for (ForecastEntry entry: entries) forecastByProject.computeIfAbsent(entry.getProject().getId(), k -> new HashMap<>()).merge(entry.getPeriodKey(), nvl(entry.getAmount()), BigDecimal::add);
        List<RevenueForecastDto.Project> result = orgProjects.stream().map(p -> {
            List<FinanceEntry> pRows = financeByProject.getOrDefault(p.getId(), List.of());
            BigDecimal actual = sum(pRows.stream().map(FinanceEntry::getActualCost).toList());
            BigDecimal budget = sum(pRows.stream().map(FinanceEntry::getBudget).toList());
            Map<String,BigDecimal> pFc = forecastByProject.getOrDefault(p.getId(), Map.of());
            List<BigDecimal> monthly = periods.stream().map(x -> nvl(pFc.get(x.toString()))).toList();
            return new RevenueForecastDto.Project(p.getId(), p.getName(), p.getCode(), health(p), actual, budget, sum(monthly), monthly);
        }).sorted(Comparator.comparing(RevenueForecastDto.Project::name, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
        List<RevenueForecastDto.Month> months = periods.stream().map(p -> new RevenueForecastDto.Month(p.toString(), p.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                sum(result.stream().map(r -> r.monthlyForecast().get(p.getMonthValue()-1)).toList()))).toList();
        BigDecimal actual = sum(result.stream().map(RevenueForecastDto.Project::actual).toList());
        BigDecimal forecast = sum(result.stream().map(RevenueForecastDto.Project::forecast).toList());
        BigDecimal budget = sum(result.stream().map(RevenueForecastDto.Project::budget).toList());
        return new RevenueForecastDto(year, actual, forecast, budget, budget.subtract(actual.add(forecast)), months, result);
    }
    private BigDecimal sum(List<BigDecimal> values) { return values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String health(Project p) { if (p.getPlannedEnd()!=null && p.getPlannedEnd().isBefore(java.time.LocalDate.now())) return "Delayed"; return "HIGH".equalsIgnoreCase(p.getRiskLevel()) || "MEDIUM".equalsIgnoreCase(p.getRiskLevel()) ? "At risk" : "On track"; }
}
