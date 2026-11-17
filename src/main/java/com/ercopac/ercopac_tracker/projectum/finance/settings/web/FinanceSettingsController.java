package com.ercopac.ercopac_tracker.projectum.finance.settings.web;

import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.FinanceSettingsDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ForecastBaselineDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ImportFinanceWbsTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.PaymentScheduleDto; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SaveFinanceSettingsRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SavePaymentScheduleRequest; // ✅ NOUVEAU
import com.ercopac.ercopac_tracker.projectum.finance.settings.service.FinanceSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/settings")
public class FinanceSettingsController {

    private static final String FINANCE_READ =
            "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FINANCE)";
    private static final String FINANCE_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FINANCE)";

    private final FinanceSettingsService financeSettingsService;

    public FinanceSettingsController(FinanceSettingsService financeSettingsService) {
        this.financeSettingsService = financeSettingsService;
    }

    @GetMapping
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinanceSettingsDto> getSettings(@RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(financeSettingsService.getSettings(projectId));
    }

    @PutMapping
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceSettingsDto> saveSettings(
            @RequestParam(required = false) Long projectId,
            @Valid @RequestBody SaveFinanceSettingsRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.saveSettings(projectId, request));
    }

    @PostMapping("/apply-template")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<ApplyFinanceTemplateResultDto> applyTemplate(
            @RequestParam Long projectId,
            @RequestBody ApplyFinanceTemplateRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.applyTemplate(projectId, request));
    }

    @PostMapping("/import-wbs")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<FinanceSettingsDto> importWbsTemplate(
            @RequestParam(required = false) Long projectId,
            @Valid @RequestBody ImportFinanceWbsTemplateRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.importWbsTemplate(projectId, request));
    }

    // ==========================================
    // ✅ NOUVEAUX : Endpoints pour les Payments
    // ==========================================

    @GetMapping("/payments")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PaymentScheduleDto> getPaymentSchedule(@RequestParam Long projectId) {
        return ResponseEntity.ok(financeSettingsService.getPaymentSchedule(projectId));
    }

    @PutMapping("/payments")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<PaymentScheduleDto> savePaymentSchedule(
            @RequestParam Long projectId,
            @Valid @RequestBody SavePaymentScheduleRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.savePaymentSchedule(projectId, request));
    }
    @GetMapping("/forecast-baseline")
    @PreAuthorize(FINANCE_READ)
    public ResponseEntity<ForecastBaselineDto> getForecastBaseline(@RequestParam Long projectId) {
        return ResponseEntity.ok(financeSettingsService.getForecastBaseline(projectId));
    }

    @PostMapping("/forecast-baseline/set")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<ForecastBaselineDto> setForecastBaseline(@RequestParam Long projectId) {
        return ResponseEntity.ok(financeSettingsService.setForecastBaseline(projectId));
    }

    @PostMapping("/forecast-baseline/reset")
    @PreAuthorize(FINANCE_WRITE)
    public ResponseEntity<ForecastBaselineDto> resetForecastBaseline(@RequestParam Long projectId) {
        return ResponseEntity.ok(financeSettingsService.resetForecastBaseline(projectId));
    }
}