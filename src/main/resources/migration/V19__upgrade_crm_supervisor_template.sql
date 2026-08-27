-- Supervisor-template CRM upgrade. Existing string company fields are retained for
-- compatibility and backfilled into tenant-owned Account foreign keys.

-- Add nullable first, backfill existing tenant stages, then enforce the
-- invariant.  PostgreSQL rejects a direct NOT NULL addition when an old stage
-- table already contains rows.
ALTER TABLE crm_pipeline_stages ADD COLUMN IF NOT EXISTS probability INT;
UPDATE crm_pipeline_stages SET probability = CASE lower(name)
    WHEN 'make presentation' THEN 10
    WHEN 'problem setting' THEN 20
    WHEN 'problem solving' THEN 40
    WHEN 'proposal/quote' THEN 65
    WHEN 'negotiation/revision' THEN 85
    WHEN 'closed won' THEN 100
    ELSE 0
END
WHERE probability IS NULL;
ALTER TABLE crm_pipeline_stages ALTER COLUMN probability SET DEFAULT 0;
ALTER TABLE crm_pipeline_stages ALTER COLUMN probability SET NOT NULL;
ALTER TABLE crm_pipeline_stages DROP CONSTRAINT IF EXISTS chk_crm_stage_probability;
ALTER TABLE crm_pipeline_stages ADD CONSTRAINT chk_crm_stage_probability CHECK (probability BETWEEN 0 AND 100);

CREATE TABLE IF NOT EXISTS crm_accounts (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    name VARCHAR(180) NOT NULL,
    industry VARCHAR(120), country VARCHAR(100), city VARCHAR(100), address VARCHAR(300),
    phone VARCHAR(40), website VARCHAR(250), employees VARCHAR(50),
    annual_revenue DECIMAL(18,2), currency VARCHAR(10) NOT NULL DEFAULT 'EUR',
    owner_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    notes VARCHAR(4000), active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_crm_account_org_name UNIQUE (organisation_id, name)
);
CREATE INDEX IF NOT EXISTS idx_crm_account_org ON crm_accounts(organisation_id);
CREATE INDEX IF NOT EXISTS idx_crm_account_owner ON crm_accounts(owner_id);

INSERT INTO crm_accounts (organisation_id, name)
SELECT DISTINCT organisation_id, TRIM(company) FROM crm_leads
WHERE company IS NOT NULL AND TRIM(company) <> ''
ON CONFLICT (organisation_id, name) DO NOTHING;
INSERT INTO crm_accounts (organisation_id, name)
SELECT DISTINCT organisation_id, TRIM(account_name) FROM crm_opportunities
WHERE account_name IS NOT NULL AND TRIM(account_name) <> ''
ON CONFLICT (organisation_id, name) DO NOTHING;

ALTER TABLE crm_leads ADD COLUMN IF NOT EXISTS account_id BIGINT REFERENCES crm_accounts(id) ON DELETE RESTRICT;
ALTER TABLE crm_leads ADD COLUMN IF NOT EXISTS job_title VARCHAR(150);
ALTER TABLE crm_leads ADD COLUMN IF NOT EXISTS mobile VARCHAR(40);
ALTER TABLE crm_leads ADD COLUMN IF NOT EXISTS rating VARCHAR(40);
UPDATE crm_leads l SET account_id = a.id FROM crm_accounts a
WHERE l.account_id IS NULL AND a.organisation_id = l.organisation_id AND LOWER(a.name) = LOWER(TRIM(l.company));
CREATE INDEX IF NOT EXISTS idx_crm_lead_account ON crm_leads(organisation_id, account_id);

CREATE TABLE IF NOT EXISTS crm_supply_categories (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL, display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_crm_supply_category_org_name UNIQUE (organisation_id, name)
);
CREATE INDEX IF NOT EXISTS idx_crm_supply_category_org ON crm_supply_categories(organisation_id, display_order);

ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS account_id BIGINT REFERENCES crm_accounts(id) ON DELETE RESTRICT;
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS supply_category_id BIGINT REFERENCES crm_supply_categories(id) ON DELETE SET NULL;
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS opportunity_type VARCHAR(20);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS pipeline VARCHAR(20);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS quote_number VARCHAR(80);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS quote_requested_date DATE;
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS quote_submitted_date DATE;
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS shipment_date DATE;
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS next_step VARCHAR(500);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS description VARCHAR(8000);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS material_value DECIMAL(15,2);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS services_value DECIMAL(15,2);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS ercopac_material_value DECIMAL(15,2);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS third_party_material_value DECIMAL(15,2);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS ercopac_resale_value DECIMAL(15,2);
ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS resale_value DECIMAL(15,2);
UPDATE crm_opportunities o SET account_id = a.id FROM crm_accounts a
WHERE o.account_id IS NULL AND a.organisation_id = o.organisation_id AND LOWER(a.name) = LOWER(TRIM(o.account_name));
UPDATE crm_opportunities SET description = notes WHERE description IS NULL AND notes IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_crm_opp_account ON crm_opportunities(organisation_id, account_id);
CREATE INDEX IF NOT EXISTS idx_crm_opp_supply_category ON crm_opportunities(organisation_id, supply_category_id);

CREATE TABLE IF NOT EXISTS crm_opportunity_notes (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    content VARCHAR(8000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_note_tenant ON crm_opportunity_notes(organisation_id, opportunity_id, created_at);

CREATE TABLE IF NOT EXISTS crm_opportunity_attachments (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL, stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(120) NOT NULL, file_size BIGINT NOT NULL, storage_path VARCHAR(500) NOT NULL,
    uploaded_by_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_attachment_tenant ON crm_opportunity_attachments(organisation_id, opportunity_id);

CREATE TABLE IF NOT EXISTS crm_opportunity_history (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    field_name VARCHAR(80) NOT NULL, old_value VARCHAR(2000), new_value VARCHAR(2000),
    changed_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_history_tenant ON crm_opportunity_history(organisation_id, opportunity_id, created_at DESC);

CREATE TABLE IF NOT EXISTS crm_opportunity_stage_history (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    stage_id BIGINT REFERENCES crm_pipeline_stages(id) ON DELETE SET NULL,
    stage_name VARCHAR(100) NOT NULL, probability INT NOT NULL,
    closing_date DATE, modified_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    entered_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_stage_history_tenant ON crm_opportunity_stage_history(organisation_id, opportunity_id, entered_at DESC);

CREATE TABLE IF NOT EXISTS crm_sales_targets (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_year INT NOT NULL, amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'EUR', updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_crm_target_org_user_year UNIQUE (organisation_id, user_id, target_year)
);
CREATE INDEX IF NOT EXISTS idx_crm_target_tenant ON crm_sales_targets(organisation_id, target_year);
