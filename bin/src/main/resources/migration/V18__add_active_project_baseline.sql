-- Persist the selected baseline separately from the current task schedule.
ALTER TABLE projects ADD COLUMN IF NOT EXISTS active_baseline_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_projects_active_baseline ON projects(active_baseline_id);
