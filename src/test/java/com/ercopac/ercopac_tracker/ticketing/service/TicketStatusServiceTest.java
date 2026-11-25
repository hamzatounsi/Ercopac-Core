package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.TicketStatusRequest;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketStatusRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Ticket statuses of Ticketing settings: no built-in set, unique names, one default, no hard delete of statuses in use
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketStatusServiceTest {

    @Mock TicketStatusRepository repo;
    @Mock TicketRepository tickets;
    @Mock TicketSettingsService settings;

    private TicketStatusService service;
    private Organisation org;
    private final List<TicketStatus> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new TicketStatusService(repo, tickets, settings);
        org = new Organisation();
        org.setId(5L);
        AppUser lead = new AppUser();
        lead.setOrganisation(org);
        when(settings.requireLead()).thenReturn(lead);
        when(settings.current()).thenReturn(lead);
        when(repo.findByOrganisation_IdOrderByDisplayOrderAscIdAsc(5L)).thenAnswer(i -> new ArrayList<>(stored));
        when(repo.save(any())).thenAnswer(i -> {
            TicketStatus s = i.getArgument(0);
            if (s.getId() == null) { s.setId(100L + stored.size()); stored.add(s); }
            return s;
        });
        when(repo.findByIdAndOrganisation_Id(any(), eq(5L))).thenAnswer(i -> stored.stream().filter(s -> s.getId().equals(i.getArgument(0))).findFirst());
    }

    private TicketStatusRequest request(String name, Boolean closed) {
        return new TicketStatusRequest(name, null, null, "green", null, null, closed, null);
    }

    @Test
    void organisationsStartWithoutAnyStatus() {
        assertThat(service.list(false)).isEmpty();
        assertThat(service.defaultStatus(org)).isNull();
        verify(repo, never()).save(any());
    }

    @Test
    void firstOpenStatusBecomesTheDefaultAndGetsACodeFromItsName() {
        var created = service.create(request("Waiting for parts", false));

        assertThat(created.code()).isEqualTo("WAITING_FOR_PARTS");
        assertThat(created.isDefault()).isTrue();
        assertThat(service.defaultStatus(org).getName()).isEqualTo("Waiting for parts");
    }

    @Test
    void namesAreUniqueIgnoringCase() {
        service.create(request("Open", false));

        assertThatThrownBy(() -> service.create(request("  oPEN ", false)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("already exists");
    }

    @Test
    void colourMustBeAPaletteNameOrHex() {
        assertThat(service.create(new TicketStatusRequest("Hex", null, null, "#12ab9F", null, null, false, null)).color()).isEqualTo("#12ab9F");
        assertThatThrownBy(() -> service.create(new TicketStatusRequest("Bad", null, null, "rainbow", null, null, false, null)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Unknown colour");
    }

    @Test
    void statusInUseIsNotDeletedWithoutReassignment() {
        service.create(request("Open", false));
        var done = service.create(request("Done", true));
        when(tickets.countByStatus_Id(done.id())).thenReturn(3L);

        assertThatThrownBy(() -> service.delete(done.id(), null))
                .isInstanceOfSatisfying(TicketStatusInUseException.class, e -> assertThat(e.getTicketCount()).isEqualTo(3));
        verify(repo, never()).delete(any());
    }

    @Test
    void statusInUseIsDeletedAfterMovingItsTickets() {
        var open = service.create(request("Open", false));
        var done = service.create(request("Done", true));
        when(tickets.countByStatus_Id(done.id())).thenReturn(3L);

        service.delete(done.id(), open.id());

        verify(tickets).reassignStatus(5L, done.id(), open.id());
        verify(repo).delete(argThat(s -> s.getId().equals(done.id())));
    }

    @Test
    void defaultStatusCannotBeDeletedOrClosed() {
        var open = service.create(request("Open", false));
        service.create(request("Done", true));

        assertThatThrownBy(() -> service.delete(open.id(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThatThrownBy(() -> service.update(open.id(), request("Open", true)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("cannot be a closed status");
    }

    @Test
    void quickActionTargetsFollowTheClosedFlag() {
        service.create(request("New", false));
        var fixed = service.create(request("Fixed", true));
        var archived = service.create(request("Archived", true));

        assertThat(service.resolveTarget(org).getId()).isEqualTo(fixed.id());
        assertThat(service.closeTarget(org).getId()).isEqualTo(archived.id());
        assertThat(service.reopenTarget(org).getName()).isEqualTo("New");
    }

    @Test
    void inactiveStatusesCannotBeAssigned() {
        service.create(request("Open", false));
        var old = service.create(new TicketStatusRequest("Old", null, null, "gray", null, null, false, false));
        when(repo.findByIdAndOrganisation_Id(old.id(), 5L)).thenReturn(Optional.of(stored.get(1)));

        assertThatThrownBy(() -> service.requireAssignable(org, old.id(), null)).hasMessageContaining("inactive");
    }
}
