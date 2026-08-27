package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.repository.*;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrmServiceTenantTest {
    @Mock CrmPipelineStageRepository stageRepo;
    @Mock CrmLeadRepository leadRepo;
    @Mock CrmOpportunityRepository opportunityRepo;
    @Mock CrmActivityRepository activityRepo;
    @Mock CrmAccountRepository accountRepo;
    @Mock CrmSupplyCategoryRepository categoryRepo;
    @Mock CrmOpportunityNoteRepository noteRepo;
    @Mock CrmOpportunityAttachmentRepository attachmentRepo;
    @Mock CrmOpportunityHistoryRepository historyRepo;
    @Mock CrmOpportunityStageHistoryRepository stageHistoryRepo;
    @Mock CrmSalesTargetRepository targetRepo;
    @Mock OrganisationRepository organisationRepo;
    @Mock UserRepository userRepo;
    @Mock SecurityUtils security;
    CrmService service;

    @BeforeEach
    void setUp() {
        service = new CrmService(stageRepo, leadRepo, opportunityRepo, activityRepo, accountRepo,
                categoryRepo, noteRepo, attachmentRepo, historyRepo, stageHistoryRepo, targetRepo,
                organisationRepo, userRepo, security, "target/test-crm-attachments");
        when(security.isPlatformUser()).thenReturn(false);
        when(security.getCurrentOrganisationId()).thenReturn(11L);
    }

    @Test
    void tenantRequestUsesRepositoryOrganisationPredicate() {
        when(accountRepo.findByOrganisation_IdAndActiveTrueOrderByNameAsc(11L)).thenReturn(List.of());

        service.getAccounts(11L, null);

        verify(accountRepo).findByOrganisation_IdAndActiveTrueOrderByNameAsc(11L);
    }

    @Test
    void crossOrganisationPathIsForbiddenBeforeRepositoryAccess() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getAccounts(22L, null));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(accountRepo);
    }
}
