CREATE TABLE IF NOT EXISTS crm_equipment_types (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    code VARCHAR(40) NOT NULL, name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_crm_equipment_type_org_code UNIQUE (organisation_id, code),
    CONSTRAINT uq_crm_equipment_type_org_name UNIQUE (organisation_id, name)
);
CREATE INDEX IF NOT EXISTS idx_crm_equipment_type_org_active ON crm_equipment_types(organisation_id, active);

CREATE TABLE IF NOT EXISTS crm_opportunity_equipment (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    equipment_type_id BIGINT NOT NULL REFERENCES crm_equipment_types(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL,
    CONSTRAINT chk_crm_opportunity_equipment_quantity CHECK (quantity > 0),
    CONSTRAINT uq_crm_opportunity_equipment_type UNIQUE (opportunity_id, equipment_type_id)
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_equipment_org ON crm_opportunity_equipment(organisation_id, opportunity_id);

CREATE TABLE IF NOT EXISTS crm_report_schedules (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    report_type VARCHAR(60) NOT NULL, recipients VARCHAR(2000) NOT NULL,
    frequency VARCHAR(20) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
    last_sent_at TIMESTAMP, next_run_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_crm_report_schedule_due ON crm_report_schedules(active, next_run_at);
