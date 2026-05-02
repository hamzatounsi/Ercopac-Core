package com.ercopac.ercopac_tracker.platform_dashboard.dto;

public class CreateOrganisationWithAdminResponse {
    public Long organisationId;
    public String organisationName;
    public String organisationCode;
    public Long adminId;
    public String adminEmail;

    public CreateOrganisationWithAdminResponse(
            Long organisationId,
            String organisationName,
            String organisationCode,
            Long adminId,
            String adminEmail
    ) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
        this.organisationCode = organisationCode;
        this.adminId = adminId;
        this.adminEmail = adminEmail;
    }
}