package com.ercopac.ercopac_tracker.ticketing.repository; import com.ercopac.ercopac_tracker.ticketing.domain.TicketReadState; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketReadStateRepository extends JpaRepository<TicketReadState,Long> { Optional<TicketReadState> findByTicket_IdAndUser_Id(Long ticketId,Long userId); }
