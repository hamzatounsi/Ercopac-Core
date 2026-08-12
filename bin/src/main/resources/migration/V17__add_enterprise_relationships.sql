-- Additive relationship columns and indexes for enterprise-grade JPA navigation.
-- The application currently uses Hibernate ddl-auto=update; this file documents
-- the equivalent database changes for environments that apply SQL migrations.

ALTER TABLE users ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE departments ADD COLUMN IF NOT EXISTS manager_id BIGINT;
ALTER TABLE project_tasks ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE department_holidays ADD COLUMN IF NOT EXISTS department_id BIGINT;

ALTER TABLE action_items ADD COLUMN IF NOT EXISTS owner_id BIGINT;
ALTER TABLE action_items ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE action_items ADD COLUMN IF NOT EXISTS risk_id BIGINT;
ALTER TABLE action_items ADD COLUMN IF NOT EXISTS change_request_id BIGINT;
ALTER TABLE action_items ADD COLUMN IF NOT EXISTS project_task_id BIGINT;
ALTER TABLE action_assignees ADD COLUMN IF NOT EXISTS assignee_user_id BIGINT;
ALTER TABLE action_comments ADD COLUMN IF NOT EXISTS author_user_id BIGINT;

ALTER TABLE change_requests ADD COLUMN IF NOT EXISTS requester_id BIGINT;
ALTER TABLE change_requests ADD COLUMN IF NOT EXISTS approver_id BIGINT;
ALTER TABLE change_requests ADD COLUMN IF NOT EXISTS affected_task_id BIGINT;
ALTER TABLE change_request_history ADD COLUMN IF NOT EXISTS performed_by_user_id BIGINT;

ALTER TABLE crm_opportunities ADD COLUMN IF NOT EXISTS project_id BIGINT;

ALTER TABLE finance_entries ADD COLUMN IF NOT EXISTS owner_id BIGINT;
ALTER TABLE finance_entries ADD COLUMN IF NOT EXISTS project_task_id BIGINT;
ALTER TABLE forecast_entries ADD COLUMN IF NOT EXISTS finance_entry_id BIGINT;

UPDATE users u
SET department_id = d.id
FROM departments d
WHERE u.department_id IS NULL
  AND u.organisation_id = d.organisation_id
  AND u.department_code = d.code;

UPDATE project_tasks t
SET department_id = d.id
FROM departments d
WHERE t.department_id IS NULL
  AND t.organisation_id = d.organisation_id
  AND t.department_code = d.code;

DELETE FROM task_resource_assignments a
WHERE NOT EXISTS (
    SELECT 1 FROM projects p WHERE p.id = a.project_id
)
   OR NOT EXISTS (
    SELECT 1 FROM project_tasks t WHERE t.id = a.task_id
);

DELETE FROM task_dependencies d
WHERE NOT EXISTS (
    SELECT 1 FROM projects p WHERE p.id = d.project_id
)
   OR NOT EXISTS (
    SELECT 1 FROM project_tasks t WHERE t.id = d.predecessor_task_id
)
   OR NOT EXISTS (
    SELECT 1 FROM project_tasks t WHERE t.id = d.successor_task_id
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_id) REFERENCES users(id);
ALTER TABLE project_tasks
    ADD CONSTRAINT fk_project_tasks_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE department_holidays
    ADD CONSTRAINT fk_department_holidays_department
    FOREIGN KEY (department_id) REFERENCES departments(id);

ALTER TABLE action_items
    ADD CONSTRAINT fk_action_items_owner
    FOREIGN KEY (owner_id) REFERENCES users(id);
ALTER TABLE action_items
    ADD CONSTRAINT fk_action_items_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE action_items
    ADD CONSTRAINT fk_action_items_risk
    FOREIGN KEY (risk_id) REFERENCES risk_items(id);
ALTER TABLE action_items
    ADD CONSTRAINT fk_action_items_change_request
    FOREIGN KEY (change_request_id) REFERENCES change_requests(id);
ALTER TABLE action_items
    ADD CONSTRAINT fk_action_items_project_task
    FOREIGN KEY (project_task_id) REFERENCES project_tasks(id);
ALTER TABLE action_assignees
    ADD CONSTRAINT fk_action_assignees_user
    FOREIGN KEY (assignee_user_id) REFERENCES users(id);
ALTER TABLE action_comments
    ADD CONSTRAINT fk_action_comments_author
    FOREIGN KEY (author_user_id) REFERENCES users(id);

ALTER TABLE change_requests
    ADD CONSTRAINT fk_change_requests_requester
    FOREIGN KEY (requester_id) REFERENCES users(id);
ALTER TABLE change_requests
    ADD CONSTRAINT fk_change_requests_approver
    FOREIGN KEY (approver_id) REFERENCES users(id);
ALTER TABLE change_requests
    ADD CONSTRAINT fk_change_requests_affected_task
    FOREIGN KEY (affected_task_id) REFERENCES project_tasks(id);
ALTER TABLE change_request_history
    ADD CONSTRAINT fk_change_request_history_user
    FOREIGN KEY (performed_by_user_id) REFERENCES users(id);

