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
import com.ercopac.ercopac_tracker.user.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Customer users (UserType.CUSTOMER, CLIENT role): they see every ticket of their own customer and nothing else,
// and their new tickets are filed under that customer. Legacy CLIENT accounts keep seeing only their own tickets.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceCustomerUserTest {

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
    @Mock TicketConfigService config;
    @Mock TicketStatusService statuses;

    private TicketService service;
    private Organisation organisation;
    private Customer acme;
    private Customer stark;
    private AppUser acmeUser;
    private AppUser acmeColleague;
    private AppUser agent;

    @BeforeEach
    void setUp() {
        service = new TicketService(tickets, messages, activities, attachments, readStates, users, organisations, security,
                statuses, customers, settings, analytics, notifier, config);
        organisation = new Organisation();
        organisation.setId(5L);
        acme = customer(7L, "Acme");
        stark = customer(8L, "Stark");
        acmeUser = customerUser(20L, acme);
        acmeColleague = customerUser(21L, acme);
        agent = user(1L, Role.H24);

        when(users.findById(20L)).thenReturn(Optional.of(acmeUser));
        when(users.findById(1L)).thenReturn(Optional.of(agent));
        when(organisations.findById(5L)).thenReturn(Optional.of(organisation));
        when(config.requireCategory(eq(5L), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(tickets.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            if (t.getId() == null) ReflectionTestUtils.setField(t, "id", 500L);
            ReflectionTestUtils.setField(t, "createdAt", Instant.now());
            ReflectionTestUtils.setField(t, "updatedAt", Instant.now());
            when(tickets.findById(t.getId())).thenReturn(Optional.of(t));
            return t;
        });
    }

    @Test
    void customerUserListIsScopedToItsCustomer() {
        signIn(acmeUser);
        when(tickets.search(anyBoolean(), any(), anyBoolean(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        service.list(null, null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 20));

        verify(tickets).search(eq(false), eq(5L), eq(true), eq(20L), eq(7L), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(Pageable.class));
    }

    @Test
    void legacyClientAccountKeepsSeeingOnlyItsOwnTickets() {
        AppUser legacy = user(30L, Role.CLIENT);
        when(users.findById(30L)).thenReturn(Optional.of(legacy));
        signIn(legacy);
        when(tickets.search(anyBoolean(), any(), anyBoolean(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        service.list(null, null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 20));

        verify(tickets).search(eq(false), eq(5L), eq(true), eq(30L), isNull(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(Pageable.class));
    }

    @Test
    void staffVisibilityIsUnchangedAndCanFilterByCustomer() {
        signIn(agent);
        when(tickets.search(anyBoolean(), any(), anyBoolean(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        service.list(null, null, null, null, null, null, null, null, 8L, null, null, PageRequest.of(0, 20));

        // platform, organisation, clientOnly, user, scopeCustomerId, search..creatorId (6), customerId filter, from, to, page
        verify(tickets).search(eq(false), eq(5L), eq(false), eq(1L), isNull(), any(), any(), any(), any(), any(), any(), any(),
                eq(8L), any(), any(), any(Pageable.class));
    }

    @Test
    void customerUserOpensTicketsOfItsCustomerCreatedByColleagues() {
        signIn(acmeUser);
        Ticket colleagueTicket = ticket(100L, acme, acmeColleague);
        when(tickets.findById(100L)).thenReturn(Optional.of(colleagueTicket));

        TicketDetails details = service.get(100L);

        assertThat(details.ticket().id()).isEqualTo(100L);
    }

    @Test
    void customerUserCannotOpenTicketsOfAnotherCustomer() {
        signIn(acmeUser);
        when(tickets.findById(101L)).thenReturn(Optional.of(ticket(101L, stark, agent)));

        assertThatThrownBy(() -> service.get(101L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void customerUserCannotOpenATicketWithoutCustomerEvenIfItCreatedIt() {
        signIn(acmeUser);
        when(tickets.findById(102L)).thenReturn(Optional.of(ticket(102L, null, acmeUser)));

        assertThatThrownBy(() -> service.get(102L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void ticketCreatedByCustomerUserIsLinkedToItsCustomerWhateverTheRequestSays() {
        signIn(acmeUser);
        when(customers.customerOfClient(acmeUser)).thenReturn(acme);

        service.create(new CreateTicketRequest("Printer down", "Line 2 printer stopped", "SOFTWARE", TicketPriority.HIGH,
                null, null, null, null, null, 8L, null, null, null));

        ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
        verify(tickets, atLeastOnce()).save(saved.capture());
        Ticket created = saved.getValue();
        assertThat(created.getCustomer()).isSameAs(acme);
        assertThat(created.getClientUser()).isSameAs(acmeUser);
        verify(customers, never()).requireCustomer(8L, 5L);
    }

    @Test
    void ticketFiledByStaffForACustomerUserGoesToThatUsersCustomer() {
        signIn(agent);
        when(users.findByIdAndOrganisation_Id(20L, 5L)).thenReturn(Optional.of(acmeUser));
        when(customers.customerOfClient(acmeUser)).thenReturn(acme);

        service.create(new CreateTicketRequest("Printer down", "Line 2 printer stopped", "SOFTWARE", TicketPriority.HIGH,
                null, null, null, 20L, null, 8L, null, null, null));

        ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
        verify(tickets, atLeastOnce()).save(saved.capture());
        assertThat(saved.getValue().getCustomer()).isSameAs(acme);
    }

    @Test
    void customerUsersCannotManageTickets() {
        signIn(acmeUser);
        when(tickets.findById(100L)).thenReturn(Optional.of(ticket(100L, acme, acmeUser)));

        assertThatThrownBy(() -> service.updateContext(100L, new TicketContextRequest(8L, null, null, null, 0L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    private void signIn(AppUser user) {
        when(security.getCurrentUserId()).thenReturn(user.getId());
    }

    private Ticket ticket(Long id, Customer customer, AppUser creator) {
        Ticket t = new Ticket();
        ReflectionTestUtils.setField(t, "id", id);
        ReflectionTestUtils.setField(t, "version", 0L);
        ReflectionTestUtils.setField(t, "createdAt", Instant.now());
        ReflectionTestUtils.setField(t, "updatedAt", Instant.now());
        t.setTicketNumber("TKT-2026-000" + id);
        t.setSubject("Subject " + id);
        t.setDescription("Description");
        t.setOrganisation(organisation);
        t.setCreatedByUser(creator);
        t.setCustomer(customer);
        t.setStatus(TicketStatusFixtures.standard("OPEN"));
        t.setPriority(TicketPriority.MEDIUM);
        t.setCategory(TicketCategory.SOFTWARE);
        return t;
    }

    private Customer customer(Long id, String name) {
        Customer c = new Customer();
        ReflectionTestUtils.setField(c, "id", id);
        c.setName(name);
        c.setOrganisation(organisation);
        return c;
    }

    private AppUser customerUser(Long id, Customer customer) {
        AppUser u = user(id, Role.CLIENT);
        u.setUserType(UserType.CUSTOMER);
        u.setCustomer(customer);
        return u;
    }

    private AppUser user(Long id, Role role) {
        AppUser u = new AppUser("User " + id, "u" + id + "@example.test", "x", role);
        ReflectionTestUtils.setField(u, "id", id);
        u.setOrganisation(organisation);
        return u;
    }
}
