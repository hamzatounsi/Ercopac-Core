package com.ercopac.ercopac_tracker.milestone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Compatibility work for installations created before milestone types became
 * project-scoped. Hibernate adds columns but deliberately does not replace an
 * existing unique constraint, which would otherwise keep a display code global.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MilestoneSchemaCompatibilityMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public MilestoneSchemaCompatibilityMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        jdbcTemplate.execute("alter table if exists public.milestone_types add column if not exists project_id bigint");

        if (isPostgres()) {
            // Hibernate's former @Column(unique = true) generated a provider
            // specific constraint name. Remove only a one-column unique
            // constraint on code, then enforce uniqueness per project.
            jdbcTemplate.execute("""
                    do $$
                    declare old_constraint text;
                    begin
                      select c.conname into old_constraint
                      from pg_constraint c
                      join pg_class t on t.oid = c.conrelid
                      join pg_namespace n on n.oid = t.relnamespace
                      where n.nspname = 'public' and t.relname = 'milestone_types'
                        and c.contype = 'u'
                        and pg_get_constraintdef(c.oid) = 'UNIQUE (code)'
                      limit 1;
                      if old_constraint is not null then
                        execute format('alter table public.milestone_types drop constraint %I', old_constraint);
                      end if;
                    end $$
                    """);
            jdbcTemplate.execute("drop index if exists public.ux_milestone_types_project_code");
            jdbcTemplate.execute("create unique index if not exists ux_milestone_types_project_label on public.milestone_types (project_id, lower(label))");
        } else {
            jdbcTemplate.execute("drop index if exists ux_milestone_types_project_code");
            jdbcTemplate.execute("create unique index if not exists ux_milestone_types_project_label on public.milestone_types (project_id, label)");
        }
    }

    private boolean isPostgres() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
        }
    }
}
