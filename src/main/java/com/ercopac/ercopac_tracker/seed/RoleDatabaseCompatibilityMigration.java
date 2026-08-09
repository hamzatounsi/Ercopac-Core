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
        // Add the two newly billable organisation role limits without
        // resetting existing organisations or subscriptions.
        jdbcTemplate.execute("alter table public.organisations add column if not exists sales_manager_licence_limit integer not null default 0");
        jdbcTemplate.execute("alter table public.organisations add column if not exists client_licence_limit integer not null default 0");
        jdbcTemplate.execute("alter table public.users drop constraint if exists users_role_check");
        jdbcTemplate.execute("alter table public.role_permissions drop constraint if exists role_permissions_role_check");
        jdbcTemplate.execute("alter table public.admin_licence_assignments drop constraint if exists admin_licence_assignments_licence_type_check");

        jdbcTemplate.update("""
                update public.users
                set role = case upper(trim(role))
                    when 'OWNER' then 'PLATFORM_OWNER'
                    when 'ADMIN' then case when organisation_id is null then 'PLATFORM_OWNER' else 'ORG_ADMIN' end
                    when 'ORGANIZATION_ADMIN' then 'ORG_ADMIN'
                    when 'ORGANISATION_ADMIN' then 'ORG_ADMIN'
                    when 'GENERAL_MANAGER' then 'PROJECT_MANAGER'
                    when 'PLATFORM_ADMIN' then case when organisation_id is null then 'PLATFORM_OWNER' else 'ORG_ADMIN' end
                    else upper(trim(role))
                end
                where role is not null
                """);

        // The legacy allocation enum predated the business roles. Keep old
        // allocations readable while moving them to the final role values.
        jdbcTemplate.update("""
                update public.admin_licence_assignments
                set licence_type = case upper(trim(licence_type))
                    when 'ADMIN' then 'ORG_ADMIN'
                    when 'PM' then 'PROJECT_MANAGER'
                    when 'MANAGER' then 'PROJECT_MANAGER'
                    when 'DEPT_MANAGER' then 'DEPARTMENT_MANAGER'
                    when 'READ_ONLY' then 'EMPLOYEE'
                    else upper(trim(licence_type))
                end
                where licence_type is not null
                """);

        // Non-resource roles must not remain in department capacity/resource
        // planning because of historical seed or admin-profile defaults.
        jdbcTemplate.update("""
                update public.users
                set department_id = null,
                    department_code = null,
                    resource_type_id = null,
                    internal_user = false
                where role in ('PLATFORM_OWNER', 'ORG_ADMIN', 'CLIENT')
                """);

        // Role permissions use the same enum values and must be migrated
        // before permission checks read them.
        jdbcTemplate.update("""
                update public.role_permissions
                set role = case upper(trim(role))
                    when 'GENERAL_MANAGER' then 'PROJECT_MANAGER'
                    when 'PLATFORM_ADMIN' then 'ORG_ADMIN'
                    else upper(trim(role))
                end
                where role is not null
                """);

        Integer unsupportedRoles = jdbcTemplate.queryForObject("""
                select count(*) from public.users
                where role is not null
                  and role not in (
                    'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER',
                    'DEPARTMENT_MANAGER', 'EMPLOYEE',
                    'SALES_MANAGER', 'CLIENT'
                  )
                """, Integer.class);
        if (unsupportedRoles != null && unsupportedRoles > 0) {
            throw new IllegalStateException("Unsupported role values remain in users; migration was not applied");
        }

        jdbcTemplate.execute("""
                alter table public.users add constraint users_role_check check (
                    role in (
                        'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER',
                        'DEPARTMENT_MANAGER', 'EMPLOYEE',
                        'SALES_MANAGER', 'CLIENT'
                    )
                )
                """);

        jdbcTemplate.execute("""
                alter table public.role_permissions add constraint role_permissions_role_check check (
                    role in (
                        'PLATFORM_OWNER', 'ORG_ADMIN', 'PROJECT_MANAGER',
                        'DEPARTMENT_MANAGER', 'EMPLOYEE',
                        'SALES_MANAGER', 'CLIENT'
                    )
                )
                """);

        jdbcTemplate.execute("""
                alter table public.admin_licence_assignments
                add constraint admin_licence_assignments_licence_type_check check (
                    licence_type in (
                        'ORG_ADMIN', 'PROJECT_MANAGER', 'DEPARTMENT_MANAGER',
                        'EMPLOYEE', 'SALES_MANAGER', 'CLIENT'
                    )
                )
                """);
    }
}
