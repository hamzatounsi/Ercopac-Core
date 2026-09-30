package com.ercopac.ercopac_tracker.ticketing.repository;

import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatusConfig;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketStatusConfigRepository extends JpaRepository<TicketStatusConfig, Long> {
    List<TicketStatusConfig> findAllByOrderByDisplayOrderAsc();
    Optional<TicketStatusConfig> findByStatus(TicketStatus status);
}