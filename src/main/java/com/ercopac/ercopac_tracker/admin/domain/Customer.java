package com.ercopac.ercopac_tracker.admin.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organisation_id", "customer_code"})
    }
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "customer_code", nullable = false, length = 60)
    private String customerCode;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 80)
    private String country;

    @Column(length = 120)
    private String town;

    @Column(length = 300)
    private String address;

    @Column(name = "vat_tax_id", length = 80)
    private String vatTaxId;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 180)
    private String email;

    @Column(length = 60)
    private String phone;

    @Column(name = "erp_id", length = 100)
    private String erpId;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVatTaxId() { return vatTaxId; }
    public void setVatTaxId(String vatTaxId) { this.vatTaxId = vatTaxId; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getErpId() { return erpId; }
    public void setErpId(String erpId) { this.erpId = erpId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}