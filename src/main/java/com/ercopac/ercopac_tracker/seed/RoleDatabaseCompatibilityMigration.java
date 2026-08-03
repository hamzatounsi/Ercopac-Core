package com.ercopac.ercopac_tracker.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This project uses Hibernate schema update rather than Flyway.  Hibernate
 * does not revise an existing PostgreSQL CHECK constraint when a Java enum
 * grows, so role compatibility is handled explicitly and idempotently here.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoleDatabaseCompatibilityMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public RoleDatabaseCompatibilityMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        jdbcTemplate.execute("alter table public.users drop constraint if exists users_role_check");

        jdbcTemplate.update("""
                update public.users
                set role = case upper(trim(role))
                    when 'OWNER' then 'PLATFORM_OWNER'
                    when 'ADMIN' then 'PLATFORM_ADMIN'
                    when 'ORGANIZATION_ADMIN' then 'ORG_ADMIN'
                    when 'ORGANISATION_ADMIN' then 'ORG_ADMIN'
                    else upper(trim(role))
                end
                where role is not null
                """);

        Integer unsupportedRoles = jdbcTemplate.queryForObject("""
                select count(*) from public.users
                where role is not null
                  and role not in (
                    'PLATFORM_OWNER', 'PLATFORM_ADMIN', 'ORG_ADMIN',
                    'GENERAL_MANAGER', 'DEPARTMENT_MANAGER', 'EMPLOYEE',
                    'SALES_MANAGER', 'CLIENT'
                  )
                """, Integer.class);
        if (unsupportedRoles != null && unsupportedRoles > 0) {
            throw new IllegalStateException("Unsupported role values remain in users; migration was not applied");
        }

        jdbcTemplate.execute("""
                alter table public.users add constraint users_role_check check (
                    role in (
                        'PLATFORM_OWNER', 'PLATFORM_ADMIN', 'ORG_ADMIN',
                        'GENERAL_MANAGER', 'DEPARTMENT_MANAGER', 'EMPLOYEE',
                        'SALES_MANAGER', 'CLIENT'
                    )
                )
                """);
    }
}
