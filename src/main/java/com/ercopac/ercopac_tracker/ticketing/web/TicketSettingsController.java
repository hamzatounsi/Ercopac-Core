package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.TicketAccountService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketAnalyticsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketConfigService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketSettingsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketStatusService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketTranslationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

// Ticketing settings (statuses, priority SLA targets, support team) and the performance dashboard.
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
    private final TicketConfigService config;
    private final TicketAccountService accounts;
    private final TicketStatusService statuses;

    public TicketSettingsController(TicketSettingsService settings, TicketAnalyticsService analytics, TicketTranslationService translation, TicketConfigService config, TicketAccountService accounts, TicketStatusService statuses) {
        this.statuses = statuses;
        this.config = config;
        this.accounts = accounts;
        this.settings = settings;
        this.analytics = analytics;
        this.translation = translation;
    }

    @GetMapping("/settings")
    @PreAuthorize(STANDARD_ACCESS)
    public TicketSettingsDto settings() {
        return withConfig(settings.settings());
    }

    // Priority settings plus the organisation's statuses (all of them, inactive included, for labels) and categories
    private TicketSettingsDto withConfig(TicketSettingsDto base) {
        return new TicketSettingsDto(statuses.list(false), base.priorities(), config.categories());
    }

    @PostMapping("/settings/categories")
    @PreAuthorize(LEAD_ACCESS)
    public CategoryDto createCategory(@Valid @RequestBody CategoryRequest request) {
        return config.createCategory(request);
    }

    @PutMapping("/settings/categories/{id}")
    @PreAuthorize(LEAD_ACCESS)
    public CategoryDto updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return config.updateCategory(id, request);
    }

    /** reassignTo: category code the tickets of a category in use are moved to before it is deleted. */
    @DeleteMapping("/settings/categories/{id}")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id, @RequestParam(required = false) String reassignTo) {
        config.deleteCategory(id, reassignTo);
    }

    // ---------- ticket statuses ----------
    /** activeOnly=true: the statuses offered in dropdowns; otherwise every status (labels of old tickets, settings table). */
    @GetMapping("/statuses")
    @PreAuthorize(STANDARD_ACCESS)
    public List<TicketStatusDto> statuses(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return statuses.list(activeOnly);
    }

    @GetMapping("/statuses/active")
    @PreAuthorize(STANDARD_ACCESS)
    public List<TicketStatusDto> activeStatuses() {
        return statuses.list(true);
    }

    @PostMapping("/settings/statuses")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public TicketStatusDto createStatus(@Valid @RequestBody TicketStatusRequest request) {
        return statuses.create(request);
    }

    @PutMapping("/settings/statuses/{id}")
    @PreAuthorize(LEAD_ACCESS)
    public TicketStatusDto updateStatus(@PathVariable Long id, @Valid @RequestBody TicketStatusRequest request) {
        return statuses.update(id, request);
    }

    @PutMapping("/settings/statuses/order")
    @PreAuthorize(LEAD_ACCESS)
    public List<TicketStatusDto> reorderStatuses(@Valid @RequestBody StatusOrderRequest request) {
        return statuses.reorder(request.ids());
    }

    /** reassignTo: id of the status the tickets of a status in use are moved to; without it a status in use answers 409 with ticketCount. */
    @DeleteMapping("/settings/statuses/{id}")
    @PreAuthorize(LEAD_ACCESS)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteStatus(@PathVariable Long id, @RequestParam(required = false) Long reassignTo) {
        statuses.delete(id, reassignTo);
    }

    /** New support account (H24 agent, H24 lead or L2 technician employee) in the lead's organisation. */
    @PostMapping("/settings/accounts")
    @PreAuthorize(LEAD_ACCESS)
    public AgentDto createAccount(@Valid @RequestBody AccountRequest request) {
        return accounts.create(request);
    }

    @PutMapping("/settings/priorities")
    @PreAuthorize(LEAD_ACCESS)
    public TicketSettingsDto savePriorities(@Valid @RequestBody List<@Valid PrioritySettingDto> request) {
        return withConfig(settings.savePriorities(request));
    }

    /** group=H24 lists the H24 team (H24 / H24_LEAD role or H24 resource type); without it, the support team and L2 technicians. */
    @GetMapping("/settings/agents")
    @PreAuthorize(STAFF_ACCESS)
    public List<AgentDto> agents(@RequestParam(required = false) String group) {
        return settings.agents(group);
    }

    @GetMapping("/settings/departments")
    @PreAuthorize(LEAD_ACCESS)
    public List<Ref> departments() {
        return accounts.departments();
    }

    @GetMapping("/resource-types")
    @PreAuthorize(STAFF_ACCESS)
    public List<ResourceTypeOption> resourceTypes() {
        return settings.resourceTypes();
    }

    /** Employees of one resource type (cascading L2 choice); l2Only keeps those flagged as L2 technicians. */
    @GetMapping("/employees")
    @PreAuthorize(STAFF_ACCESS)
    public List<EmployeeOption> employees(@RequestParam Long resourceTypeId, @RequestParam(defaultValue = "false") boolean l2Only) {
        return settings.employees(resourceTypeId, l2Only);
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
