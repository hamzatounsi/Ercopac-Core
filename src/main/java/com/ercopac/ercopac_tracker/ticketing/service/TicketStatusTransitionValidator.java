package com.ercopac.ercopac_tracker.ticketing.service;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus; import org.springframework.stereotype.Component; import java.util.*;
@Component public class TicketStatusTransitionValidator { private static final Map<TicketStatus,Set<TicketStatus>> ALLOWED=Map.of(
 TicketStatus.OPEN,EnumSet.of(TicketStatus.IN_PROGRESS,TicketStatus.ESCALATED,TicketStatus.CANCELLED), TicketStatus.IN_PROGRESS,EnumSet.of(TicketStatus.ESCALATED,TicketStatus.RESOLVED,TicketStatus.CANCELLED), TicketStatus.ESCALATED,EnumSet.of(TicketStatus.IN_PROGRESS,TicketStatus.RESOLVED), TicketStatus.RESOLVED,EnumSet.of(TicketStatus.REOPENED,TicketStatus.CLOSED), TicketStatus.REOPENED,EnumSet.of(TicketStatus.IN_PROGRESS), TicketStatus.CLOSED,EnumSet.noneOf(TicketStatus.class), TicketStatus.CANCELLED,EnumSet.noneOf(TicketStatus.class));
 public void validate(TicketStatus from,TicketStatus to,boolean mayReopenClosed){ if(from==to)return; if(from==TicketStatus.CLOSED&&to==TicketStatus.REOPENED&&mayReopenClosed)return; if(!ALLOWED.getOrDefault(from,Set.of()).contains(to))throw new TicketConflictException("Transition from "+from+" to "+to+" is not permitted."); }
}
