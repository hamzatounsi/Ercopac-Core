package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.admin.domain.Customer; import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// A site (plant, office, campus) of a customer company; tickets and equipment refer to it
@Entity @Table(name="ticket_customer_sites", indexes=@Index(name="idx_ticket_site_customer",columnList="organisation_id,customer_id")) public class TicketCustomerSite {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @Column(nullable=false,length=160) private String name; @Column(length=300) private String address;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public String getName(){return name;} public void setName(String v){name=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;}
}
