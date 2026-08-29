CREATE TABLE IF NOT EXISTS crm_notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL REFERENCES organisations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    stage_change_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    closing_date_reminders BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_crm_notification_preferences_org_user UNIQUE (organisation_id, user_id)
);
