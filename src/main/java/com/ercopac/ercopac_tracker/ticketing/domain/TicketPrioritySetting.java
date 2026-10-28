package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// Organisation label and SLA targets of a priority
@Entity @Table(name="ticket_priority_settings", uniqueConstraints=@UniqueConstraint(name="uk_ticket_priority_setting",columnNames={"organisation_id","priority"})) public class TicketPrioritySetting {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private TicketPriority priority;
 @Column(nullable=false,length=60) private String label; @Column(name="first_response_minutes",nullable=false) private int firstResponseMinutes; @Column(name="resolution_hours",nullable=false) private int resolutionHours;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public TicketPriority getPriority(){return priority;} public void setPriority(TicketPriority v){priority=v;}
 public String getLabel(){return label;} public void setLabel(String v){label=v;} public int getFirstResponseMinutes(){return firstResponseMinutes;} public void setFirstResponseMinutes(int v){firstResponseMinutes=v;} public int getResolutionHours(){return resolutionHours;} public void setResolutionHours(int v){resolutionHours=v;}
}
