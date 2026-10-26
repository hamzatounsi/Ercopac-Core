package com.ercopac.ercopac_tracker.projectum.finance.web;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceCostBreakdownDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDetailDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceImportResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceProjectChartDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceSummaryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.UpsertFinanceEntryRequest;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/finance")
public class FinanceController {

    private static final String FINANCE_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FINANCE)";

    private static final String FINANCE_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FINANCE)";

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<List<FinanceEntryDto>> getFinanceRows(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectFinance(projectId));
    }
    

    @GetMapping("/summary")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinanceSummaryDto> getFinanceSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectFinanceSummary(projectId));
    }

    @PostMapping
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceEntryDto> createFinanceEntry(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertFinanceEntryRequest request
    ) {
        return ResponseEntity.ok(financeService.createEntry(projectId, request));
    }

    @PutMapping("/{entryId}")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceEntryDto> updateFinanceEntry(
            @PathVariable Long projectId,
            @PathVariable Long entryId,
            @Valid @RequestBody UpsertFinanceEntryRequest request
    ) {
        return ResponseEntity.ok(financeService.updateEntry(projectId, entryId, request));
    }

    @PatchMapping("/{entryId}/forecast")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceEntryDto> updateForecastValue(@PathVariable Long projectId,
            @PathVariable Long entryId, @RequestBody Map<String, BigDecimal> request) {
        return ResponseEntity.ok(financeService.updateForecastValue(projectId, entryId, request.get("forecast")));
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<Void> deleteFinanceEntry(
            @PathVariable Long projectId,
            @PathVariable Long entryId
    ) {
        financeService.deleteEntry(projectId, entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceImportResultDto> importFinance(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "false") boolean overwrite,
            @RequestBody List<UpsertFinanceEntryRequest> rows
    ) {
        return ResponseEntity.ok(financeService.importEntries(projectId, rows, overwrite));
    }

    @GetMapping("/{entryId}/detail")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinanceEntryDetailDto> getFinanceEntryDetail(
            @PathVariable Long projectId,
            @PathVariable Long entryId
    ) {
        return ResponseEntity.ok(financeService.getEntryDetail(projectId, entryId));
    }

    @GetMapping("/analytics/cost-breakdown")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinanceCostBreakdownDto> getCostBreakdown(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getCostBreakdown(projectId));
    }

    @GetMapping("/analytics/project-overview")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinanceProjectChartDto> getProjectOverview(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectOverview(projectId));
    }

    @PostMapping("/recalculate-labour")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<Void> recalculateLabour(@PathVariable Long projectId) {
        financeService.recalculateLabourRowsFromTasks(projectId);
        return ResponseEntity.ok().build();
    }
}