ALTER TABLE crm_opportunities
    ADD CONSTRAINT fk_crm_opportunities_project
    FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE finance_entries
    ADD CONSTRAINT fk_finance_entries_owner
    FOREIGN KEY (owner_id) REFERENCES users(id);
ALTER TABLE finance_entries
    ADD CONSTRAINT fk_finance_entries_project_task
    FOREIGN KEY (project_task_id) REFERENCES project_tasks(id);
ALTER TABLE forecast_entries
    ADD CONSTRAINT fk_forecast_entries_finance_entry
    FOREIGN KEY (finance_entry_id) REFERENCES finance_entries(id);

CREATE INDEX IF NOT EXISTS idx_users_organisation ON users(organisation_id);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_departments_organisation ON departments(organisation_id);
CREATE INDEX IF NOT EXISTS idx_departments_manager ON departments(manager_id);
CREATE INDEX IF NOT EXISTS idx_projects_organisation ON projects(organisation_id);
CREATE INDEX IF NOT EXISTS idx_project_tasks_organisation ON project_tasks(organisation_id);
CREATE INDEX IF NOT EXISTS idx_project_tasks_project ON project_tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_project_tasks_assigned_user ON project_tasks(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_project_tasks_department ON project_tasks(department_id);
CREATE INDEX IF NOT EXISTS idx_project_tasks_parent ON project_tasks(parent_id);

CREATE INDEX IF NOT EXISTS idx_task_assignments_task ON task_resource_assignments(task_id);
CREATE INDEX IF NOT EXISTS idx_task_assignments_project ON task_resource_assignments(project_id);
CREATE INDEX IF NOT EXISTS idx_task_assignments_user ON task_resource_assignments(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_project ON task_dependencies(project_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_predecessor ON task_dependencies(predecessor_task_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_successor ON task_dependencies(successor_task_id);

CREATE INDEX IF NOT EXISTS idx_department_holidays_organisation ON department_holidays(organisation_id);
CREATE INDEX IF NOT EXISTS idx_department_holidays_department ON department_holidays(department_id);
CREATE INDEX IF NOT EXISTS idx_department_holidays_member ON department_holidays(member_id);

CREATE INDEX IF NOT EXISTS idx_finance_entries_project ON finance_entries(project_id);
CREATE INDEX IF NOT EXISTS idx_finance_entries_organisation ON finance_entries(organisation_id);
CREATE INDEX IF NOT EXISTS idx_finance_entries_owner ON finance_entries(owner_id);
CREATE INDEX IF NOT EXISTS idx_finance_entries_project_task ON finance_entries(project_task_id);
CREATE INDEX IF NOT EXISTS idx_forecast_entries_project ON forecast_entries(project_id);
CREATE INDEX IF NOT EXISTS idx_forecast_entries_organisation ON forecast_entries(organisation_id);
CREATE INDEX IF NOT EXISTS idx_forecast_entries_finance_entry ON forecast_entries(finance_entry_id);

CREATE INDEX IF NOT EXISTS idx_risk_items_project ON risk_items(project_id);
CREATE INDEX IF NOT EXISTS idx_risk_items_organisation ON risk_items(organisation_id);
CREATE INDEX IF NOT EXISTS idx_risk_items_owner ON risk_items(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_change_requests_project ON change_requests(project_id);
CREATE INDEX IF NOT EXISTS idx_change_requests_organisation ON change_requests(organisation_id);
CREATE INDEX IF NOT EXISTS idx_change_requests_requester ON change_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_change_requests_approver ON change_requests(approver_id);
CREATE INDEX IF NOT EXISTS idx_change_requests_task ON change_requests(affected_task_id);

CREATE INDEX IF NOT EXISTS idx_action_items_project ON action_items(project_id);
CREATE INDEX IF NOT EXISTS idx_action_items_organisation ON action_items(organisation_id);
CREATE INDEX IF NOT EXISTS idx_action_items_owner ON action_items(owner_id);
CREATE INDEX IF NOT EXISTS idx_action_items_department ON action_items(department_id);
CREATE INDEX IF NOT EXISTS idx_action_items_risk ON action_items(risk_id);
CREATE INDEX IF NOT EXISTS idx_action_items_change_request ON action_items(change_request_id);
CREATE INDEX IF NOT EXISTS idx_action_items_project_task ON action_items(project_task_id);
CREATE INDEX IF NOT EXISTS idx_action_assignees_user ON action_assignees(assignee_user_id);

CREATE INDEX IF NOT EXISTS idx_crm_opportunities_organisation ON crm_opportunities(organisation_id);
CREATE INDEX IF NOT EXISTS idx_crm_opportunities_owner ON crm_opportunities(owner_id);
CREATE INDEX IF NOT EXISTS idx_crm_opportunities_project ON crm_opportunities(project_id);
CREATE INDEX IF NOT EXISTS idx_crm_leads_organisation ON crm_leads(organisation_id);
CREATE INDEX IF NOT EXISTS idx_crm_leads_owner ON crm_leads(owner_id);
CREATE INDEX IF NOT EXISTS idx_customers_organisation ON customers(organisation_id);
