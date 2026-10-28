package com.ercopac.ercopac_tracker.ticketing.repository; import com.ercopac.ercopac_tracker.ticketing.domain.TicketMessage; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketMessageRepository extends JpaRepository<TicketMessage,Long> { List<TicketMessage> findByTicket_IdOrderByCreatedAtAsc(Long ticketId); List<TicketMessage> findByTicket_IdAndInternalNoteFalseOrderByCreatedAtAsc(Long ticketId); long countByTicket_IdAndCreatedAtAfterAndSender_IdNot(Long ticketId, java.time.Instant instant, Long userId);
 // First public reply on each ticket from someone other than the ticket client (the first response used for SLA)
 @org.springframework.data.jpa.repository.Query("select t.id, min(m.createdAt) from TicketMessage m join m.ticket t left join t.clientUser c where t.id in :ids and m.internalNote = false and m.deleted = false and (c is null or m.sender.id <> c.id) group by t.id")
 List<Object[]> firstResponses(@org.springframework.data.repository.query.Param("ids") Collection<Long> ids);
}
