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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForecastService {

    private final ForecastEntryRepository forecastEntryRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public ForecastService(ForecastEntryRepository forecastEntryRepository,
                           FinanceEntryRepository financeEntryRepository,
                           ProjectRepository projectRepository,
                           SecurityUtils securityUtils) {
        this.forecastEntryRepository = forecastEntryRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ForecastRowDto> getForecastGrid(Long projectId, int periods) {
        Project project = getAccessibleProject(projectId);
        Long orgId = project.getOrganisation().getId();

        List<FinanceEntry> financeRows = securityUtils.isPlatformUser()
                ? financeEntryRepository.findAllByProjectIdOrderByWbsCodeAsc(projectId)
                : financeEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, orgId);

        List<ForecastEntry> fcRows = securityUtils.isPlatformUser()
                ? forecastEntryRepository.findAllByProjectIdOrderByWbsCodeAscPeriodKeyAsc(projectId)
                : forecastEntryRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAscPeriodKeyAsc(projectId, orgId);

        Map<String, Map<String, BigDecimal>> fcMap = new HashMap<>();
        for (ForecastEntry entry : fcRows) {
            fcMap.computeIfAbsent(entry.getWbsCode(), k -> new HashMap<>())
                    .put(entry.getPeriodKey(), nvl(entry.getAmount()));
        }

        List<String> periodKeys = buildPeriods(periods);
        List<ForecastRowDto> result = new ArrayList<>();

        for (FinanceEntry row : financeRows) {
            ForecastRowDto dto = new ForecastRowDto();
            dto.setWbsCode(row.getWbsCode());
            dto.setDescription(row.getDescription());
            dto.setLevel(row.getLevel());
            dto.setBudget(nvl(row.getBudget()));
            dto.setActualCost(nvl(row.getActualCost()));

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

        return result;
    }

    @Transactional(readOnly = true)
    public ForecastSummaryDto getSummary(Long projectId, int periods) {
        List<ForecastRowDto> rows = getForecastGrid(projectId, periods);

        BigDecimal totalForecast = BigDecimal.ZERO;
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;

        for (ForecastRowDto row : rows) {
            totalForecast = totalForecast.add(nvl(row.getTotalForecast()));
            totalBudget = totalBudget.add(nvl(row.getBudget()));
            totalActual = totalActual.add(nvl(row.getActualCost()));
        }

        ForecastSummaryDto dto = new ForecastSummaryDto();
        dto.setTotalForecast(totalForecast);
        dto.setTotalBudget(totalBudget);
        dto.setTotalActualCost(totalActual);

        BigDecimal eac = totalActual.add(totalForecast);
        dto.setTotalEac(eac);
        dto.setTotalVariance(totalBudget.subtract(eac));
        return dto;
    }

    public void upsertForecast(Long projectId, UpsertForecastEntryRequest request) {
        Project project = getAccessibleProject(projectId);
        Long orgId = project.getOrganisation().getId();

        ForecastEntry entry = securityUtils.isPlatformUser()
                ? forecastEntryRepository.findByProjectIdAndWbsCodeAndPeriodKey(
                    projectId, request.getWbsCode(), request.getPeriodKey())
                    .orElseGet(ForecastEntry::new)
                : forecastEntryRepository.findByProjectIdAndOrganisationIdAndWbsCodeAndPeriodKey(
                    projectId, orgId, request.getWbsCode(), request.getPeriodKey())
                    .orElseGet(ForecastEntry::new);

        entry.setProject(project);
        entry.setOrganisation(project.getOrganisation());
        entry.setWbsCode(request.getWbsCode());
        entry.setPeriodKey(request.getPeriodKey());
        entry.setAmount(nvl(request.getAmount()));

        forecastEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<String> getPeriods(int periods) {
        return buildPeriods(periods);
    }

    private List<String> buildPeriods(int periods) {
        int safePeriods = Math.max(1, Math.min(periods, 24));
        YearMonth start = YearMonth.now();

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < safePeriods; i++) {
            keys.add(start.plusMonths(i).toString());
        }
        return keys;
    }

    private Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }

        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }

        return projectRepository.findByIdAndOrganisationId(projectId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}