package com.ercopac.ercopac_tracker.ticketing.repository;

import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriorityConfig;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketPriorityConfigRepository extends JpaRepository<TicketPriorityConfig, Long> {
    Optional<TicketPriorityConfig> findByPriority(TicketPriority priority);
}