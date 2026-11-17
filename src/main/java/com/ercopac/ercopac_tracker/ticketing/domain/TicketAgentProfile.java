package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import com.ercopac.ercopac_tracker.user.AppUser; import jakarta.persistence.*;
// Ticketing profile of a support user: L2 technician flag, specialisation and the language they write in
@Entity @Table(name="ticket_agent_profiles", uniqueConstraints=@UniqueConstraint(name="uk_ticket_agent_profile_user",columnNames="user_id")) public class TicketAgentProfile {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private AppUser user;
 @Column(name="l2_technician",nullable=false) private boolean l2Technician; @Column(length=120) private String specialization; @Column(name="preferred_language",length=8) private String preferredLanguage;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
 public boolean isL2Technician(){return l2Technician;} public void setL2Technician(boolean v){l2Technician=v;} public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;} public String getPreferredLanguage(){return preferredLanguage;} public void setPreferredLanguage(String v){preferredLanguage=v;}
}
