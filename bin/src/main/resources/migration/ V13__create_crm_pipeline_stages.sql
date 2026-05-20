-- ══════════════════════════════════════════════════════════════
-- V13__create_crm_pipeline_stages.sql
-- Path: src/main/resources/db/migration/V13__create_crm_pipeline_stages.sql
--
-- Configurable pipeline stages per organisation.
-- Seeded with 5 defaults on first org setup.
-- ══════════════════════════════════════════════════════════════

CREATE TABLE crm_pipeline_stages (
    id              BIGSERIAL       PRIMARY KEY,
    organisation_id BIGINT          NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    name            VARCHAR(100)    NOT NULL,
    color           VARCHAR(20)     NOT NULL DEFAULT '#64748b',
    display_order   INT             NOT NULL DEFAULT 0,
    is_won          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_lost         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_stage_org_name UNIQUE (organisation_id, name)
);

CREATE INDEX idx_crm_stage_org ON crm_pipeline_stages(organisation_id);

-- ── Default stages will be seeded by the backend service
-- on first access per organisation (not here — avoids org_id hardcoding)
-- Defaults: Make presentation → Proposal/Quote → Negotiation/Revision
--           → Closed won (is_won=true) → Closed lost (is_lost=true)