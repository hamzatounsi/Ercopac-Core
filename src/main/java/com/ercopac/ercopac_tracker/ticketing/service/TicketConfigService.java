package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class TicketConfigService {

    private final TicketStatusConfigRepository statusConfigRepo;
    private final TicketPriorityConfigRepository priorityConfigRepo;

    public TicketConfigService(
            TicketStatusConfigRepository statusConfigRepo,
            TicketPriorityConfigRepository priorityConfigRepo) {
        this.statusConfigRepo = statusConfigRepo;
        this.priorityConfigRepo = priorityConfigRepo;
    }

    // ============ STATUSES ============
    
    public List<TicketStatusConfig> getAllStatuses() {
        return statusConfigRepo.findAllByOrderByDisplayOrderAsc();
    }

    public TicketStatusConfig updateStatus(Long id, String label, String description, boolean active) {
        TicketStatusConfig config = statusConfigRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Status config not found"));
        config.setLabel(label);
        config.setDescription(description);
        config.setActive(active);
        return statusConfigRepo.save(config);
    }

    // ============ PRIORITIES ============
    
    public List<TicketPriorityConfig> getAllPriorities() {
        return priorityConfigRepo.findAll();
    }

    public TicketPriorityConfig updatePriority(Long id, int firstResponseSla, int resolutionSla) {
        TicketPriorityConfig config = priorityConfigRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Priority config not found"));
        config.setFirstResponseSlaHours(firstResponseSla);
        config.setResolutionSlaHours(resolutionSla);
        return priorityConfigRepo.save(config);
    }

    // ============ SLA CALCULATION ============
    
    public java.time.Instant calculateFirstResponseDeadline(TicketPriority priority) {
        TicketPriorityConfig config = priorityConfigRepo.findByPriority(priority)
                .orElseThrow(() -> new RuntimeException("Priority config not found"));
        return java.time.Instant.now().plusSeconds(config.getFirstResponseSlaHours() * 3600);
    }

    public java.time.Instant calculateResolutionDeadline(TicketPriority priority) {
        TicketPriorityConfig config = priorityConfigRepo.findByPriority(priority)
                .orElseThrow(() -> new RuntimeException("Priority config not found"));
        return java.time.Instant.now().plusSeconds(config.getResolutionSlaHours() * 3600);
    }
}