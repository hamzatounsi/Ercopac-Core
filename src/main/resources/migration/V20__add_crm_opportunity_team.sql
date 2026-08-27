CREATE TABLE IF NOT EXISTS crm_opportunity_team (
    opportunity_id BIGINT NOT NULL REFERENCES crm_opportunities(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (opportunity_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_crm_opp_team_user ON crm_opportunity_team(user_id);
