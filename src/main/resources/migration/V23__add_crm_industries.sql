CREATE TABLE IF NOT EXISTS crm_industries (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_crm_industries_org_lower_name
    ON crm_industries (organisation_id, lower(name));

ALTER TABLE crm_accounts ADD COLUMN IF NOT EXISTS industry_id BIGINT REFERENCES crm_industries(id);

INSERT INTO crm_industries (organisation_id, name, active)
SELECT organisation_id, trim(industry), TRUE
FROM crm_accounts
WHERE industry IS NOT NULL AND trim(industry) <> ''
GROUP BY organisation_id, lower(trim(industry)), trim(industry)
ON CONFLICT DO NOTHING;

UPDATE crm_accounts account
SET industry_id = industry.id
FROM crm_industries industry
WHERE account.industry_id IS NULL
  AND account.organisation_id = industry.organisation_id
  AND lower(trim(account.industry)) = lower(industry.name);
