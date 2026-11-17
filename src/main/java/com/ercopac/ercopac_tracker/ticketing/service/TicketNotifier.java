package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.service.NotificationService;
import com.ercopac.ercopac_tracker.ticketing.domain.Ticket;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Objects;

// In-app alerts about tickets (bell in the Ticketing header). A failed alert never fails the ticket change.
@Component
public class TicketNotifier {
 private static final Logger log = LoggerFactory.getLogger(TicketNotifier.class);
 private final NotificationService notifications;
 public TicketNotifier(NotificationService notifications) { this.notifications = notifications; }

 public void notify(Ticket ticket, AppUser recipient, AppUser actor, String subject, String message, String severity) {
  if (recipient == null || !recipient.isActive() || (actor != null && Objects.equals(recipient.getId(), actor.getId()))) return;
  try {
   notifications.create(new NotificationRequest(ticket.getOrganisation().getId(), null, null, recipient.getId(), recipient.getEmail(),
     NotificationChannel.APP_ALERT, severity, "Ticket " + ticket.getTicketNumber() + ": " + subject, message, null), "/tickets/" + ticket.getId());
  } catch (RuntimeException ex) {
   log.warn("Ticket notification for {} could not be stored: {}", ticket.getTicketNumber(), ex.getMessage());
  }
 }
}
