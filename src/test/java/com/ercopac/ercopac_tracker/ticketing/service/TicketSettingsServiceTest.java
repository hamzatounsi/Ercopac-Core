package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketSettingsServiceTest {

    @Mock TicketStatusSettingRepository statusSettings;
    @Mock TicketPrioritySettingRepository prioritySettings;
    @Mock TicketAgentProfileRepository profiles;
    @Mock TicketCustomerContactRepository contacts;
    @Mock TicketRepository tickets;
    @Mock UserRepository users;
    @Mock OrganisationRepository organisations;
    @Mock SecurityUtils security;

    private TicketSettingsService service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new TicketSettingsService(statusSettings, prioritySettings, profiles, contacts, tickets, users, organisations, security);
        organisation = new Organisation();
        organisation.setId(5L);
    }

    @Test
    void storedPriorityOverridesDefaultWhileOthersKeepDefaults() {
        TicketPrioritySetting stored = new TicketPrioritySetting();
        stored.setPriority(TicketPriority.LOW);
        stored.setLabel("Minor");
        stored.setFirstResponseMinutes(600);
        stored.setResolutionHours(120);
        when(prioritySettings.findByOrganisation_Id(5L)).thenReturn(List.of(stored));

        Map<TicketPriority, PrioritySettingDto> result = service.priorities(5L);

        assertThat(result).hasSize(TicketPriority.values().length);
        assertThat(result.get(TicketPriority.LOW).label()).isEqualTo("Minor");
        assertThat(result.get(TicketPriority.LOW).resolutionHours()).isEqualTo(120);
        assertThat(result.get(TicketPriority.HIGH).firstResponseMinutes()).isEqualTo(60);
    }

    @Test
    void leadSavesPrioritySlaTargets() {
        signIn(user(1L, Role.H24_LEAD));
        when(prioritySettings.findByOrganisation_Id(5L)).thenReturn(List.of());

        service.savePriorities(List.of(new PrioritySettingDto(TicketPriority.HIGH, " Urgent ", 30, 6)));

        ArgumentCaptor<TicketPrioritySetting> saved = ArgumentCaptor.forClass(TicketPrioritySetting.class);
        verify(prioritySettings).save(saved.capture());
        assertThat(saved.getValue().getLabel()).isEqualTo("Urgent");
        assertThat(saved.getValue().getFirstResponseMinutes()).isEqualTo(30);
        assertThat(saved.getValue().getResolutionHours()).isEqualTo(6);
        assertThat(saved.getValue().getOrganisation()).isSameAs(organisation);
    }

    @Test
    void firstResponseTargetCannotExceedResolutionTarget() {
        signIn(user(1L, Role.H24_LEAD));
        when(prioritySettings.findByOrganisation_Id(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.savePriorities(List.of(new PrioritySettingDto(TicketPriority.HIGH, "High", 600, 2))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("longer than its resolution target");
        verify(prioritySettings, never()).save(any());
    }

    @Test
    void agentsCannotChangeSettings() {
        signIn(user(2L, Role.H24));

        assertThatThrownBy(() -> service.saveStatuses(List.of(new StatusSettingDto(TicketStatus.OPEN, "Open", null, "blue"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only support leads");
    }

    @Test
    void statusColourMustBeKnown() {
        signIn(user(1L, Role.SALES_MANAGER_LEAD));
        when(statusSettings.findByOrganisation_Id(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.saveStatuses(List.of(new StatusSettingDto(TicketStatus.OPEN, "Open", null, "#ff0000"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unknown colour");
    }

    @Test
    void leadFlagsSupportUserAsL2Technician() {
        signIn(user(1L, Role.H24_LEAD));
        AppUser agent = user(7L, Role.H24);
        when(users.findByIdAndOrganisation_Id(7L, 5L)).thenReturn(Optional.of(agent));
        when(profiles.findByUser_Id(7L)).thenReturn(Optional.empty());
        when(users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(any(), any())).thenReturn(List.of(agent));
        when(profiles.save(any())).thenAnswer(inv -> {
            TicketAgentProfile p = inv.getArgument(0);
            when(profiles.findByOrganisation_Id(5L)).thenReturn(List.of(p));
            return p;
        });

        AgentDto result = service.updateAgent(7L, new AgentProfileRequest(true, "Networking", "FR"));

        assertThat(result.l2Technician()).isTrue();
        assertThat(result.specialization()).isEqualTo("Networking");
        assertThat(result.preferredLanguage()).isEqualTo("fr");
    }

    @Test
    void onlySupportStaffGetATicketingProfile() {
        signIn(user(1L, Role.H24_LEAD));
        when(users.findByIdAndOrganisation_Id(8L, 5L)).thenReturn(Optional.of(user(8L, Role.CLIENT)));

        assertThatThrownBy(() -> service.updateAgent(8L, new AgentProfileRequest(true, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only support staff");
    }

    @Test
    void clientLanguageComesFromTheLinkedContact() {
        AppUser client = user(9L, Role.CLIENT);
        TicketCustomerContact contact = new TicketCustomerContact();
        contact.setLanguage("it");
        when(contacts.findFirstByPortalUser_Id(9L)).thenReturn(Optional.of(contact));

        assertThat(service.languageOf(client)).isEqualTo("it");
        assertThat(service.languageOf(null)).isEqualTo("en");
    }

    private void signIn(AppUser user) {
        when(security.getCurrentUserId()).thenReturn(user.getId());
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
    }

    private AppUser user(Long id, Role role) {
        AppUser u = new AppUser("User " + id, "u" + id + "@example.test", "x", role);
        ReflectionTestUtils.setField(u, "id", id);
        u.setOrganisation(organisation);
        return u;
    }
}
