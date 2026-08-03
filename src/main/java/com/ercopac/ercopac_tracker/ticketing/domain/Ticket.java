package com.ercopac.ercopac_tracker.ticketing.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_org_status", columnList = "organisation_id,status"),
        @Index(name = "idx_ticket_assignee", columnList = "assigned_sales_manager_id"),
        @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true)
})
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "ticket_number", nullable = false, unique = true, updatable = false, length = 32) private String ticketNumber;
    @Column(nullable = false, length = 250) private String subject;
    @Column(nullable = false, length = 8000) private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_user_id", nullable = false) private AppUser createdByUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "client_user_id") private AppUser clientUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_sales_manager_id") private AppUser assignedSalesManager;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private TicketCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private TicketPriority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TicketStatus status = TicketStatus.OPEN;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private TicketOrigin origin = TicketOrigin.WEB;
    @Column(length = 160) private String site;
    @Column(name = "escalation_level", nullable = false) private int escalationLevel;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "last_message_at") private Instant lastMessageAt;
    @Version private Long version;
    @PrePersist void createTimestamps() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }
    public Long getId(){return id;} public String getTicketNumber(){return ticketNumber;} public void setTicketNumber(String v){ticketNumber=v;}
    public String getSubject(){return subject;} public void setSubject(String v){subject=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public AppUser getCreatedByUser(){return createdByUser;} public void setCreatedByUser(AppUser v){createdByUser=v;}
    public AppUser getClientUser(){return clientUser;} public void setClientUser(AppUser v){clientUser=v;} public AppUser getAssignedSalesManager(){return assignedSalesManager;} public void setAssignedSalesManager(AppUser v){assignedSalesManager=v;}
    public TicketCategory getCategory(){return category;} public void setCategory(TicketCategory v){category=v;} public TicketPriority getPriority(){return priority;} public void setPriority(TicketPriority v){priority=v;}
    public TicketStatus getStatus(){return status;} public void setStatus(TicketStatus v){status=v;} public TicketOrigin getOrigin(){return origin;} public void setOrigin(TicketOrigin v){origin=v;}
    public String getSite(){return site;} public void setSite(String v){site=v;} public int getEscalationLevel(){return escalationLevel;} public void setEscalationLevel(int v){escalationLevel=v;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public Instant getResolvedAt(){return resolvedAt;} public void setResolvedAt(Instant v){resolvedAt=v;} public Instant getClosedAt(){return closedAt;} public void setClosedAt(Instant v){closedAt=v;} public Instant getLastMessageAt(){return lastMessageAt;} public void setLastMessageAt(Instant v){lastMessageAt=v;} public Long getVersion(){return version;}
}
