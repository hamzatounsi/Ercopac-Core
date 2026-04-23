package com.ercopac.ercopac_tracker.projectum.finance.settings.web;

import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.FinanceSettingsDto;
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
    public ResponseEntity<FinanceSettingsDto> getSettings() {
        return ResponseEntity.ok(financeSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<FinanceSettingsDto> saveSettings(
            @Valid @RequestBody SaveFinanceSettingsRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.saveSettings(request));
    }

    @PostMapping("/apply-template")
    public ResponseEntity<ApplyFinanceTemplateResultDto> applyTemplate(
            @RequestBody ApplyFinanceTemplateRequest request
    ) {
        return ResponseEntity.ok(financeSettingsService.applyTemplate(request));
    }
}