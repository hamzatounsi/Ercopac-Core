-- ══════════════════════════════════════════════════════════════
-- V15__create_crm_opportunities.sql
-- Path: src/main/resources/db/migration/V15__create_crm_opportunities.sql
--
-- CRM Opportunities — qualified leads with value and pipeline stage.
-- Account is text for now (no FK to customers yet).
-- ══════════════════════════════════════════════════════════════

CREATE TABLE crm_opportunities (
    id              BIGSERIAL       PRIMARY KEY,
    organisation_id BIGINT          NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,

    -- Opportunity info
    name            VARCHAR(200)    NOT NULL,
    account_name    VARCHAR(150),   -- text for now, FK to customers later

    -- Pipeline
    stage_id        BIGINT          REFERENCES crm_pipeline_stages(id) ON DELETE SET NULL,

    -- Financials
    value           DECIMAL(15,2),
    currency        VARCHAR(10)     NOT NULL DEFAULT 'EUR',
    probability     INT             NOT NULL DEFAULT 0
                    CHECK (probability >= 0 AND probability <= 100),
    closing_date    DATE,

    -- Owner (salesperson)
    owner_id        BIGINT          REFERENCES users(id) ON DELETE SET NULL,

    -- Link back to originating lead (nullable — can create opp without lead)
    lead_id         BIGINT          REFERENCES crm_leads(id) ON DELETE SET NULL,

    -- Win/loss (derived from stage but stored for fast queries)
    is_won          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_lost         BOOLEAN         NOT NULL DEFAULT FALSE,

    notes           VARCHAR(2000),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_opp_org      ON crm_opportunities(organisation_id);
CREATE INDEX idx_crm_opp_stage    ON crm_opportunities(stage_id);
CREATE INDEX idx_crm_opp_owner    ON crm_opportunities(owner_id);
CREATE INDEX idx_crm_opp_lead     ON crm_opportunities(lead_id);
CREATE INDEX idx_crm_opp_closing  ON crm_opportunities(organisation_id, closing_date);