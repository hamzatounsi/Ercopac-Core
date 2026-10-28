package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
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

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// L2 escalation, ticket context (customer / project / equipment) and the problem statement on TicketService
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceAssignmentTest {

    @Mock TicketRepository tickets;
    @Mock TicketMessageRepository messages;
    @Mock TicketActivityRepository activities;
    @Mock TicketAttachmentRepository attachments;
    @Mock TicketReadStateRepository readStates;
    @Mock UserRepository users;
    @Mock OrganisationRepository organisations;
    @Mock SecurityUtils security;
    @Mock TicketCustomerService customers;
    @Mock TicketSettingsService settings;
    @Mock TicketAnalyticsService analytics;
    @Mock TicketNotifier notifier;

    private TicketService service;
    private Organisation organisation;
    private AppUser agent;
    private AppUser technician;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        service = new TicketService(tickets, messages, activities, attachments, readStates, users, organisations, security,
                new TicketStatusTransitionValidator(), customers, settings, analytics, notifier);
        organisation = new Organisation();
        organisation.setId(5L);
        agent = user(1L, Role.H24);
        technician = user(2L, Role.H24_LEAD);

        ticket = new Ticket();
        ReflectionTestUtils.setField(ticket, "id", 100L);
        ReflectionTestUtils.setField(ticket, "version", 3L);
        ReflectionTestUtils.setField(ticket, "createdAt", Instant.now());
        ReflectionTestUtils.setField(ticket, "updatedAt", Instant.now());
        ticket.setTicketNumber("TKT-2026-000100");
        ticket.setSubject("Line 3 PLC offline");
        ticket.setDescription("Lost connection");
        ticket.setOrganisation(organisation);
        ticket.setCreatedByUser(agent);
        ticket.setStatus(TicketStatus.ESCALATED);
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setCategory(TicketCategory.SOFTWARE);

        when(security.getCurrentUserId()).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(agent));
        when(tickets.findById(100L)).thenReturn(Optional.of(ticket));
        when(users.findByIdAndOrganisation_Id(2L, 5L)).thenReturn(Optional.of(technician));
    }

    @Test
    void assignsFlaggedL2TechnicianAndRecordsAndNotifies() {
        when(settings.isL2Technician(technician)).thenReturn(true);

        TicketDetails details = service.assignL2(100L, new AssignmentRequest(2L, 3L));

        assertThat(ticket.getL2Technician()).isSameAs(technician);
        assertThat(details.ticket().l2Technician().fullName()).isEqualTo(technician.getFullName());
        ArgumentCaptor<TicketActivity> activity = ArgumentCaptor.forClass(TicketActivity.class);
        verify(activities).save(activity.capture());
        assertThat(activity.getValue().getField()).isEqualTo("l2Technician");
        assertThat(activity.getValue().getNewValue()).isEqualTo(technician.getFullName());
        verify(notifier).notify(eq(ticket), eq(technician), eq(agent), contains("L2"), anyString(), eq("WARNING"));
    }

    @Test
    void rejectsUserNotFlaggedAsL2() {
        when(settings.isL2Technician(technician)).thenReturn(false);

        assertThatThrownBy(() -> service.assignL2(100L, new AssignmentRequest(2L, 3L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not an L2 technician");
        assertThat(ticket.getL2Technician()).isNull();
    }

    @Test
    void staleVersionIsRejected() {
        assertThatThrownBy(() -> service.assignL2(100L, new AssignmentRequest(2L, 2L)))
                .isInstanceOf(TicketConflictException.class);
    }

    @Test
    void clientsCannotAssignL2() {
        AppUser client = user(9L, Role.CLIENT);
        when(security.getCurrentUserId()).thenReturn(9L);
        when(users.findById(9L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.assignL2(100L, new AssignmentRequest(2L, 3L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void contextChangeSetsCustomerProjectEquipmentAndSite() {
        Customer acme = new Customer();
        ReflectionTestUtils.setField(acme, "id", 7L);
        acme.setName("Acme");
        TicketCustomerEquipment plc = new TicketCustomerEquipment();
        plc.setName("PLC S7-1500");
        plc.setCustomer(acme);
        when(customers.requireCustomer(7L, 5L)).thenReturn(acme);
        when(customers.requireEquipment(11L, acme, 5L)).thenReturn(plc);

        TicketDetails details = service.updateContext(100L, new TicketContextRequest(7L, " Plant North ", null, 11L, 3L));

        assertThat(ticket.getCustomer()).isSameAs(acme);
        assertThat(ticket.getEquipment()).isSameAs(plc);
        assertThat(ticket.getProject()).isNull();
        assertThat(ticket.getSite()).isEqualTo("Plant North");
        assertThat(details.ticket().customer().name()).isEqualTo("Acme");
        verify(activities).save(argThat(a -> "context".equals(a.getField())));
    }

    @Test
    void problemStatementIsStoredAndBlankFieldsCleared() {
        service.updateProblemStatement(100L, new ProblemStatementRequest("PLC drops off the network", "  ", "Replaced the switch port", 3L));

        assertThat(ticket.getRcaProblem()).isEqualTo("PLC drops off the network");
        assertThat(ticket.getRcaCause()).isNull();
        assertThat(ticket.getRcaSolution()).isEqualTo("Replaced the switch port");
    }

    @Test
    void closedTicketsKeepTheirProblemStatement() {
        ticket.setStatus(TicketStatus.CLOSED);

        assertThatThrownBy(() -> service.updateProblemStatement(100L, new ProblemStatementRequest("x", null, null, 3L)))
                .isInstanceOf(TicketConflictException.class);
    }

    private AppUser user(Long id, Role role) {
        AppUser u = new AppUser("User " + id, "u" + id + "@example.test", "x", role);
        ReflectionTestUtils.setField(u, "id", id);
        u.setOrganisation(organisation);
        return u;
    }
}
