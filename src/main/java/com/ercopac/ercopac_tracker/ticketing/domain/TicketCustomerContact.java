package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.admin.domain.Customer; import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import com.ercopac.ercopac_tracker.user.AppUser; import jakarta.persistence.*;
// A person at a customer company. When portalUser is set, that CLIENT account belongs to the customer:
// its tickets are filed under the customer and the contact language is the one used for translation.
@Entity @Table(name="ticket_customer_contacts", indexes=@Index(name="idx_ticket_contact_customer",columnList="organisation_id,customer_id")) public class TicketCustomerContact {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @Column(name="full_name",nullable=false,length=150) private String fullName; @Column(name="job_title",length=120) private String jobTitle; @Column(length=180) private String email; @Column(length=60) private String phone; @Column(length=8) private String language;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="portal_user_id") private AppUser portalUser;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public String getJobTitle(){return jobTitle;} public void setJobTitle(String v){jobTitle=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getLanguage(){return language;} public void setLanguage(String v){language=v;} public AppUser getPortalUser(){return portalUser;} public void setPortalUser(AppUser v){portalUser=v;}
}
