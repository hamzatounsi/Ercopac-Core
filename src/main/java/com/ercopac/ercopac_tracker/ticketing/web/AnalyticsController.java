package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.ticketing.service.TicketAnalyticsService;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ticketing/analytics")
public class AnalyticsController {

    private final TicketAnalyticsService analyticsService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public AnalyticsController(TicketAnalyticsService analyticsService, UserRepository userRepository, SecurityUtils securityUtils) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    private Long getCurrentOrgId() {
        AppUser user = userRepository.findById(securityUtils.getCurrentUserId()).orElseThrow();
        return user.getOrganisation() != null ? user.getOrganisation().getId() : 1L;
    }

    // --- Ces endpoints complètent ton /api/tickets/statistics existant ---
    
    @GetMapping("/team-kpis")
    public ResponseEntity<Map<String, Object>> getTeamKpis() {
        return ResponseEntity.ok(analyticsService.getTeamKpis(getCurrentOrgId()));
    }

    @GetMapping("/agent-workload")
    public ResponseEntity<?> getAgentWorkload() {
        return ResponseEntity.ok(analyticsService.getAgentWorkload(getCurrentOrgId()));
    }
}