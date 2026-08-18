package com.ercopac.ercopac_tracker.company_dashboard.web;

import com.ercopac.ercopac_tracker.company_dashboard.dto.CompanyDashboardDto;
import com.ercopac.ercopac_tracker.company_dashboard.service.CompanyDashboardService;
import com.ercopac.ercopac_tracker.company_dashboard.dto.RevenueForecastDto;
import com.ercopac.ercopac_tracker.company_dashboard.service.RevenueForecastService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company-dashboard")
public class CompanyDashboardController {
    private final CompanyDashboardService service;
    private final RevenueForecastService revenueForecastService;
    public CompanyDashboardController(CompanyDashboardService service, RevenueForecastService revenueForecastService) { this.service = service; this.revenueForecastService = revenueForecastService; }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ROLE_MANAGER')")
    public CompanyDashboardDto dashboard() { return service.getDashboard(); }
    @GetMapping("/project-performance/revenue-forecast")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ROLE_MANAGER')")
    public RevenueForecastDto revenueForecast(@RequestParam(required = false) Integer year) {
        return revenueForecastService.get(year == null ? java.time.LocalDate.now().getYear() : year);
    }
}
