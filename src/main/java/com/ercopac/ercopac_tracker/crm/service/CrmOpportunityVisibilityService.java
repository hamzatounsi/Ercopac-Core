package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/** Applies the authenticated CRM opportunity scope after organisation scoping. */
@Service
public class CrmOpportunityVisibilityService {
    private final SecurityUtils security;

    public CrmOpportunityVisibilityService(SecurityUtils security) {
        this.security = security;
    }

    public List<CrmOpportunity> visible(List<CrmOpportunity> opportunities) {
        return opportunities.stream().filter(this::canView).toList();
    }

    public CrmOpportunity requireVisible(CrmOpportunity opportunity) {
        if (!canView(opportunity)) {
            // Avoid confirming that an inaccessible record exists.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found.");
        }
        return opportunity;
    }

    public boolean isRestrictedRole() {
        String role = security.getCurrentRole();
        return "SALES_MANAGER".equals(role) || "SYSTEM_ENGINEER".equals(role);
    }

    private boolean canView(CrmOpportunity opportunity) {
        String role = security.getCurrentRole();
        Long userId = security.getCurrentUserId();
        if ("SALES_MANAGER".equals(role)) {
            return opportunity.getOwner() != null && Objects.equals(opportunity.getOwner().getId(), userId);
        }
        if ("SYSTEM_ENGINEER".equals(role)) {
            return opportunity.getOwner() != null && Objects.equals(opportunity.getOwner().getId(), userId)
                    || opportunity.getTeamMembers().stream().map(AppUser::getId).anyMatch(userId::equals);
        }
        // SALES_MANAGER_LEAD (and platform/other already-authorized roles) keep the
        // organisation-wide scope established by the repository query.
        return true;
    }
}
