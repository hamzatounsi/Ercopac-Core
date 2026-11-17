-- Ticketing: configurable categories, custom statuses, contact person, ticket deletion.
-- Reference script (Flyway is not on the classpath): Hibernate ddl-auto=update creates the tables/columns and
-- TicketingSchemaCompatibilityMigration drops the category CHECK at startup. Safe to run manually, idempotent,
-- never touches existing values.

alter table if exists tickets drop constraint if exists tickets_category_check;

alter table if exists tickets add column if not exists contact_name varchar(150);
alter table if exists tickets add column if not exists contact_email varchar(180);
alter table if exists tickets add column if not exists contact_phone varchar(60);
alter table if exists tickets add column if not exists custom_status_id bigint;

create table if not exists ticket_category_settings (
    id bigserial primary key,
    organisation_id bigint not null references organisations(id),
    code varchar(32) not null,
    label varchar(80) not null,
    system boolean not null default false,
    active boolean not null default true,
    sort_order integer not null default 0,
    constraint uk_ticket_category_setting unique (organisation_id, code)
);

create table if not exists ticket_custom_statuses (
    id bigserial primary key,
    organisation_id bigint not null references organisations(id),
    code varchar(40) not null,
    label varchar(60) not null,
    description varchar(300),
    color varchar(20) not null,
    base_status varchar(20) not null,
    constraint uk_ticket_custom_status unique (organisation_id, code)
);

do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'fk_ticket_custom_status') then
    alter table tickets add constraint fk_ticket_custom_status foreign key (custom_status_id) references ticket_custom_statuses(id);
  end if;
end $$;

-- CRM: comment notification preference (default ON)
alter table if exists crm_notification_preferences add column if not exists comment_notifications boolean not null default true;
