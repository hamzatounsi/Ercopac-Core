package com.ercopac.ercopac_tracker.platform_dashboard.dto;

import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import java.time.LocalDateTime;

public class PlatformOrganisationDto {
    public Long id;
    public String name;
    public String code;
    public String country;
    public String domain;
    public OrganisationStatus status;
    public String plan;
    public int warehouseLimit;
    public int userLimit;
    public int adminLicenceLimit;
    public int specialistLicenceLimit;
    public int supervisorLicenceLimit;
    public int operatorLicenceLimit;
    public int readonlyLicenceLimit;
    public double monthlyRevenue;
    public int healthScore;
    public LocalDateTime createdAt;

    public String billingEmail;
    public String vatNumber;
    public String paymentMethod;

    public String force2faAdmins;
    public String force2faSpecialists;
    public String force2faOperators;
    public String default2faMethod;
    public String sessionTimeout;
    public int maxFailedLogins;
    public int passwordMinLength;
    public String passwordExpiry;

    public String internalNotes;
    public boolean flagAtRisk;
    public boolean flagPaymentOverdue;
    public boolean flagUpsellOpportunity;
    public boolean flagVipPriority;
    public boolean flagPilotFeatures;
    public boolean flagUnderReview;
    public String adminFullName;
}