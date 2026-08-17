package com.ercopac.ercopac_tracker.projectum.finance.settings.web;

import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.FinanceSettingsDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ImportFinanceWbsTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SaveFinanceSettingsRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.service.FinanceSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/settings")
public class FinanceSettingsController {

    private final FinanceSettingsService financeSettingsService;

    public FinanceSettingsController(FinanceSettingsService financeSettingsService) {
        this.financeSettingsService = financeSettingsService;
    }

    @GetMapping
    public ResponseEntity<FinanceSettingsDto> getSettings(@RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(financeSettingsService.getSettings(projectId));
    }

    @PutMapping
    public ResponseEntity<FinanceSettingsDto> saveSettings(
            @RequestParam(required = false) Long projectId,
            @Valid @RequestBody SaveFinanceSettingsRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.saveSettings(projectId, request));
    }

    @PostMapping("/apply-template")
    public ResponseEntity<ApplyFinanceTemplateResultDto> applyTemplate(
            @RequestParam Long projectId, // ✅ Requis pour savoir quel projet appliquer
            @RequestBody ApplyFinanceTemplateRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.applyTemplate(projectId, request));
    }

    @PostMapping("/import-wbs")
    public ResponseEntity<FinanceSettingsDto> importWbsTemplate(
            @RequestParam(required = false) Long projectId,
            @Valid @RequestBody ImportFinanceWbsTemplateRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.importWbsTemplate(projectId, request));
    }
}