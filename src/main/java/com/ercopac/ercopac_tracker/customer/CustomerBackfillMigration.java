package com.ercopac.ercopac_tracker.customer;

import com.ercopac.ercopac_tracker.common.NameNormalizer;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Links free-text customers to Customer records.
 *
 * Projects (projects.customer) and tickets (tickets.customer_name) saved without a customer_id
 * are matched to a customer of the same organisation by name, ignoring case and spacing
 * (trim + collapsed whitespace + uppercase, as {@link NameNormalizer#matchKey}). When no
 * customer matches, one is created with a generated AUTO-nnnn code (the same "find or create"
 * the ticketing module already applies to manually typed customers).
 *
 * Every link and creation is logged ("Customer backfill: ...") for review. Idempotent: only rows
 * whose customer_id is still null are processed, and a second run finds the customers created
 * by the first one, so it changes nothing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class CustomerBackfillMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CustomerBackfillMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final TransactionTemplate transactionTemplate;

    public CustomerBackfillMigration(JdbcTemplate jdbcTemplate, DataSource dataSource, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres() || !columnExists("customers", "name")) {
            return;
        }
        boolean projects = columnExists("projects", "customer") && columnExists("projects", "customer_id");
        boolean tickets = columnExists("tickets", "customer_name") && columnExists("tickets", "customer_id");
        if (!projects && !tickets) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            Catalogue catalogue = loadCatalogue();
            int linked = 0;
            if (projects) {
                linked += backfill(catalogue, "project", """
                        select id, organisation_id, customer as text from public.projects
                        where customer_id is null and organisation_id is not null
                          and customer is not null and trim(customer) <> ''
                        order by id
                        """,
                        "update public.projects set customer_id = ?, customer = ? where id = ? and customer_id is null");
            }
            if (tickets) {
                linked += backfill(catalogue, "ticket", """
                        select id, organisation_id, customer_name as text from public.tickets
                        where customer_id is null and organisation_id is not null
                          and customer_name is not null and trim(customer_name) <> ''
                        order by id
                        """,
                        "update public.tickets set customer_id = ?, customer_name = ? where id = ? and customer_id is null");
            }
            if (linked > 0) {
                log.info("Customer backfill: {} record(s) linked, {} customer(s) created.", linked, catalogue.created);
            }
        });
    }

    private int backfill(Catalogue catalogue, String kind, String selectSql, String updateSql) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql);
        int linked = 0;
        for (Map<String, Object> row : rows) {
            Long recordId = ((Number) row.get("id")).longValue();
            Long organisationId = ((Number) row.get("organisation_id")).longValue();
            String text = (String) row.get("text");
            String key = NameNormalizer.matchKey(text);
            if (key == null) {
                continue;
            }

            CustomerRef customer = catalogue.find(organisationId, key);
            boolean created = false;
            if (customer == null) {
                customer = catalogue.create(organisationId, key);
                created = true;
            }

            jdbcTemplate.update(updateSql, customer.id(), customer.name(), recordId);
            linked++;
            log.info("Customer backfill: {} id={} (organisation {}) text '{}' -> {} customer id={} code={} name='{}'",
                    kind, recordId, organisationId, text, created ? "CREATED" : "linked to existing",
                    customer.id(), customer.code(), customer.name());
        }
        return linked;
    }

    private Catalogue loadCatalogue() {
        Catalogue catalogue = new Catalogue();
        jdbcTemplate.query("select id, organisation_id, customer_code, name from public.customers order by id", rs -> {
            catalogue.register(rs.getLong("organisation_id"),
                    new CustomerRef(rs.getLong("id"), rs.getString("customer_code"), rs.getString("name")));
        });
        return catalogue;
    }

    private record CustomerRef(Long id, String code, String name) {
    }

    /** Customers per organisation keyed by normalised name, plus the codes already in use. */
    private final class Catalogue {
        private final Map<Long, Map<String, CustomerRef>> byOrganisation = new HashMap<>();
        private final Map<Long, Set<String>> codes = new HashMap<>();
        private int created;

        void register(Long organisationId, CustomerRef customer) {
            String key = NameNormalizer.matchKey(customer.name());
            if (key != null) {
                // Lowest id wins when legacy data already holds two customers with the same name.
                byOrganisation.computeIfAbsent(organisationId, id -> new HashMap<>()).putIfAbsent(key, customer);
            }
            if (customer.code() != null) {
                codes.computeIfAbsent(organisationId, id -> new HashSet<>()).add(customer.code().toUpperCase(Locale.ROOT));
            }
        }

        CustomerRef find(Long organisationId, String key) {
            return byOrganisation.getOrDefault(organisationId, Map.of()).get(key);
        }

        CustomerRef create(Long organisationId, String name) {
            Set<String> used = codes.computeIfAbsent(organisationId, id -> new HashSet<>());
            int sequence = 1;
            String code;
            do {
                code = String.format(Locale.ROOT, "AUTO-%04d", sequence++);
            } while (used.contains(code));

            Long id = jdbcTemplate.queryForObject(
                    "insert into public.customers (organisation_id, customer_code, name, active) values (?, ?, ?, true) returning id",
                    Long.class, organisationId, code, name);
            CustomerRef customer = new CustomerRef(id, code, name);
            register(organisationId, customer);
            created++;
            return customer;
        }
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
