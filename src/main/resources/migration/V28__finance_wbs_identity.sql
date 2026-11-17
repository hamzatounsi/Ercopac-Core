ALTER TABLE finance_entries ADD COLUMN IF NOT EXISTS parent_id BIGINT;
ALTER TABLE finance_entries ADD COLUMN IF NOT EXISTS template_row_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_finance_entries_project_parent ON finance_entries(project_id, parent_id);
CREATE INDEX IF NOT EXISTS idx_finance_entries_project_template ON finance_entries(project_id, template_row_id);
