package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.repository.DepartmentRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.ticketing.domain.Ticket;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketActivity;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketActivityType;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCategory;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketMessage;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketOrigin;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriority;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketReadState;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketActivityRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketMessageRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketReadStateRepository;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Opt-in, idempotent local data only.  It intentionally does not run in a
 * deployed environment unless projectum.seed.local=true is explicitly set.
 */
@Component
@ConditionalOnProperty(name = "projectum.seed.local", havingValue = "true")
public class DevelopmentTicketingDataSeeder implements CommandLineRunner {

    private static final String DEVELOPMENT_PASSWORD = "Projectum123!";

    private final OrganisationRepository organisations;
    private final DepartmentRepository departments;
    private final UserRepository users;
    private final TicketRepository tickets;
    private final TicketMessageRepository messages;
    private final TicketActivityRepository activities;
    private final TicketReadStateRepository readStates;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentTicketingDataSeeder(
            OrganisationRepository organisations,
            DepartmentRepository departments,
            UserRepository users,
            TicketRepository tickets,
            TicketMessageRepository messages,
            TicketActivityRepository activities,
            TicketReadStateRepository readStates,
            PasswordEncoder passwordEncoder
    ) {
        this.organisations = organisations;
        this.departments = departments;
        this.users = users;
        this.tickets = tickets;
        this.messages = messages;
        this.activities = activities;
        this.readStates = readStates;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Organisation ercopac = organisation("ERCOPAC", "ERCOPAC", "Germany");
        Organisation demo = organisation("DEMO_CLIENT", "Demo Client Organisation", "Germany");
        Department ercopacEngineering = department(ercopac, "ENGINEERING", "Engineering");
        Department demoEngineering = department(demo, "ENGINEERING", "Engineering");

        AppUser owner = user("owner@projectum.local", "Platform Owner", Role.PLATFORM_OWNER, null, null, true);
        user("platform.admin@projectum.local", "Platform Administrator", Role.PLATFORM_ADMIN, null, null, true);
        user("org.admin@ercopac.local", "ERCOPAC Organisation Admin", Role.ORG_ADMIN, ercopac, null, true);
        user("gm@ercopac.local", "ERCOPAC General Manager", Role.GENERAL_MANAGER, ercopac, null, true);
        AppUser ercopacDm = user("dm.engineering@ercopac.local", "ERCOPAC Engineering Manager", Role.DEPARTMENT_MANAGER, ercopac, ercopacEngineering, true);
        user("employee.engineering@ercopac.local", "ERCOPAC Engineer", Role.EMPLOYEE, ercopac, ercopacEngineering, true);
        AppUser ercopacSales = user("sales.manager@ercopac.local", "ERCOPAC Sales Manager", Role.SALES_MANAGER, ercopac, null, true);
        AppUser ercopacClient = user("client@ercopac.local", "ERCOPAC Client Contact", Role.CLIENT, ercopac, null, false);

        user("gm@demo.local", "Demo General Manager", Role.GENERAL_MANAGER, demo, null, true);
        AppUser demoDm = user("dm@demo.local", "Demo Department Manager", Role.DEPARTMENT_MANAGER, demo, demoEngineering, true);
        user("employee@demo.local", "Demo Employee", Role.EMPLOYEE, demo, demoEngineering, true);
        AppUser demoSales = user("sales@demo.local", "Demo Sales Manager", Role.SALES_MANAGER, demo, null, true);
        AppUser demoClient = user("client@demo.local", "Demo Client Contact", Role.CLIENT, demo, null, false);

        ercopacEngineering.setManager(ercopacDm);
        demoEngineering.setManager(demoDm);
        departments.save(ercopacEngineering);
        departments.save(demoEngineering);

        ticket("TKT-2026-100001", ercopac, ercopacClient, ercopacClient, null,
                "Unable to activate new user account", TicketCategory.ACCESS, TicketPriority.HIGH, TicketStatus.OPEN,
                "Berlin HQ", "A new employee cannot access the planning workspace.", false);
        ticket("TKT-2026-100002", ercopac, ercopacClient, ercopacClient, ercopacSales,
                "Integration timeout on purchase orders", TicketCategory.INTEGRATION, TicketPriority.CRITICAL, TicketStatus.IN_PROGRESS,
                "Berlin HQ", "The ERP integration times out during peak processing.", true);
        ticket("TKT-2026-100003", ercopac, ercopacSales, ercopacClient, ercopacSales,
                "Slow dashboard load for engineering", TicketCategory.PERFORMANCE, TicketPriority.MEDIUM, TicketStatus.ESCALATED,
                "Munich Site", "Engineering dashboard needs more than 20 seconds to load.", true);
        ticket("TKT-2026-100004", ercopac, ercopacClient, ercopacClient, ercopacSales,
                "Clarify invoice export fields", TicketCategory.BILLING, TicketPriority.LOW, TicketStatus.RESOLVED,
                "Berlin HQ", "The client requested clarification of the invoice export columns.", true);
        ticket("TKT-2026-100005", ercopac, ercopacSales, ercopacClient, ercopacSales,
                "Feature request: saved ticket filters", TicketCategory.FEATURE_REQUEST, TicketPriority.LOW, TicketStatus.CLOSED,
                "Berlin HQ", "Saved ticket filters have been delivered and accepted.", true);
        ticket("TKT-2026-200001", demo, demoClient, demoClient, demoSales,
                "Demo tenant data import question", TicketCategory.DATA, TicketPriority.MEDIUM, TicketStatus.OPEN,
                "Hamburg Site", "Imported project dates require confirmation.", true);

        // Keep a platform-level actor in the object graph for testing platform visibility.
        if (owner.getId() == null) {
            throw new IllegalStateException("Development platform owner was not persisted");
        }
    }

