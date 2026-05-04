package com.ercopac.ercopac_tracker.organisation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organisations")
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 80)
    private String country;

    @Column(length = 120)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganisationStatus status = OrganisationStatus.ACTIVE;

    @Column(nullable = false, length = 30)
    private String plan = "STARTER";

    @Column(nullable = false)
    private int warehouseLimit = 1;

    @Column(nullable = false)
    private int userLimit = 10;

    @Column(nullable = false)
    private int adminLicenceLimit = 1;

    @Column(nullable = false)
    private int specialistLicenceLimit = 5;

    @Column(nullable = false)
    private int supervisorLicenceLimit = 2;

    @Column(nullable = false)
    private int operatorLicenceLimit = 10;

    @Column(nullable = false)
    private int readonlyLicenceLimit = 5;

    @Column(nullable = false)
    private double monthlyRevenue = 0;

    @Column(nullable = false)
    private int healthScore = 100;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 160)
    private String billingEmail;

    @Column(length = 80)
    private String vatNumber;

    @Column(length = 50)
    private String paymentMethod = "SEPA_DIRECT_DEBIT";

    @Column(length = 30)
    private String force2faAdmins = "ENABLED";

    @Column(length = 30)
    private String force2faSpecialists = "ENABLED";

    @Column(length = 30)
    private String force2faOperators = "OPTIONAL";

    @Column(length = 50)
    private String default2faMethod = "AUTHENTICATOR_APP";

    @Column(length = 30)
    private String sessionTimeout = "4_HOURS";

    @Column(nullable = false)
    private int maxFailedLogins = 5;

    @Column(nullable = false)
    private int passwordMinLength = 8;

    @Column(length = 30)
    private String passwordExpiry = "NEVER";

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    @Column(nullable = false)
    private boolean flagAtRisk = false;

    @Column(nullable = false)
    private boolean flagPaymentOverdue = false;

    @Column(nullable = false)
    private boolean flagUpsellOpportunity = false;

    @Column(nullable = false)
    private boolean flagVipPriority = false;

    @Column(nullable = false)
    private boolean flagPilotFeatures = false;

    @Column(nullable = false)
    private boolean flagUnderReview = false;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getWarehouseLimit() {
        return warehouseLimit;
    }

    public void setWarehouseLimit(int warehouseLimit) {
        this.warehouseLimit = warehouseLimit;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public int getAdminLicenceLimit() {
        return adminLicenceLimit;
    }

    public void setAdminLicenceLimit(int adminLicenceLimit) {
        this.adminLicenceLimit = adminLicenceLimit;
    }

    public int getSpecialistLicenceLimit() {
        return specialistLicenceLimit;
    }

    public void setSpecialistLicenceLimit(int specialistLicenceLimit) {
        this.specialistLicenceLimit = specialistLicenceLimit;
    }

    public int getSupervisorLicenceLimit() {
        return supervisorLicenceLimit;
    }

    public void setSupervisorLicenceLimit(int supervisorLicenceLimit) {
        this.supervisorLicenceLimit = supervisorLicenceLimit;
    }

    public int getOperatorLicenceLimit() {
        return operatorLicenceLimit;
    }

    public void setOperatorLicenceLimit(int operatorLicenceLimit) {
        this.operatorLicenceLimit = operatorLicenceLimit;
    }

    public int getReadonlyLicenceLimit() {
        return readonlyLicenceLimit;
    }

    public void setReadonlyLicenceLimit(int readonlyLicenceLimit) {
        this.readonlyLicenceLimit = readonlyLicenceLimit;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getForce2faAdmins() {
        return force2faAdmins;
    }

    public void setForce2faAdmins(String force2faAdmins) {
        this.force2faAdmins = force2faAdmins;
    }

    public String getForce2faSpecialists() {
        return force2faSpecialists;
    }

    public void setForce2faSpecialists(String force2faSpecialists) {
        this.force2faSpecialists = force2faSpecialists;
    }

    public String getForce2faOperators() {
        return force2faOperators;
    }

    public void setForce2faOperators(String force2faOperators) {
        this.force2faOperators = force2faOperators;
    }

    public String getDefault2faMethod() {
        return default2faMethod;
    }

    public void setDefault2faMethod(String default2faMethod) {
        this.default2faMethod = default2faMethod;
    }

    public String getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getMaxFailedLogins() {
        return maxFailedLogins;
    }

    public void setMaxFailedLogins(int maxFailedLogins) {
        this.maxFailedLogins = maxFailedLogins;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public String getPasswordExpiry() {
        return passwordExpiry;
    }

    public void setPasswordExpiry(String passwordExpiry) {
        this.passwordExpiry = passwordExpiry;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public boolean isFlagAtRisk() {
        return flagAtRisk;
    }

    public void setFlagAtRisk(boolean flagAtRisk) {
        this.flagAtRisk = flagAtRisk;
    }

    public boolean isFlagPaymentOverdue() {
        return flagPaymentOverdue;
    }

    public void setFlagPaymentOverdue(boolean flagPaymentOverdue) {
        this.flagPaymentOverdue = flagPaymentOverdue;
    }

    public boolean isFlagUpsellOpportunity() {
        return flagUpsellOpportunity;
    }

    public void setFlagUpsellOpportunity(boolean flagUpsellOpportunity) {
        this.flagUpsellOpportunity = flagUpsellOpportunity;
    }

    public boolean isFlagVipPriority() {
        return flagVipPriority;
    }

    public void setFlagVipPriority(boolean flagVipPriority) {
        this.flagVipPriority = flagVipPriority;
    }

    public boolean isFlagPilotFeatures() {
        return flagPilotFeatures;
    }

    public void setFlagPilotFeatures(boolean flagPilotFeatures) {
        this.flagPilotFeatures = flagPilotFeatures;
    }

    public boolean isFlagUnderReview() {
        return flagUnderReview;
    }

    public void setFlagUnderReview(boolean flagUnderReview) {
        this.flagUnderReview = flagUnderReview;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}