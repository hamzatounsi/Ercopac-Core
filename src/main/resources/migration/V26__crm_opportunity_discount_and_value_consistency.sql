ALTER TABLE crm_opportunities
    ADD COLUMN IF NOT EXISTS discount NUMERIC(5, 2) NOT NULL DEFAULT 0;

UPDATE crm_opportunities
SET material_value = COALESCE(value, 0),
    services_value = 0
WHERE COALESCE(material_value, 0) = 0
  AND COALESCE(services_value, 0) = 0
  AND COALESCE(value, 0) <> 0;

UPDATE crm_opportunities
SET value = COALESCE(material_value, 0) + COALESCE(services_value, 0),
    discount = COALESCE(discount, 0);

ALTER TABLE crm_opportunities
    DROP CONSTRAINT IF EXISTS chk_crm_opportunity_discount;
ALTER TABLE crm_opportunities
    ADD CONSTRAINT chk_crm_opportunity_discount CHECK (discount >= 0 AND discount <= 100);
