package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
/** One-to-one Live chat message between two users of the same organisation (not linked to a ticket). */
@Entity @Table(name="ticket_direct_messages", indexes={@Index(name="idx_direct_msg_org_sender_recipient", columnList="organisation_id,sender_id,recipient_id"), @Index(name="idx_direct_msg_recipient_read", columnList="recipient_id,read_at")})
public class DirectChatMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="sender_id", nullable=false) private AppUser sender;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="recipient_id", nullable=false) private AppUser recipient;
 @Column(nullable=false, length=4000) private String message;
 @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
 @Column(name="read_at") private Instant readAt;
 @PrePersist void create(){createdAt=Instant.now();}
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public AppUser getSender(){return sender;} public void setSender(AppUser v){sender=v;} public AppUser getRecipient(){return recipient;} public void setRecipient(AppUser v){recipient=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} public Instant getCreatedAt(){return createdAt;} public Instant getReadAt(){return readAt;} public void setReadAt(Instant v){readAt=v;}
}
