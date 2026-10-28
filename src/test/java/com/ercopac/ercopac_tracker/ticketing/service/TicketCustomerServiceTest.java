package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.crm.repository.CrmEquipmentTypeRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.*;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class TicketCustomerServiceTest {

    @Mock CustomerRepository customers;
    @Mock TicketCustomerSiteRepository sites;
    @Mock TicketCustomerContactRepository contacts;
    @Mock TicketCustomerEquipmentRepository equipment;
    @Mock CrmEquipmentTypeRepository equipmentTypes;
    @Mock ProjectRepository projects;
    @Mock TicketRepository tickets;
    @Mock UserRepository users;
    @Mock TicketSettingsService settings;

    private TicketCustomerService service;
    private Organisation organisation;
    private Customer acme;
    private Customer stark;
    private AppUser lead;

    @BeforeEach
    void setUp() {
        service = new TicketCustomerService(customers, sites, contacts, equipment, equipmentTypes, projects, tickets, users, settings);
        organisation = new Organisation();
        organisation.setId(5L);
        acme = customer(7L, "Acme");
        stark = customer(8L, "Stark");
        lead = user(1L, Role.H24_LEAD);
        when(settings.requireLead()).thenReturn(lead);
        when(settings.requireStaff()).thenReturn(lead);
        when(customers.findByIdAndOrganisation_Id(7L, 5L)).thenReturn(Optional.of(acme));
        when(contacts.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void equipmentOfAnotherCustomerIsRejected() {
        TicketCustomerEquipment item = new TicketCustomerEquipment();
        item.setCustomer(stark);
        when(equipment.findByIdAndOrganisation_Id(3L, 5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.requireEquipment(3L, acme, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("another customer");
        assertThatThrownBy(() -> service.requireEquipment(3L, null, 5L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void projectOfAnotherCustomerIsRejected() {
        Project project = new Project();
        project.setCustomerEntity(stark);
        when(projects.findByIdAndOrganisationId(4L, 5L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.requireProject(4L, acme, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("another customer");
        assertThat(service.requireProject(4L, stark, 5L)).isSameAs(project);
    }

    @Test
    void contactCanBeLinkedToAClientAccountOnce() {
        AppUser client = user(9L, Role.CLIENT);
        when(users.findByIdAndOrganisation_Id(9L, 5L)).thenReturn(Optional.of(client));
        when(contacts.findFirstByPortalUser_Id(9L)).thenReturn(Optional.empty());

        ContactDto created = service.createContact(7L, new ContactRequest("Sarah Johnson", "IT Manager", "s.johnson@acme.test", null, "FR", 9L));

        assertThat(created.portalUser().id()).isEqualTo(9L);
        assertThat(created.language()).isEqualTo("fr");

        TicketCustomerContact existing = new TicketCustomerContact();
        ReflectionTestUtils.setField(existing, "id", 50L);
        existing.setFullName("Sarah Johnson");
        existing.setCustomer(acme);
        when(contacts.findFirstByPortalUser_Id(9L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createContact(7L, new ContactRequest("Other person", null, null, null, null, 9L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already linked");
    }

    @Test
    void onlyClientAccountsCanBeLinked() {
        when(users.findByIdAndOrganisation_Id(1L, 5L)).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> service.createContact(7L, new ContactRequest("Lead", null, null, null, null, 1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only CLIENT accounts");
    }

    @Test
    void customerCodeMustBeUnique() {
        when(customers.existsByOrganisation_IdAndCustomerCodeIgnoreCase(5L, "ACME")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CustomerRequest("ACME", "Acme again", null, null, null, null, null, null, true)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already used");
        verify(customers, never()).save(any());
    }

    @Test
    void equipmentReferencedByTicketsCannotBeDeleted() {
        TicketCustomerEquipment item = new TicketCustomerEquipment();
        item.setCustomer(acme);
        when(equipment.findByIdAndOrganisation_Id(3L, 5L)).thenReturn(Optional.of(item));
        when(tickets.existsByEquipment_Id(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteEquipment(7L, 3L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("referenced by tickets");
        verify(equipment, never()).delete(any());
    }

    @Test
    void clientOnlySeesTheCustomerTheirAccountBelongsTo() {
        AppUser client = user(9L, Role.CLIENT);
        TicketCustomerContact link = new TicketCustomerContact();
        link.setCustomer(acme);
        when(settings.current()).thenReturn(client);
        when(contacts.findFirstByPortalUser_Id(9L)).thenReturn(Optional.of(link));
        when(projects.findAllByOrganisationId(5L)).thenReturn(List.of());

        FormOptions options = service.formOptions(8L);

        assertThat(options.customers()).extracting(Ref::name).containsExactly("Acme");
        assertThat(options.customerId()).isEqualTo(7L);
    }

    private Customer customer(Long id, String name) {
        Customer c = new Customer();
        ReflectionTestUtils.setField(c, "id", id);
        c.setName(name);
        c.setOrganisation(organisation);
        return c;
    }

    private AppUser user(Long id, Role role) {
        AppUser u = new AppUser("User " + id, "u" + id + "@example.test", "x", role);
        ReflectionTestUtils.setField(u, "id", id);
        u.setOrganisation(organisation);
        return u;
    }
}
