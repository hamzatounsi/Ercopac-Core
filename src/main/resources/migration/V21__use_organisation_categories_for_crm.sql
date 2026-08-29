-- CRM now uses organisation-owned project_categories as its only category source.
-- This migration is safe to run on PostgreSQL installations that execute the
-- repository migration scripts. Hibernate's update mode also adds the column;
-- CrmService performs the same idempotent legacy-data bridge at runtime.

ALTER TABLE crm_opportunities
    ADD COLUMN IF NOT EXISTS project_category_id BIGINT;

ALTER TABLE crm_opportunities
    DROP CONSTRAINT IF EXISTS fk_crm_opportunities_project_category;

ALTER TABLE crm_opportunities
    ADD CONSTRAINT fk_crm_opportunities_project_category
    FOREIGN KEY (project_category_id) REFERENCES project_categories(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_crm_opp_project_category
    ON crm_opportunities(organisation_id, project_category_id);

-- Existing categories with exactly matching names can be linked immediately.
UPDATE crm_opportunities opportunity
SET project_category_id = category.id
FROM crm_supply_categories legacy
JOIN project_categories category
  ON category.organisation_id = legacy.organisation_id
 AND lower(trim(category.name)) = lower(trim(legacy.name))
WHERE opportunity.supply_category_id = legacy.id
  AND opportunity.project_category_id IS NULL
  AND opportunity.organisation_id = legacy.organisation_id;
