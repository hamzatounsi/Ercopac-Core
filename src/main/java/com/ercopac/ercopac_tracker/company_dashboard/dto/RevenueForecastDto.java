package com.ercopac.ercopac_tracker.company_dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Read-only, organisation-scoped portfolio forecast used by Command Center. */
public record RevenueForecastDto(int year, BigDecimal totalActual, BigDecimal totalForecast,
                                 BigDecimal totalBudget, BigDecimal variance,
                                 List<Month> months, List<Project> projects) {
    public record Month(String key, String label, BigDecimal forecast) {}
    public record Project(Long id, String name, String code, String status, BigDecimal actual,
                          BigDecimal budget, BigDecimal forecast, List<BigDecimal> monthlyForecast) {}
}
