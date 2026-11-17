package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.admin.domain.Customer; import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// A customer project entered by hand in the Projects tab (historical or external work not tracked in Projectum).
// Projectum projects of the customer are read from Projectum and never copied here.
@Entity @Table(name="ticket_customer_projects", indexes=@Index(name="idx_ticket_customer_project_customer",columnList="organisation_id,customer_id")) public class TicketCustomerProject {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @Column(nullable=false,length=250) private String name; @Column(name="project_number",length=60) private String projectNumber; @Column(name="project_year") private Integer projectYear;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public String getName(){return name;} public void setName(String v){name=v;} public String getProjectNumber(){return projectNumber;} public void setProjectNumber(String v){projectNumber=v;} public Integer getProjectYear(){return projectYear;} public void setProjectYear(Integer v){projectYear=v;}
}
