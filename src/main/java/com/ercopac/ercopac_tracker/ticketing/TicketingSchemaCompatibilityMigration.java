package com.ercopac.ercopac_tracker.ticketing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Compatibility work for the Ticketing links with Projectum (customer / project snapshots,
 * equipment project, Ticketing permission module). Hibernate schema update adds the new
 * nullable columns and the ticket_customer_projects table; this runner only does what it
 * cannot do, idempotently and without touching existing values:
 * - copy the linked customer and project onto tickets created before the snapshot columns;
 * - drop the role_permissions module CHECK constraint generated before TICKETING existed;
 * - keep equipment linked to at most one project (Projectum or manual);
 * - drop the tickets.category enum CHECK so custom categories can be stored (see V32 script).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TicketingSchemaCompatibilityMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public TicketingSchemaCompatibilityMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres()) return;

        jdbcTemplate.execute("alter table if exists public.role_permissions drop constraint if exists role_permissions_module_check");
        // Ticket categories are configurable now (system + custom codes): drop the enum CHECK Hibernate generated on tickets.category
        jdbcTemplate.execute("alter table if exists public.tickets drop constraint if exists tickets_category_check");

        if (columnExists("tickets", "customer_name")) {
            jdbcTemplate.update("""
                    update public.tickets t set customer_name = c.name
                    from public.customers c
                    where t.customer_id = c.id and t.customer_name is null
                    """);
        }
        if (columnExists("tickets", "project_name")) {
            jdbcTemplate.update("""
                    update public.tickets t
                    set project_name = p.name,
                        project_number = coalesce(t.project_number, p.code),
                        project_year = coalesce(t.project_year, cast(extract(year from p.planned_start) as integer))
                    from public.projects p
                    where t.project_id = p.id and t.project_name is null
                    """);
        }
        if (columnExists("ticket_customer_equipment", "customer_project_id")) {
            jdbcTemplate.execute("""
                    do $$
                    begin
                      if not exists (select 1 from pg_constraint where conname = 'ck_ticket_equipment_one_project') then
                        alter table public.ticket_customer_equipment add constraint ck_ticket_equipment_one_project
                          check (project_id is null or customer_project_id is null);
                      end if;
                    end $$
                    """);
        }
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.columns
                where lower(table_schema) = 'public'
                  and lower(table_name) = lower(?)
                  and lower(column_name) = lower(?)
                """, Integer.class, table, column);
        return count != null && count > 0;
    }

    private boolean isPostgres() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
        }
    }
}
