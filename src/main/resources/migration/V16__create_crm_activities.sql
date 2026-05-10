-- ══════════════════════════════════════════════════════════════
-- V16__create_crm_activities.sql
-- Path: src/main/resources/db/migration/V16__create_crm_activities.sql
--
-- CRM Activity feed — tracks all actions on leads and opportunities.
-- Powers the "Recent activity" section on the dashboard.
-- ══════════════════════════════════════════════════════════════

CREATE TABLE crm_activities (
    id              BIGSERIAL       PRIMARY KEY,
    organisation_id BIGINT          NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,

    -- Who did it
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,

    -- What kind of activity
    activity_type   VARCHAR(50)     NOT NULL,
    -- Values: EMAIL_SENT, STAGE_UPDATED, OFFER_ATTACHED,
    --         LEAD_CREATED, OPPORTUNITY_CREATED, NOTE_ADDED,
    --         LEAD_CONVERTED, DEAL_WON, DEAL_LOST

    -- Human readable description
    description     VARCHAR(500)    NOT NULL,

    -- Linked entity (one of these will be set)
    lead_id         BIGINT          REFERENCES crm_leads(id) ON DELETE CASCADE,
    opportunity_id  BIGINT          REFERENCES crm_opportunities(id) ON DELETE CASCADE,

    -- Extra metadata (JSON-like string, e.g. old_stage → new_stage)
    metadata        VARCHAR(500),

    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_activity_org     ON crm_activities(organisation_id);
CREATE INDEX idx_crm_activity_user    ON crm_activities(user_id);
CREATE INDEX idx_crm_activity_lead    ON crm_activities(lead_id);
CREATE INDEX idx_crm_activity_opp     ON crm_activities(opportunity_id);
CREATE INDEX idx_crm_activity_date    ON crm_activities(organisation_id, created_at DESC);