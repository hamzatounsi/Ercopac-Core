package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// Organisation-defined ticket status (e.g. "Waiting for parts"). It maps onto a system lifecycle status (baseStatus), so
// SLA, transitions and statistics keep working; the system statuses themselves (TicketStatus) cannot be deleted or renamed in code.
@Entity @Table(name="ticket_custom_statuses", uniqueConstraints=@UniqueConstraint(name="uk_ticket_custom_status",columnNames={"organisation_id","code"})) public class TicketCustomStatus {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @Column(nullable=false,length=40) private String code; @Column(nullable=false,length=60) private String label; @Column(length=300) private String description; @Column(nullable=false,length=20) private String color;
 @Enumerated(EnumType.STRING) @Column(name="base_status",nullable=false,length=20) private TicketStatus baseStatus;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public String getCode(){return code;} public void setCode(String v){code=v;}
 public String getLabel(){return label;} public void setLabel(String v){label=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getColor(){return color;} public void setColor(String v){color=v;}
 public TicketStatus getBaseStatus(){return baseStatus;} public void setBaseStatus(TicketStatus v){baseStatus=v;}
}
