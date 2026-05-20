-- ══════════════════════════════════════════════════════════════
-- V14__create_crm_leads.sql
-- Path: src/main/resources/db/migration/V14__create_crm_leads.sql
--
-- CRM Leads — contacts that may become opportunities.
-- Source: Referral, Trade fair, Agent, Customer, Partner, Other
-- Status: NOT_CONTACTED, CONTACTED, CONTACT_IN_FUTURE, CONVERTED
-- ══════════════════════════════════════════════════════════════

CREATE TABLE crm_leads (
    id              BIGSERIAL       PRIMARY KEY,
    organisation_id BIGINT          NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,

    -- Contact info
    full_name       VARCHAR(150)    NOT NULL,
    company         VARCHAR(150),
    email           VARCHAR(180),
    phone           VARCHAR(40),

    -- Lead metadata
    source          VARCHAR(40)     NOT NULL DEFAULT 'OTHER',
    -- Values: REFERRAL, TRADE_FAIR, AGENT, CUSTOMER, PARTNER, OTHER

    status          VARCHAR(40)     NOT NULL DEFAULT 'NOT_CONTACTED',
    -- Values: NOT_CONTACTED, CONTACTED, CONTACT_IN_FUTURE, CONVERTED

    -- Owner (salesperson) — AppUser in same org
    owner_id        BIGINT          REFERENCES users(id) ON DELETE SET NULL,

    -- Conversion tracking
    converted       BOOLEAN         NOT NULL DEFAULT FALSE,
    converted_at    TIMESTAMP,

    notes           VARCHAR(2000),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_lead_org          ON crm_leads(organisation_id);
CREATE INDEX idx_crm_lead_owner        ON crm_leads(owner_id);
CREATE INDEX idx_crm_lead_status       ON crm_leads(organisation_id, status);
CREATE INDEX idx_crm_lead_source       ON crm_leads(organisation_id, source);