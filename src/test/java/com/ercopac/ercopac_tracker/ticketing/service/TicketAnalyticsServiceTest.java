package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.crm.domain.CrmEquipmentType;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.ticketing.domain.*;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketMessageRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAnalyticsServiceTest {

    private static final PrioritySettingDto HIGH = new PrioritySettingDto(TicketPriority.HIGH, "High", 60, 8);
    private static final Instant NOW = Instant.now();

    @Mock TicketRepository tickets;
    @Mock TicketMessageRepository messages;
    @Mock UserRepository users;
    @Mock TicketSettingsService settings;

    private TicketAnalyticsService service;
    private Organisation organisation;
    private AppUser lead;

    @BeforeEach
    void setUp() {
        service = new TicketAnalyticsService(tickets, messages, users, settings);
        organisation = new Organisation();
        organisation.setId(5L);
        lead = new AppUser("Lead", "lead@example.test", "x", Role.H24_LEAD);
        ReflectionTestUtils.setField(lead, "id", 1L);
        lead.setOrganisation(organisation);
    }

    @Test
    void ticketMeetsSlaOnlyWhenResponseAndResolutionAreWithinTargets() {
        Ticket fast = ticket(1L, TicketStatus.RESOLVED, NOW.minus(10, ChronoUnit.HOURS));
        fast.setResolvedAt(fast.getCreatedAt().plus(5, ChronoUnit.HOURS));
        Ticket slowResponse = ticket(2L, TicketStatus.RESOLVED, NOW.minus(10, ChronoUnit.HOURS));
        slowResponse.setResolvedAt(slowResponse.getCreatedAt().plus(5, ChronoUnit.HOURS));

        var met = TicketAnalyticsService.measure(fast, fast.getCreatedAt().plus(30, ChronoUnit.MINUTES), HIGH, NOW);
        var breached = TicketAnalyticsService.measure(slowResponse, slowResponse.getCreatedAt().plus(2, ChronoUnit.HOURS), HIGH, NOW);

        assertThat(met.slaMet()).isTrue();
        assertThat(met.responseHours()).isEqualTo(0.5);
        assertThat(met.resolutionHours()).isEqualTo(5.0);
        assertThat(breached.slaMet()).isFalse();
        assertThat(TicketAnalyticsService.compliance(List.of(met, breached))).isEqualTo(50.0);
    }

    @Test
    void unfinishedTicketIsMeasuredUpToNowAndFlaggedOverdue() {
        Ticket old = ticket(3L, TicketStatus.IN_PROGRESS, NOW.minus(9, ChronoUnit.HOURS));
        Ticket fresh = ticket(4L, TicketStatus.OPEN, NOW.minus(10, ChronoUnit.MINUTES));

        var oldMeasure = TicketAnalyticsService.measure(old, old.getCreatedAt().plus(20, ChronoUnit.MINUTES), HIGH, NOW);
        var freshMeasure = TicketAnalyticsService.measure(fresh, null, HIGH, NOW);

        assertThat(oldMeasure.overdue()).isTrue();
        assertThat(oldMeasure.slaMet()).isFalse();
        assertThat(freshMeasure.overdue()).isFalse();
        assertThat(freshMeasure.slaMet()).isTrue();
        assertThat(freshMeasure.responseHours()).isNull();
    }

    @Test
    void cancelledTicketsAreLeftOutOfSlaFigures() {
        Ticket cancelled = ticket(5L, TicketStatus.CANCELLED, NOW.minus(30, ChronoUnit.HOURS));
        var measure = TicketAnalyticsService.measure(cancelled, null, HIGH, NOW);

        assertThat(measure.slaMet()).isNull();
        assertThat(TicketAnalyticsService.compliance(List.of(measure))).isNull();
        assertThat(measure.overdue()).isFalse();
    }

    @Test
    void analyticsGroupsByCustomerAgentAndDay() {
        Customer acme = new Customer();
        ReflectionTestUtils.setField(acme, "id", 9L);
        acme.setName("Acme");
        AppUser agent = new AppUser("Agent", "agent@example.test", "x", Role.H24);
        ReflectionTestUtils.setField(agent, "id", 2L);

        Ticket resolved = ticket(10L, TicketStatus.RESOLVED, NOW.minus(2, ChronoUnit.DAYS));
        resolved.setResolvedAt(resolved.getCreatedAt().plus(2, ChronoUnit.HOURS));
        resolved.setCustomer(acme);
        resolved.setAssignedSalesManager(agent);
        Ticket open = ticket(11L, TicketStatus.OPEN, NOW.minus(1, ChronoUnit.HOURS));

        Instant from = NOW.minus(7, ChronoUnit.DAYS);
        when(settings.requireLead()).thenReturn(lead);
        when(tickets.findForAnalytics(eq(5L), eq(from), eq(NOW), isNull())).thenReturn(List.of(resolved, open));
        when(settings.priorities(5L)).thenReturn(Map.of(TicketPriority.HIGH, HIGH));
        List<Object[]> responses = new ArrayList<>();
        responses.add(new Object[]{10L, resolved.getCreatedAt().plus(15, ChronoUnit.MINUTES)});
        when(messages.firstResponses(anyCollection())).thenReturn(responses);
        when(settings.profilesByUser(5L)).thenReturn(Map.of());
        when(users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(eq(5L), anyCollection())).thenReturn(List.of(agent));

        TicketAnalytics result = service.analytics(from, NOW, null);

        assertThat(result.bucket()).isEqualTo("day");
        assertThat(result.timeline()).hasSize((int) Duration.between(from, NOW).toDays() + 1);
        assertThat(result.timeline().stream().mapToLong(Breakdown::count).sum()).isEqualTo(2);
        assertThat(result.totals().total()).isEqualTo(2);
        assertThat(result.totals().open()).isEqualTo(1);
        assertThat(result.totals().resolved()).isEqualTo(1);
        assertThat(result.totals().avgFirstResponseHours()).isEqualTo(0.3);
        assertThat(result.totals().slaCompliance()).isEqualTo(100.0);
        assertThat(result.byCustomer()).extracting(CustomerStat::name).containsExactly("Acme", "No customer");
        assertThat(result.agents()).singleElement().satisfies(a -> {
            assertThat(a.assigned()).isEqualTo(1);
            assertThat(a.resolved()).isEqualTo(1);
            assertThat(a.open()).isZero();
        });
    }

    private Ticket ticket(Long id, TicketStatus status, Instant createdAt) {
        Ticket t = new Ticket();
        ReflectionTestUtils.setField(t, "id", id);
        ReflectionTestUtils.setField(t, "createdAt", createdAt);
        ReflectionTestUtils.setField(t, "updatedAt", createdAt);
        t.setOrganisation(organisation);
        t.setPriority(TicketPriority.HIGH);
        t.setStatus(status);
        t.setTicketNumber("TKT-" + id);
        return t;
    }

    @Test
    void equipmentFilterMatchesTypeAndSerialNumber() {
        CrmEquipmentType robot = new CrmEquipmentType();
        ReflectionTestUtils.setField(robot, "id", 4L);
        TicketCustomerEquipment arm = new TicketCustomerEquipment();
        arm.setEquipmentType(robot);
        arm.setSerialNumber("SN-ARM-0042");

        assertThat(TicketAnalyticsService.matchesEquipment(arm, 4L, null)).isTrue();
        assertThat(TicketAnalyticsService.matchesEquipment(arm, 5L, null)).isFalse();
        assertThat(TicketAnalyticsService.matchesEquipment(arm, null, "arm-00")).isTrue();
        assertThat(TicketAnalyticsService.matchesEquipment(arm, 4L, "other")).isFalse();
        assertThat(TicketAnalyticsService.matchesEquipment(null, null, "arm")).isFalse();
    }
}
