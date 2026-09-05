package com.ercopac.ercopac_tracker.crm.domain;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
@Entity @Table(name="crm_equipment_types") public class CrmEquipmentType {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organisation_id",nullable=false) private Organisation organisation;
 @Column(nullable=false,length=40) private String code; @Column(nullable=false,length=160) private String name; @Column(nullable=false) private boolean active=true;
 public Long getId(){return id;} public Organisation getOrganisation(){return organisation;} public void setOrganisation(Organisation v){organisation=v;} public String getCode(){return code;} public void setCode(String v){code=v;} public String getName(){return name;} public void setName(String v){name=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
