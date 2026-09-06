package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CrmOpportunityVisibilityServiceTest {
    @Mock SecurityUtils security;
    @Mock AppUser salesA;
    @Mock AppUser salesB;
    CrmOpportunityVisibilityService visibility;

    @BeforeEach
    void setUp() {
        visibility = new CrmOpportunityVisibilityService(security);
        lenient().when(security.getCurrentUserId()).thenReturn(10L);
        lenient().when(salesA.getId()).thenReturn(10L);
        lenient().when(salesB.getId()).thenReturn(20L);
    }

    @Test
    void salesManagerOnlySeesOwnedOpportunitiesAndDirectIdIsHidden() {
        when(security.getCurrentRole()).thenReturn("SALES_MANAGER");
        CrmOpportunity own = opportunity(salesA);
        CrmOpportunity other = opportunity(salesB);

        assertEquals(List.of(own), visibility.visible(List.of(own, other)));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> visibility.requireVisible(other));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void systemEngineerSeesOwnedOrAssignedOpportunitiesOnly() {
        when(security.getCurrentRole()).thenReturn("SYSTEM_ENGINEER");
        CrmOpportunity owned = opportunity(salesA);
        CrmOpportunity assigned = opportunity(salesB);
        assigned.getTeamMembers().add(salesA);
        CrmOpportunity other = opportunity(salesB);

        assertEquals(List.of(owned, assigned), visibility.visible(List.of(owned, assigned, other)));
    }

    @Test
    void salesManagerLeadKeepsOrganisationScopedRepositoryResult() {
        when(security.getCurrentRole()).thenReturn("SALES_MANAGER_LEAD");
        CrmOpportunity first = opportunity(salesA);
        CrmOpportunity second = opportunity(salesB);

        assertEquals(List.of(first, second), visibility.visible(List.of(first, second)));
    }

    private CrmOpportunity opportunity(AppUser owner) {
        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.setOwner(owner);
        return opportunity;
    }
}
