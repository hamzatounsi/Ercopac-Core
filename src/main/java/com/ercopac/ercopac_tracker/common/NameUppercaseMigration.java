package com.ercopac.ercopac_tracker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Brings names stored before the uppercase rule ({@link NameNormalizer}) in line with it:
 * user full names (resources), customers, suppliers, organisations and the customer
 * labels denormalised on projects and tickets.
 *
 * Idempotent: each statement only touches rows whose value differs from its normalised
 * form, so a second run updates nothing. Organisation names are unique, so a row whose
 * uppercase form would clash with another organisation is skipped and logged instead of
 * aborting startup.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class NameUppercaseMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NameUppercaseMigration.class);

    /** SQL equivalent of {@link NameNormalizer#upper(String)}. */
    static String normalized(String column) {
        return "upper(regexp_replace(trim(" + column + "), '\\s+', ' ', 'g'))";
    }

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public NameUppercaseMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres()) {
            return;
        }

        uppercase("users", "full_name");
        uppercase("customers", "name");
        uppercase("suppliers", "name");
        uppercase("projects", "customer");
        uppercase("tickets", "customer_name");
        uppercaseOrganisations();
    }

    private void uppercase(String table, String column) throws SQLException {
        if (!columnExists(table, column)) {
            return;
        }
        String target = normalized(column);
        int updated = jdbcTemplate.update(
                "update public." + table + " set " + column + " = " + target
                        + " where " + column + " is not null and " + column + " <> " + target);
        if (updated > 0) {
            log.info("Name uppercase migration: {} row(s) normalised in {}.{}", updated, table, column);
        }
    }

    private void uppercaseOrganisations() throws SQLException {
        if (!columnExists("organisations", "name")) {
            return;
        }
        String target = normalized("o.name");

        List<Map<String, Object>> clashes = jdbcTemplate.queryForList(
                "select o.id, o.name from public.organisations o"
                        + " where o.name <> " + target
                        + " and exists (select 1 from public.organisations other where other.id <> o.id"
                        + " and " + normalized("other.name") + " = " + target + ")");
        for (Map<String, Object> clash : clashes) {
            log.warn("Name uppercase migration: organisation id={} name='{}' kept as is, its uppercase form "
                    + "is already used by another organisation. Rename one of them manually.",
                    clash.get("id"), clash.get("name"));
        }

        int updated = jdbcTemplate.update(
                "update public.organisations o set name = " + target
                        + " where o.name <> " + target
                        + " and not exists (select 1 from public.organisations other where other.id <> o.id"
                        + " and " + normalized("other.name") + " = " + target + ")");
        if (updated > 0) {
            log.info("Name uppercase migration: {} row(s) normalised in organisations.name", updated);
        }
    }

    private boolean isPostgres() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
        }
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getColumns(null, "public", table, column)) {
            return rs.next();
        }
    }
}
