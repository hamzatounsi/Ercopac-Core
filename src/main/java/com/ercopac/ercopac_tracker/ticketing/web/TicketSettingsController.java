package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.TicketAnalyticsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketSettingsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketTranslationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

// Ticketing settings (status wording, priority SLA targets, support team) and the performance dashboard.
// Reading the settings is open to every Ticketing user; changes and the dashboard are for support leads.
@RestController
@RequestMapping("/api/tickets")
public class TicketSettingsController {

    private static final String STANDARD_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24', 'H24_LEAD', 'CLIENT')";
    private static final String STAFF_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24', 'H24_LEAD')";
    private static final String LEAD_ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24_LEAD')";

    private final TicketSettingsService settings;
    private final TicketAnalyticsService analytics;
    private final TicketTranslationService translation;

    public TicketSettingsController(TicketSettingsService settings, TicketAnalyticsService analytics, TicketTranslationService translation) {
        this.settings = settings;
        this.analytics = analytics;
        this.translation = translation;
    }

    @GetMapping("/settings")
    @PreAuthorize(STANDARD_ACCESS)
    public TicketSettingsDto settings() {
        return settings.settings();
    }

    @PutMapping("/settings/statuses")
    @PreAuthorize(LEAD_ACCESS)
    public TicketSettingsDto saveStatuses(@Valid @RequestBody List<@Valid StatusSettingDto> request) {
        return settings.saveStatuses(request);
    }

    @PutMapping("/settings/priorities")
    @PreAuthorize(LEAD_ACCESS)
    public TicketSettingsDto savePriorities(@Valid @RequestBody List<@Valid PrioritySettingDto> request) {
        return settings.savePriorities(request);
    }

    @GetMapping("/settings/agents")
    @PreAuthorize(STAFF_ACCESS)
    public List<AgentDto> agents() {
        return settings.agents();
    }

    @PutMapping("/settings/agents/{userId}")
    @PreAuthorize(LEAD_ACCESS)
    public AgentDto updateAgent(@PathVariable Long userId, @Valid @RequestBody AgentProfileRequest request) {
        return settings.updateAgent(userId, request);
    }

    @GetMapping("/settings/me")
    @PreAuthorize(STANDARD_ACCESS)
    public MySettingsDto me() {
        return settings.me();
    }

    @PutMapping("/settings/me/language")
    @PreAuthorize(STANDARD_ACCESS)
    public MySettingsDto myLanguage(@Valid @RequestBody LanguageRequest request) {
        return settings.updateMyLanguage(request);
    }

    @PostMapping("/translate")
    @PreAuthorize(STANDARD_ACCESS)
    public TranslateResponse translate(@Valid @RequestBody TranslateRequest request) {
        return new TranslateResponse(translation.translate(request.text(), request.to()));
    }

    @GetMapping("/analytics")
    @PreAuthorize(LEAD_ACCESS)
    public TicketAnalytics analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) String serial) {
        return analytics.analytics(from, to, customerId, equipmentTypeId, serial);
    }
}
