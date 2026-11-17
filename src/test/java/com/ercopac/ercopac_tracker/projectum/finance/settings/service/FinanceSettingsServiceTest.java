package com.ercopac.ercopac_tracker.projectum.finance.settings.service;

import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SaveFinanceSettingsRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceWbsTemplateRowRepository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.repository.FinanceSettingsRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceSettingsServiceTest {
    @Mock private SecurityUtils securityUtils;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private ProjectAccessService projectAccessService;
    @Mock private FinanceService financeService;
    @Mock private FinanceWbsTemplateRowRepository templateRowRepository;
    @Mock private FinanceSettingsRepository financeSettingsRepository;
    @Mock private FinanceEntryRepository financeEntryRepository;
    @InjectMocks private FinanceSettingsService service;

    @Test
    void savingSettingsWithNoTemplateDoesNotRejectLegacyFinanceRows() {
        when(securityUtils.getCurrentOrganisationId()).thenReturn(7L);
        when(organisationRepository.findById(7L)).thenReturn(Optional.of(new Organisation()));
        Organisation organisation = new Organisation();
        organisation.setId(7L);
        Project project = new Project();
        project.setOrganisation(organisation);
        when(projectAccessService.getAccessibleProject(3L)).thenReturn(project);
        when(templateRowRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(3L)).thenReturn(List.of());
        SaveFinanceSettingsRequest request = new SaveFinanceSettingsRequest();
        request.setDefaultHourlyRate(BigDecimal.valueOf(65));

        assertTrue(service.saveSettings(3L, request).getTemplateRows().isEmpty());
        verify(financeEntryRepository, never()).delete(any());
    }

    @Test
    void addImportKeepsUnmatchedFinanceRowsWithData() {
        FinanceEntry legacy = new FinanceEntry();
        legacy.setWbsCode("2.2");
        legacy.setBudget(BigDecimal.valueOf(500));
        assertFalse(FinanceSettingsService.shouldPrune(legacy, false, true, false));
        assertFalse(FinanceSettingsService.shouldPrune(legacy, false, false, false));
        assertTrue(FinanceSettingsService.shouldPrune(legacy, false, false, true));
    }
}