    private Organisation organisation(String code, String name, String country) {
        return organisations.findByCode(code).orElseGet(() -> {
            Organisation organisation = new Organisation();
            organisation.setCode(code);
            organisation.setName(name);
            organisation.setCountry(country);
            organisation.setDomain(code.toLowerCase() + ".local");
            organisation.setStatus(OrganisationStatus.ACTIVE);
            organisation.setActive(true);
            organisation.setPlan("ENTERPRISE");
            organisation.setUserLimit(100);
            organisation.setOrgAdminLicenceLimit(10);
            organisation.setGeneralManagerLicenceLimit(10);
            organisation.setDepartmentManagerLicenceLimit(20);
            organisation.setEmployeeLicenceLimit(100);
            return organisations.save(organisation);
        });
    }

    private Department department(Organisation organisation, String code, String label) {
        return departments.findByCodeAndOrganisation_Id(code, organisation.getId()).orElseGet(() ->
                departments.save(new Department(code, label, organisation)));
    }

    private AppUser user(String email, String name, Role role, Organisation organisation, Department department, boolean internal) {
        return users.findByEmailIgnoreCase(email).map(existing -> {
            existing.setFullName(name);
            existing.setRole(role);
            existing.setOrganisation(organisation);
            existing.setDepartment(department);
            existing.setDepartmentCode(department == null ? null : department.getCode());
            existing.setInternalUser(internal);
            existing.setActive(true);
            existing.setJobTitle(role.name());
            existing.setDefaultRate(new BigDecimal("120"));
            if (!passwordEncoder.matches(DEVELOPMENT_PASSWORD, existing.getPasswordHash())) {
                existing.setPasswordHash(passwordEncoder.encode(DEVELOPMENT_PASSWORD));
            }
            return users.save(existing);
        }).orElseGet(() -> {
            AppUser created = new AppUser(name, email, passwordEncoder.encode(DEVELOPMENT_PASSWORD), role);
            created.setOrganisation(organisation);
            created.setDepartment(department);
            created.setDepartmentCode(department == null ? null : department.getCode());
            created.setInternalUser(internal);
            created.setJobTitle(role.name());
            created.setDefaultRate(new BigDecimal("120"));
            return users.save(created);
        });
    }

    private void ticket(
            String number,
            Organisation organisation,
            AppUser creator,
            AppUser client,
            AppUser assignee,
            String subject,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            String site,
            String description,
            boolean includeInternalNote
    ) {
        if (tickets.findByTicketNumber(number).isPresent()) {
            return;
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(number);
        ticket.setOrganisation(organisation);
        ticket.setCreatedByUser(creator);
        ticket.setClientUser(client);
        ticket.setAssignedSalesManager(assignee);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setOrigin(TicketOrigin.WEB);
        ticket.setSite(site);
        ticket.setEscalationLevel(status == TicketStatus.ESCALATED ? 1 : 0);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(Instant.now().minusSeconds(86_400));
        }
        if (status == TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now().minusSeconds(3_600));
        }
        ticket = tickets.save(ticket);

        activity(ticket, creator, TicketActivityType.CREATED, null, status.name(), "Ticket created");
        if (assignee != null) {
            activity(ticket, assignee, TicketActivityType.ASSIGNED, null, assignee.getFullName(), "Assigned to sales manager");
        }
        if (status != TicketStatus.OPEN) {
            activity(ticket, assignee == null ? creator : assignee, activityFor(status), TicketStatus.OPEN.name(), status.name(), "Ticket lifecycle updated");
        }

        message(ticket, client, "We need help with: " + subject, false);
        if (assignee != null) {
            message(ticket, assignee, "Thanks, we are reviewing this request and will update you shortly.", false);
        }
        if (includeInternalNote && assignee != null) {
            message(ticket, assignee, "Internal note: verify the tenant configuration before replying.", true);
        }

        TicketReadState clientReadState = new TicketReadState();
        clientReadState.setTicket(ticket);
        clientReadState.setUser(client);
        clientReadState.setLastReadAt(Instant.now().minusSeconds(3_600));
        readStates.save(clientReadState);
    }

    private TicketActivityType activityFor(TicketStatus status) {
        return switch (status) {
            case IN_PROGRESS -> TicketActivityType.STATUS_CHANGED;
            case ESCALATED -> TicketActivityType.ESCALATED;
            case RESOLVED -> TicketActivityType.RESOLVED;
            case CLOSED -> TicketActivityType.CLOSED;
            case REOPENED -> TicketActivityType.REOPENED;
            default -> TicketActivityType.UPDATED;
        };
    }

    private void activity(Ticket ticket, AppUser actor, TicketActivityType type, String previous, String next, String description) {
        TicketActivity activity = new TicketActivity();
        activity.setTicket(ticket);
        activity.setActor(actor);
        activity.setActivityType(type);
        activity.setPreviousValue(previous);
        activity.setNewValue(next);
        activity.setDescription(description);
        activities.save(activity);
    }

    private void message(Ticket ticket, AppUser sender, String body, boolean internalNote) {
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(body);
        message.setInternalNote(internalNote);
        message.setMessageType(internalNote ? "INTERNAL_NOTE" : "MESSAGE");
        messages.save(message);
    }
}
