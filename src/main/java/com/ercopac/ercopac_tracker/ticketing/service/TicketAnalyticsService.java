package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.Ticket;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketAnalyticsService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketAnalyticsService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // --- ORGANISATION KPIS ---
    public Map<String, Object> getOrganisationKpis(Long organisationId) {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganisation().getId().equals(organisationId))
                .toList();

        long openCount = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolvedThisMonth = tickets.stream().filter(t -> 
                t.getStatus() == TicketStatus.RESOLVED && 
                t.getResolvedAt() != null && 
                t.getResolvedAt().isAfter(Instant.now().minusSeconds(30L * 24 * 60 * 60))
        ).count();

        // Simulation SLA Compliance (à affiner avec tes champs slaDeadline)
        double slaCompliance = 92.5; 
        double avgResolutionHours = 14.2;

        return Map.of(
                "openTickets", openCount,
                "resolvedThisMonth", resolvedThisMonth,
                "slaCompliance", slaCompliance,
                "avgResolutionHours", avgResolutionHours
        );
    }

    // --- STATUS DISTRIBUTION ---
    public Map<String, Long> getStatusDistribution(Long organisationId) {
        return ticketRepository.findAll().stream()
                .filter(t -> t.getOrganisation().getId().equals(organisationId))
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
    }

    // --- TEAM KPIS (Agents) ---
    public Map<String, Object> getTeamKpis(Long organisationId) {
        long agentsOnline = userRepository.findAll().stream()
                .filter(u -> u.getOrganisation() != null && u.getOrganisation().getId().equals(organisationId))
                .filter(u -> u.getRoles().contains(Role.H24) || u.getRoles().contains(Role.H24_LEAD))
                .filter(u -> "ONLINE".equals(u.getAgentStatus()))
                .count();

        return Map.of(
                "agentsOnline", agentsOnline,
                "totalAgents", 5L, // À dynamiser si besoin
                "avgSatisfaction", 4.8
        );
    }

    // --- AGENT WORKLOAD ---
    public List<Map<String, Object>> getAgentWorkload(Long organisationId) {
        List<AppUser> agents = userRepository.findAll().stream()
                .filter(u -> u.getOrganisation() != null && u.getOrganisation().getId().equals(organisationId))
                .filter(u -> u.getRoles().contains(Role.H24) || u.getRoles().contains(Role.H24_LEAD))
                .toList();

        return agents.stream().map(agent -> {
            long activeTickets = ticketRepository.findAll().stream()
                    .filter(t -> t.getOrganisation().getId().equals(organisationId))
                    .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(agent.getId()))
                    .filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS)
                    .count();
            
            return Map.<String, Object>of(
                    "name", agent.getFullName(),
                    "activeTickets", activeTickets,
                    "status", agent.getAgentStatus() != null ? agent.getAgentStatus() : "OFFLINE",
                    "capacity", 5 // maxConcurrentTickets
            );
        }).collect(Collectors.toList());
    }
}