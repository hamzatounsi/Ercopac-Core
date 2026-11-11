package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCategorySetting;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCustomStatus;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketCategorySettingRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketCustomStatusRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// Ticket categories (system ones protected, in-use ones reassigned) and custom statuses (deleting falls back to the lifecycle status)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketConfigServiceTest {

    @Mock TicketCategorySettingRepository categories;
    @Mock TicketCustomStatusRepository statuses;
    @Mock TicketRepository tickets;
    @Mock TicketSettingsService settings;

    private TicketConfigService service;

    @BeforeEach
    void setUp() {
        service = new TicketConfigService(categories, statuses, tickets, settings);
        Organisation org = new Organisation();
        org.setId(5L);
        AppUser lead = new AppUser();
        lead.setOrganisation(org);
        when(settings.requireLead()).thenReturn(lead);
        when(settings.current()).thenReturn(lead);
    }

    private TicketCategorySetting category(long id, String code, boolean system) {
        TicketCategorySetting c = new TicketCategorySetting();
        ReflectionTestUtils.setField(c, "id", id);
        c.setCode(code);
        c.setLabel(code);
        c.setSystem(system);
        when(categories.findByIdAndOrganisation_Id(id, 5L)).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void systemCategoriesCannotBeDeleted() {
        category(1L, "SOFTWARE", true);
        assertThatThrownBy(() -> service.deleteCategory(1L, null)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("System categories");
        verify(categories, never()).delete(any());
    }

    @Test
    void categoryInUseNeedsAReassignmentTarget() {
        category(2L, "PLC", false);
        when(tickets.countByOrganisation_IdAndCategory(5L, "PLC")).thenReturn(3L);
        assertThatThrownBy(() -> service.deleteCategory(2L, null)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("3 ticket");
        verify(categories, never()).delete(any());
    }

    @Test
    void categoryInUseIsReassignedThenDeleted() {
        TicketCategorySetting plc = category(2L, "PLC", false);
        when(tickets.countByOrganisation_IdAndCategory(5L, "PLC")).thenReturn(3L);
        service.deleteCategory(2L, "software");
        verify(tickets).reassignCategory(5L, "PLC", "SOFTWARE");
        verify(categories).delete(plc);
    }

    @Test
    void unknownCategoryCodeIsRejected() {
        when(categories.findByOrganisation_IdAndCodeIgnoreCase(5L, "NOPE")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.requireCategory(5L, "NOPE")).isInstanceOf(ResponseStatusException.class);
        assertThat(service.requireCategory(5L, "access")).isEqualTo("ACCESS");
    }

    @Test
    void deletingACustomStatusClearsItFromTickets() {
        TicketCustomStatus waiting = new TicketCustomStatus();
        ReflectionTestUtils.setField(waiting, "id", 9L);
        waiting.setBaseStatus(TicketStatus.IN_PROGRESS);
        when(statuses.findByIdAndOrganisation_Id(9L, 5L)).thenReturn(Optional.of(waiting));
        service.deleteStatus(9L);
        verify(tickets).clearCustomStatus(9L);
        verify(statuses).delete(waiting);
    }
}
