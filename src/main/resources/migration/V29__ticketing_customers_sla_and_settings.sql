-- Ticketing: customer context, L2 technician, problem statement, settings and customer records.
-- Reference only: the schema is applied by Hibernate (spring.jpa.hibernate.ddl-auto=update); Flyway is not on the classpath.

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES customers(id);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS project_id BIGINT REFERENCES projects(id);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS equipment_id BIGINT;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS l2_technician_id BIGINT REFERENCES users(id);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS rca_problem VARCHAR(4000);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS rca_cause VARCHAR(4000);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS rca_solution VARCHAR(4000);

-- Which ticket field an activity is about (assignee, l2Technician, context, problemStatement, category, origin)
ALTER TABLE ticket_activities ADD COLUMN IF NOT EXISTS field VARCHAR(40);

CREATE TABLE IF NOT EXISTS ticket_customer_sites (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    name VARCHAR(160) NOT NULL,
    address VARCHAR(300)
);
CREATE INDEX IF NOT EXISTS idx_ticket_site_customer ON ticket_customer_sites (organisation_id, customer_id);

CREATE TABLE IF NOT EXISTS ticket_customer_contacts (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    full_name VARCHAR(150) NOT NULL,
    job_title VARCHAR(120),
    email VARCHAR(180),
    phone VARCHAR(60),
    language VARCHAR(8),
    portal_user_id BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_ticket_contact_customer ON ticket_customer_contacts (organisation_id, customer_id);

CREATE TABLE IF NOT EXISTS ticket_customer_equipment (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    equipment_type_id BIGINT REFERENCES crm_equipment_types(id),
    site_id BIGINT REFERENCES ticket_customer_sites(id),
    name VARCHAR(160) NOT NULL,
    serial_number VARCHAR(100),
    note VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS idx_ticket_equipment_customer ON ticket_customer_equipment (organisation_id, customer_id);
ALTER TABLE tickets ADD CONSTRAINT fk_ticket_equipment FOREIGN KEY (equipment_id) REFERENCES ticket_customer_equipment(id);

CREATE TABLE IF NOT EXISTS ticket_agent_profiles (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    l2_technician BOOLEAN NOT NULL DEFAULT FALSE,
    specialization VARCHAR(120),
    preferred_language VARCHAR(8),
    CONSTRAINT uk_ticket_agent_profile_user UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS ticket_status_settings (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    status VARCHAR(20) NOT NULL,
    label VARCHAR(60) NOT NULL,
    description VARCHAR(300),
    color VARCHAR(20) NOT NULL,
    CONSTRAINT uk_ticket_status_setting UNIQUE (organisation_id, status)
);

CREATE TABLE IF NOT EXISTS ticket_priority_settings (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    priority VARCHAR(16) NOT NULL,
    label VARCHAR(60) NOT NULL,
    first_response_minutes INTEGER NOT NULL,
    resolution_hours INTEGER NOT NULL,
    CONSTRAINT uk_ticket_priority_setting UNIQUE (organisation_id, priority)
);
