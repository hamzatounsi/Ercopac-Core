package com.ercopac.ercopac_tracker.ticketing.repository; import com.ercopac.ercopac_tracker.ticketing.domain.TicketActivity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketActivityRepository extends JpaRepository<TicketActivity,Long> { List<TicketActivity> findByTicket_IdOrderByCreatedAtAsc(Long ticketId); }
