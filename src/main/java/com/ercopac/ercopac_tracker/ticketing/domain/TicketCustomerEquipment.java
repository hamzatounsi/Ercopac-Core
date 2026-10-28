package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.admin.domain.Customer; import com.ercopac.ercopac_tracker.crm.domain.CrmEquipmentType; import com.ercopac.ercopac_tracker.organisation.domain.Organisation; import jakarta.persistence.*;
// An installed item at a customer; its type comes from the organisation equipment catalogue (CRM equipment types)
@Entity @Table(name="ticket_customer_equipment", indexes=@Index(name="idx_ticket_equipment_customer",columnList="organisation_id,customer_id")) public class TicketCustomerEquipment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="equipment_type_id") private CrmEquipmentType equipmentType;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="site_id") private TicketCustomerSite site;
 @Column(nullable=false,length=160) private String name; @Column(name="serial_number",length=100) private String serialNumber; @Column(length=500) private String note;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public CrmEquipmentType getEquipmentType(){return equipmentType;} public void setEquipmentType(CrmEquipmentType v){equipmentType=v;} public TicketCustomerSite getSite(){return site;} public void setSite(TicketCustomerSite v){site=v;}
 public String getName(){return name;} public void setName(String v){name=v;} public String getSerialNumber(){return serialNumber;} public void setSerialNumber(String v){serialNumber=v;} public String getNote(){return note;} public void setNote(String v){note=v;}
}
