-- Ticketing links with Projectum: customer / project saved on tickets (linked or manual), manual customer projects,
-- equipment project and the TICKETING permission module.
-- Reference only: the schema is applied by Hibernate (spring.jpa.hibernate.ddl-auto=update); Flyway is not on the classpath.
-- The backfill and constraint changes run at startup in TicketingSchemaCompatibilityMigration.

-- Ticket customer / project as saved: copied from Projectum when customer_id / project_id is set, typed by hand otherwise
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS customer_name VARCHAR(180);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS project_name VARCHAR(250);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS project_number VARCHAR(60);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS project_year INTEGER;

UPDATE tickets t SET customer_name = c.name FROM customers c WHERE t.customer_id = c.id AND t.customer_name IS NULL;
UPDATE tickets t
SET project_name = p.name,
    project_number = COALESCE(t.project_number, p.code),
    project_year = COALESCE(t.project_year, CAST(EXTRACT(YEAR FROM p.planned_start) AS INTEGER))
FROM projects p
WHERE t.project_id = p.id AND t.project_name IS NULL;

-- Customer projects entered by hand in the Projects tab (not tracked in Projectum)
CREATE TABLE IF NOT EXISTS ticket_customer_projects (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    name VARCHAR(250) NOT NULL,
    project_number VARCHAR(60),
    project_year INTEGER
);
CREATE INDEX IF NOT EXISTS idx_ticket_customer_project_customer ON ticket_customer_projects (organisation_id, customer_id);

-- Equipment project: a Projectum project or a manual one, never both
ALTER TABLE ticket_customer_equipment ADD COLUMN IF NOT EXISTS project_id BIGINT REFERENCES projects(id);
ALTER TABLE ticket_customer_equipment ADD COLUMN IF NOT EXISTS customer_project_id BIGINT REFERENCES ticket_customer_projects(id);
ALTER TABLE ticket_customer_equipment DROP CONSTRAINT IF EXISTS ck_ticket_equipment_one_project;
ALTER TABLE ticket_customer_equipment ADD CONSTRAINT ck_ticket_equipment_one_project CHECK (project_id IS NULL OR customer_project_id IS NULL);

-- New PermissionModule value TICKETING: drop the CHECK constraint Hibernate generated for the old enum values
ALTER TABLE role_permissions DROP CONSTRAINT IF EXISTS role_permissions_module_check;
