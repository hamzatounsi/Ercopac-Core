package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
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
    @Mock ResourceTypeRepository resourceTypes;

    private TicketSettingsService service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new TicketSettingsService(statusSettings, prioritySettings, profiles, contacts, tickets, users, organisations, security, resourceTypes);
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
    void h24ListQueriesByH24RoleOrH24ResourceType() {
        signIn(user(1L, Role.H24_LEAD));
        AppUser byRole = user(7L, Role.H24);
        AppUser byType = user(8L, Role.EMPLOYEE);
        byType.setResourceType(resourceType(30L, "H24", "H24 Support"));
        when(users.findActiveByRolesOrResourceTypeCode(5L, TicketSettingsService.H24_ROLES, "H24")).thenReturn(List.of(byRole, byType));
        when(profiles.findByOrganisation_Id(5L)).thenReturn(List.of());
        when(tickets.findActive(any(), any())).thenReturn(List.of());

        List<AgentDto> result = service.agents("H24");

        assertThat(result).extracting(AgentDto::id).containsExactly(7L, 8L);
        assertThat(result).allMatch(AgentDto::h24);
        assertThat(result.get(1).ticketingAccess()).isFalse();
        assertThat(result.get(1).resourceType()).isEqualTo("H24 Support");
        verify(users, never()).findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(any(), any());
    }

    @Test
    void employeesAreFilteredByResourceTypeAndOptionallyByL2Flag() {
        signIn(user(1L, Role.H24_LEAD));
        ResourceType plc = resourceType(3L, "PLC", "PLC Engineer");
        when(resourceTypes.findByIdAndOrganisation_Id(3L, 5L)).thenReturn(Optional.of(plc));
        AppUser nora = user(20L, Role.EMPLOYEE); nora.setResourceType(plc); nora.setFullName("Nora");
        AppUser adam = user(21L, Role.EMPLOYEE); adam.setResourceType(plc); adam.setFullName("Adam");
        TicketAgentProfile noraL2 = new TicketAgentProfile();
        noraL2.setUser(nora);
        noraL2.setL2Technician(true);
        when(profiles.findByOrganisation_Id(5L)).thenReturn(List.of(noraL2));
        when(users.findByOrganisation_IdAndResourceType_IdAndActiveTrue(5L, 3L)).thenReturn(List.of(nora, adam));

        assertThat(service.employees(3L, false)).extracting(EmployeeOption::fullName).containsExactly("Adam", "Nora");
        assertThat(service.employees(3L, true)).extracting(EmployeeOption::id).containsExactly(20L);
        assertThat(service.employees(3L, false).get(0).resourceTypeId()).isEqualTo(3L);
    }

    @Test
    void resourceTypeOfAnotherOrganisationIsNotFound() {
        signIn(user(1L, Role.H24_LEAD));
        when(resourceTypes.findByIdAndOrganisation_Id(99L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.employees(99L, false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Resource type not found");
        verify(users, never()).findByOrganisation_IdAndResourceType_IdAndActiveTrue(any(), any());
    }

    @Test
    void projectumEmployeeCanBeFlaggedAndAssignedAsL2() {
        signIn(user(1L, Role.H24_LEAD));
        AppUser engineer = user(20L, Role.EMPLOYEE);
        engineer.setResourceType(resourceType(3L, "PLC", "PLC Engineer"));
        when(users.findByIdAndOrganisation_Id(20L, 5L)).thenReturn(Optional.of(engineer));
        when(profiles.findByUser_Id(20L)).thenReturn(Optional.empty());
        when(tickets.findActive(any(), any())).thenReturn(List.of());
        when(profiles.save(any())).thenAnswer(inv -> {
            TicketAgentProfile p = inv.getArgument(0);
            when(profiles.findByUser_Id(20L)).thenReturn(Optional.of(p));
            return p;
        });

        AgentDto result = service.updateAgent(20L, new AgentProfileRequest(true, "Siemens PLC", null));

        assertThat(result.l2Technician()).isTrue();
        assertThat(result.resourceType()).isEqualTo("PLC Engineer");
        assertThat(service.isL2Technician(engineer)).isTrue();
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

    private ResourceType resourceType(Long id, String code, String label) {
        ResourceType t = new ResourceType(code, label, organisation);
        ReflectionTestUtils.setField(t, "id", id);
        return t;
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
