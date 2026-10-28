package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// Organisation wording and colour of a lifecycle status; the lifecycle itself (TicketStatus and its transitions) is fixed
@Entity @Table(name="ticket_status_settings", uniqueConstraints=@UniqueConstraint(name="uk_ticket_status_setting",columnNames={"organisation_id","status"})) public class TicketStatusSetting {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TicketStatus status;
 @Column(nullable=false,length=60) private String label; @Column(length=300) private String description; @Column(nullable=false,length=20) private String color;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public TicketStatus getStatus(){return status;} public void setStatus(TicketStatus v){status=v;}
 public String getLabel(){return label;} public void setLabel(String v){label=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getColor(){return color;} public void setColor(String v){color=v;}
}
