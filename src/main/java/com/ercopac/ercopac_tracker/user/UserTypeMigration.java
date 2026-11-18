package com.ercopac.ercopac_tracker.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Customer users: users.user_type (INTERNAL / CUSTOMER) and users.customer_id.
 *
 * Hibernate schema update adds both columns (user_type with default 'INTERNAL', so every existing
 * account becomes INTERNAL). This runner then, idempotently:
 * 1. fills a missing user_type with INTERNAL (safety net for rows written before the default);
 * 2. converts legacy CLIENT portal accounts: an INTERNAL account whose only role is CLIENT and that
 *    is linked, through ticket_customer_contacts.portal_user_id, to exactly one customer of its own
 *    organisation becomes CUSTOMER with that customer. Accounts linked to no customer or to several,
 *    or that have other roles too, are left unchanged and listed in the log for a manual fix;
 * 3. adds the users_customer_type_check constraint: CUSTOMER rows have a customer, INTERNAL rows none.
 * A second run finds nothing left to convert and the constraint already present.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class UserTypeMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserTypeMigration.class);
    static final String CONSTRAINT = "users_customer_type_check";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final TransactionTemplate transactionTemplate;

    public UserTypeMigration(JdbcTemplate jdbcTemplate, DataSource dataSource, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres() || !columnExists("users", "user_type") || !columnExists("users", "customer_id")) {
            return;
        }
        boolean contacts = columnExists("ticket_customer_contacts", "portal_user_id");

        transactionTemplate.executeWithoutResult(status -> {
            int filled = jdbcTemplate.update("update public.users set user_type = 'INTERNAL' where user_type is null");
            if (filled > 0) {
                log.info("User type migration: {} account(s) set to INTERNAL", filled);
            }
            if (contacts) {
                convertLegacyClients();
            }
            addConstraint();
        });
    }

    private void convertLegacyClients() {
        // Legacy CLIENT accounts not converted yet, with the distinct customers of their own organisation
        // reachable through a customer contact, and whether they hold any role other than CLIENT
        List<Map<String, Object>> candidates = jdbcTemplate.queryForList("""
                select u.id, u.email,
                       count(distinct c.customer_id) as customers,
                       min(c.customer_id) as customer_id,
                       (select count(*) from public.user_roles r2
                         where r2.user_id = u.id and r2.role <> 'CLIENT') as other_roles
                from public.users u
                join public.user_roles r on r.user_id = u.id and r.role = 'CLIENT'
                left join public.ticket_customer_contacts c
                       on c.portal_user_id = u.id and c.organisation_id = u.organisation_id
                where u.user_type = 'INTERNAL' and u.customer_id is null
                group by u.id, u.email
                order by u.id
                """);

        int converted = 0;
        for (Map<String, Object> row : candidates) {
            Long userId = ((Number) row.get("id")).longValue();
            String email = String.valueOf(row.get("email"));
            long customers = ((Number) row.get("customers")).longValue();
            long otherRoles = ((Number) row.get("other_roles")).longValue();

            if (customers == 1 && otherRoles == 0) {
                Long customerId = ((Number) row.get("customer_id")).longValue();
                converted += jdbcTemplate.update("""
                        update public.users set user_type = 'CUSTOMER', customer_id = ?, internal_user = false
                        where id = ? and user_type = 'INTERNAL' and customer_id is null
                        """, customerId, userId);
                log.info("User type migration: CLIENT account {} (id {}) converted to CUSTOMER of customer id {}",
                        email, userId, customerId);
            } else {
                String reason = otherRoles > 0 ? "has roles other than CLIENT"
                        : customers == 0 ? "is linked to no customer contact" : "is linked to " + customers + " customers";
                log.warn("User type migration: CLIENT account {} (id {}) left INTERNAL, it {}. "
                        + "Fix it manually (Customer Users page or customer contacts).", email, userId, reason);
            }
        }
        if (converted > 0) {
            log.info("User type migration: {} CLIENT account(s) converted to CUSTOMER", converted);
        }
    }

    private void addConstraint() {
        Integer present = jdbcTemplate.queryForObject(
                "select count(*) from pg_constraint where conname = ?", Integer.class, CONSTRAINT);
        if (present != null && present > 0) {
            return;
        }
        Integer invalid = jdbcTemplate.queryForObject("""
                select count(*) from public.users
                where (user_type = 'CUSTOMER') <> (customer_id is not null)
                """, Integer.class);
        if (invalid != null && invalid > 0) {
            log.warn("User type migration: {} account(s) break the rule 'CUSTOMER has a customer, INTERNAL has none'; "
                    + "constraint {} not added", invalid, CONSTRAINT);
            return;
        }
        jdbcTemplate.execute("alter table public.users add constraint " + CONSTRAINT
                + " check ((user_type = 'CUSTOMER') = (customer_id is not null))");
        log.info("User type migration: constraint {} added", CONSTRAINT);
    }

    private boolean isPostgres() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres");
        }
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getColumns(null, "public", table, column)) {
            return rs.next();
        }
    }
}
