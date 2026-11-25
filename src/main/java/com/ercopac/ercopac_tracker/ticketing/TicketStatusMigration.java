package com.ercopac.ercopac_tracker.ticketing;

import com.ercopac.ercopac_tracker.ticketing.service.TicketStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Moves tickets from the old fixed status enum (tickets.status varchar + optional custom_status_id) to the
 * organisation statuses of ticket_statuses (tickets.status_id). Hibernate schema update creates the table and
 * the nullable status_id column; this runner does the rest, idempotently, at every startup:
 * 1. drops NOT NULL / the enum CHECK on the legacy tickets.status column so new tickets can be saved without it;
 * 2. organisations without statuses get their former custom statuses (ticket_custom_statuses), which users created;
 * 3. tickets without status_id get their custom status, else their legacy enum value. No built-in status set is
 *    seeded: a former enum status is only created for an organisation when some of its tickets still use it
 *    (with the label / colour configured in ticket_status_settings), so leads can then reassign and delete it;
 * 4. an organisation with statuses but no default gets one (its first active, non-closed status).
 * Once every ticket has a status_id nothing is created again. Legacy columns and tables are left untouched.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 21)
public class TicketStatusMigration implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TicketStatusMigration.class);
    private static final Set<String> LEGACY_CLOSED = Set.of("RESOLVED", "CLOSED", "CANCELLED");

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final TransactionTemplate tx;

    public TicketStatusMigration(JdbcTemplate jdbc, DataSource dataSource, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.tx = tx;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres() || !tableExists("ticket_statuses") || !columnExists("tickets", "status_id")) return;
        tx.executeWithoutResult(status -> migrate());
    }

    private void migrate() {
        boolean legacyStatus = columnExists("tickets", "status");
        if (legacyStatus) {
            jdbc.execute("alter table public.tickets alter column status drop not null");
            jdbc.execute("alter table public.tickets drop constraint if exists tickets_status_check");
        }
        boolean customTable = tableExists("ticket_custom_statuses");
        boolean customColumn = customTable && columnExists("tickets", "custom_status_id");

        // Former custom statuses were created by users: keep them all (first run only, organisation still empty)
        int imported = 0;
        if (customTable) {
            for (Map<String, Object> c : jdbc.queryForList("""
                    select c.organisation_id, c.code, c.label, c.description, c.color, c.base_status from public.ticket_custom_statuses c
                    where not exists (select 1 from public.ticket_statuses s where s.organisation_id = c.organisation_id)
                    order by c.organisation_id, c.label, c.id
                    """)) {
                Long org = ((Number) c.get("organisation_id")).longValue();
                String code = String.valueOf(c.get("code")).toUpperCase(Locale.ROOT);
                if (codeExists(org, code)) continue;
                boolean closed = LEGACY_CLOSED.contains(String.valueOf(c.get("base_status")).toUpperCase(Locale.ROOT));
                insert(org, code, uniqueName(org, String.valueOf(c.get("label"))), (String) c.get("description"), String.valueOf(c.get("color")), nextOrder(org), false, closed);
                imported++;
            }
        }

        int fromCustom = 0, fromLegacy = 0, legacyCreated = 0;
        if (customColumn) {
            fromCustom = jdbc.update("""
                    update public.tickets t set status_id = s.id
                    from public.ticket_custom_statuses c
                    join public.ticket_statuses s on s.organisation_id = c.organisation_id and upper(s.code) = upper(c.code)
                    where t.status_id is null and t.custom_status_id = c.id
                    """);
        }
        if (legacyStatus) {
            // Only the former enum statuses still used by tickets that have no status yet
            boolean settingsTable = tableExists("ticket_status_settings");
            for (Map<String, Object> row : jdbc.queryForList("select distinct organisation_id, upper(status) as code from public.tickets where status_id is null and status is not null")) {
                Long org = ((Number) row.get("organisation_id")).longValue();
                String code = String.valueOf(row.get("code"));
                if (codeExists(org, code)) continue;
                Optional<TicketStatusService.Seed> seed = TicketStatusService.STANDARD.stream().filter(x -> x.code().equals(code)).findFirst();
                if (seed.isEmpty()) continue;
                String name = seed.get().name(), description = seed.get().description(), color = seed.get().color();
                if (settingsTable) {
                    List<Map<String, Object>> o = jdbc.queryForList("select label, description, color from public.ticket_status_settings where organisation_id = ? and upper(status) = ?", org, code);
                    if (!o.isEmpty()) {
                        if (o.get(0).get("label") != null) name = o.get(0).get("label").toString();
                        description = (String) o.get(0).get("description");
                        if (o.get(0).get("color") != null) color = o.get(0).get("color").toString();
                    }
                }
                insert(org, code, uniqueName(org, name), description, color, TicketStatusService.STANDARD.indexOf(seed.get()), false, seed.get().closed());
                legacyCreated++;
            }
            fromLegacy = jdbc.update("""
                    update public.tickets t set status_id = s.id
                    from public.ticket_statuses s
                    where t.status_id is null and s.organisation_id = t.organisation_id and upper(s.code) = upper(t.status)
                    """);
        }

        // Every organisation with statuses has a default one: OPEN when migrated, else its first active open status
        int defaults = jdbc.update("""
                update public.ticket_statuses s set is_default = true
                where s.id = (select x.id from public.ticket_statuses x
                              where x.organisation_id = s.organisation_id and x.active = true and x.is_closed = false
                              order by case when upper(x.code) = 'OPEN' then 0 else 1 end, x.display_order, x.id limit 1)
                  and not exists (select 1 from public.ticket_statuses d where d.organisation_id = s.organisation_id and d.is_default = true)
                """);
        int fromDefault = jdbc.update("""
                update public.tickets t set status_id = s.id
                from public.ticket_statuses s
                where t.status_id is null and s.organisation_id = t.organisation_id and s.is_default = true
                """);
        if (imported + legacyCreated + fromCustom + fromLegacy + fromDefault + defaults > 0)
            log.info("Ticket statuses migrated: {} custom status(es) imported, {} former status(es) still in use created, {} default(s) set; tickets: {} from custom statuses, {} from legacy codes, {} set to the default",
                    imported, legacyCreated, defaults, fromCustom, fromLegacy, fromDefault);
    }

    private void insert(Long org, String code, String name, String description, String color, int order, boolean isDefault, boolean closed) {
        jdbc.update("insert into public.ticket_statuses (organisation_id, code, name, description, color, display_order, is_default, is_closed, active) values (?, ?, ?, ?, ?, ?, ?, ?, true)",
                org, code, name, description, color, order, isDefault, closed);
    }
    private int nextOrder(Long org) {
        Integer max = jdbc.queryForObject("select coalesce(max(display_order), -1) from public.ticket_statuses where organisation_id = ?", Integer.class, org);
        return (max == null ? -1 : max) + 1;
    }
    // Names are unique per organisation (case-insensitive); a clashing legacy label gets a numeric suffix
    private String uniqueName(Long org, String base) {
        String name = base.length() > 60 ? base.substring(0, 60) : base;
        for (int n = 2; nameExists(org, name); n++) name = (base.length() > 55 ? base.substring(0, 55) : base) + " (" + n + ")";
        return name;
    }
    private boolean nameExists(Long org, String name) {
        Integer c = jdbc.queryForObject("select count(*) from public.ticket_statuses where organisation_id = ? and lower(name) = lower(?)", Integer.class, org, name);
        return c != null && c > 0;
    }
    private boolean codeExists(Long org, String code) {
        Integer c = jdbc.queryForObject("select count(*) from public.ticket_statuses where organisation_id = ? and upper(code) = ?", Integer.class, org, code);
        return c != null && c > 0;
    }
    private boolean tableExists(String table) {
        Integer count = jdbc.queryForObject("select count(*) from information_schema.tables where lower(table_schema) = 'public' and lower(table_name) = lower(?)", Integer.class, table);
        return count != null && count > 0;
    }
    private boolean columnExists(String table, String column) {
        Integer count = jdbc.queryForObject("select count(*) from information_schema.columns where lower(table_schema) = 'public' and lower(table_name) = lower(?) and lower(column_name) = lower(?)",
                Integer.class, table, column);
        return count != null && count > 0;
    }
    private boolean isPostgres() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres");
        }
    }
}
