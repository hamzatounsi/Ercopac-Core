package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// Ticket category of an organisation. System categories (system = true, code = a TicketCategory name) are always
// available and cannot be deleted; custom categories are created, renamed and deleted in Ticket settings.
@Entity @Table(name="ticket_category_settings", uniqueConstraints=@UniqueConstraint(name="uk_ticket_category_setting",columnNames={"organisation_id","code"})) public class TicketCategorySetting {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @Column(nullable=false,length=32) private String code; @Column(nullable=false,length=80) private String label;
 @Column(nullable=false) private boolean system; @Column(nullable=false) private boolean active=true; @Column(name="sort_order",nullable=false) private int sortOrder;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public String getCode(){return code;} public void setCode(String v){code=v;}
 public String getLabel(){return label;} public void setLabel(String v){label=v;} public boolean isSystem(){return system;} public void setSystem(boolean v){system=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public int getSortOrder(){return sortOrder;} public void setSortOrder(int v){sortOrder=v;}
}
