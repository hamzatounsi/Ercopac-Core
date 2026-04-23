package com.ercopac.ercopac_tracker.projectum.finance.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceCostBreakdownDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDetailDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceProjectChartDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceSummaryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.UpsertFinanceEntryRequest;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/finance")
public class FinanceController {

    private static final String PROJECTUM_ACCESS =
            "hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PMO','PLATFORM_OWNER','PLATFORM_ADMIN')";

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<List<FinanceEntryDto>> getFinanceRows(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectFinance(projectId));
    }

    @GetMapping("/summary")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceSummaryDto> getFinanceSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectFinanceSummary(projectId));
    }

    @PostMapping
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceEntryDto> createFinanceEntry(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertFinanceEntryRequest request
    ) {
        return ResponseEntity.ok(financeService.createEntry(projectId, request));
    }

    @PutMapping("/{entryId}")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceEntryDto> updateFinanceEntry(
            @PathVariable Long projectId,
            @PathVariable Long entryId,
            @Valid @RequestBody UpsertFinanceEntryRequest request
    ) {
        return ResponseEntity.ok(financeService.updateEntry(projectId, entryId, request));
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<Void> deleteFinanceEntry(
            @PathVariable Long projectId,
            @PathVariable Long entryId
    ) {
        financeService.deleteEntry(projectId, entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<Void> importFinance(
            @PathVariable Long projectId,
            @RequestBody List<UpsertFinanceEntryRequest> rows
    ) {
        financeService.importEntries(projectId, rows);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{entryId}/detail")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceEntryDetailDto> getFinanceEntryDetail(
            @PathVariable Long projectId,
            @PathVariable Long entryId
    ) {
        return ResponseEntity.ok(financeService.getEntryDetail(projectId, entryId));
    }

    @GetMapping("/analytics/cost-breakdown")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceCostBreakdownDto> getCostBreakdown(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getCostBreakdown(projectId));
    }

    @GetMapping("/analytics/project-overview")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<FinanceProjectChartDto> getProjectOverview(@PathVariable Long projectId) {
        return ResponseEntity.ok(financeService.getProjectOverview(projectId));
    }

    @PostMapping("/recalculate-labour")
    @PreAuthorize(PROJECTUM_ACCESS)
    public ResponseEntity<Void> recalculateLabour(@PathVariable Long projectId) {
        financeService.recalculateLabourRowsFromTasks(projectId);
        return ResponseEntity.ok().build();
    }
}