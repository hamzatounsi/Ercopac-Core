package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="ticket_messages", indexes=@Index(name="idx_ticket_message_ticket_created", columnList="ticket_id,created_at"))
public class TicketMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="ticket_id", nullable=false) private Ticket ticket;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="sender_id", nullable=false) private AppUser sender;
 @Column(nullable=false, length=8000) private String message;
 @Column(name="message_type", nullable=false, length=30) private String messageType="MESSAGE";
 @Column(name="internal_note", nullable=false) private boolean internalNote;
 @Column(nullable=false) private boolean deleted;
 @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
 @Column(name="edited_at") private Instant editedAt;
 @PrePersist void create(){createdAt=Instant.now();}
 public Long getId(){return id;} public Ticket getTicket(){return ticket;} public void setTicket(Ticket v){ticket=v;} public AppUser getSender(){return sender;} public void setSender(AppUser v){sender=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} public String getMessageType(){return messageType;} public void setMessageType(String v){messageType=v;} public boolean isInternalNote(){return internalNote;} public void setInternalNote(boolean v){internalNote=v;} public boolean isDeleted(){return deleted;} public void setDeleted(boolean v){deleted=v;} public Instant getCreatedAt(){return createdAt;} public Instant getEditedAt(){return editedAt;}
}
