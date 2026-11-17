package com.ercopac.ercopac_tracker.user;

import com.ercopac.ercopac_tracker.security.DepartmentScope;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Links suppliers that have no resource type yet to the resource types of the departments listed in
 * their {@code departments_csv}, so the Department Manager scope ({@link DepartmentScope}) can find them.
 *
 * <p>For each department code, the active resource types linked to that department are used; when the
 * department has none, the historical department code map is used (only codes that exist as active
 * resource types of the organisation). Suppliers with no departments, or whose departments resolve to
 * nothing, are left unchanged: they stay visible to administrators and project managers and hidden from
 * Department Managers until a resource type is set.
 *
 * <p>Every link is logged ("Supplier resource type backfill: ..."). Idempotent: only suppliers with no
 * row in {@code supplier_resource_types} are processed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class SupplierResourceTypeBackfillMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SupplierResourceTypeBackfillMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final TransactionTemplate transactionTemplate;

    public SupplierResourceTypeBackfillMigration(JdbcTemplate jdbcTemplate, DataSource dataSource,
                                                 TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgres()
                || !columnExists("suppliers", "departments_csv")
                || !columnExists("supplier_resource_types", "resource_type_id")
                || !columnExists("resource_types", "department_id")) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList("""
                    select s.id, s.organisation_id, s.name, s.departments_csv
                    from public.suppliers s
                    where s.departments_csv is not null and trim(s.departments_csv) <> ''
                      and not exists (select 1 from public.supplier_resource_types srt where srt.supplier_id = s.id)
                    order by s.id
                    """);
            int linkedSuppliers = 0;
            for (Map<String, Object> supplier : suppliers) {
                Long supplierId = ((Number) supplier.get("id")).longValue();
                Long organisationId = ((Number) supplier.get("organisation_id")).longValue();
                String csv = (String) supplier.get("departments_csv");

                Map<Long, String> types = new LinkedHashMap<>();
                for (String department : departmentCodes(csv)) {
                    types.putAll(resourceTypesFor(organisationId, department));
                }
                if (types.isEmpty()) {
                    log.info("Supplier resource type backfill: supplier id={} '{}' (organisation {}) departments '{}' -> no resource type found, left unchanged",
                            supplierId, supplier.get("name"), organisationId, csv);
                    continue;
                }
                for (Map.Entry<Long, String> type : types.entrySet()) {
                    jdbcTemplate.update("""
                            insert into public.supplier_resource_types (supplier_id, resource_type_id)
                            select ?, ? where not exists (
                                select 1 from public.supplier_resource_types where supplier_id = ? and resource_type_id = ?)
                            """, supplierId, type.getKey(), supplierId, type.getKey());
                }
                linkedSuppliers++;
                log.info("Supplier resource type backfill: supplier id={} '{}' (organisation {}) departments '{}' -> resource types {}",
                        supplierId, supplier.get("name"), organisationId, csv, types.values());
            }
            if (linkedSuppliers > 0) {
                log.info("Supplier resource type backfill: {} supplier(s) linked to resource types.", linkedSuppliers);
            }
        });
    }

    /** Resource type id → code for one department of an organisation. */
    private Map<Long, String> resourceTypesFor(Long organisationId, String departmentCode) {
        Map<Long, String> types = new LinkedHashMap<>();
        jdbcTemplate.query("""
                select rt.id, rt.code from public.resource_types rt
                join public.departments d on d.id = rt.department_id
                where rt.organisation_id = ? and rt.active = true and upper(d.code) = upper(?)
                order by rt.code
                """, rs -> {
            types.put(rs.getLong("id"), rs.getString("code"));
        }, organisationId, departmentCode);
        if (!types.isEmpty()) {
            return types;
        }

        List<String> legacy = DepartmentScope.legacyResourceTypes(departmentCode);
        if (legacy.isEmpty()) {
            return types;
        }
        String placeholders = String.join(",", Collections.nCopies(legacy.size(), "?"));
        Object[] params = new Object[legacy.size() + 1];
        params[0] = organisationId;
        for (int i = 0; i < legacy.size(); i++) {
            params[i + 1] = legacy.get(i);
        }
        jdbcTemplate.query("select id, code from public.resource_types where organisation_id = ? and active = true"
                + " and upper(code) in (" + placeholders + ") order by code", rs -> {
            types.put(rs.getLong("id"), rs.getString("code"));
        }, params);
        return types;
    }

    private List<String> departmentCodes(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
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
