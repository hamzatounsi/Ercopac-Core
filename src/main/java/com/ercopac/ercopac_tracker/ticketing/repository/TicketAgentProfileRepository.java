package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketAgentProfile; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketAgentProfileRepository extends JpaRepository<TicketAgentProfile,Long> { List<TicketAgentProfile> findByOrganisation_Id(Long org); Optional<TicketAgentProfile> findByUser_Id(Long userId); }
