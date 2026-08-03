-- ERCOPAC Projectum production-compatible demo seed for PostgreSQL.
--
-- This script intentionally does not insert generated identity IDs.  Foreign
-- keys are resolved from stable demo natural keys (organisation code, project
-- code, user email, department code and task WBS code).
--
-- Known demo login password for every seeded user: password
-- BCrypt hash above is a valid Spring Security BCrypt hash for that password.

BEGIN;

-- Platform / organisation management
INSERT INTO organisations (
    name, code, country, domain, status, plan, user_limit,
    org_admin_licence_limit, general_manager_licence_limit,
    department_manager_licence_limit, employee_licence_limit,
    monthly_revenue, health_score, created_at, billing_email, vat_number,
    payment_method, force2fa_admins, force2fa_specialists,
    force2fa_operators, default2fa_method, session_timeout,
    max_failed_logins, password_min_length, password_expiry, internal_notes,
    flag_at_risk, flag_payment_overdue, flag_upsell_opportunity,
    flag_vip_priority, flag_pilot_features, flag_under_review, active
) VALUES
(
    'Northstar Industrial Automation GmbH', 'NSIA', 'Germany', 'northstar-ia.example',
    'ACTIVE', 'ENTERPRISE', 60, 3, 4, 8, 45, 2490.00, 92,
    TIMESTAMP '2025-11-04 09:00:00', 'billing@northstar-ia.example', 'DE318765492',
    'SEPA_DIRECT_DEBIT', 'ENABLED', 'ENABLED', 'OPTIONAL', 'AUTHENTICATOR_APP', '4_HOURS',
    5, 12, 'NEVER', 'Enterprise demonstration tenant for industrial automation delivery.',
    false, false, true, true, true, false, true
),
(
    'Veltis Field Services Ltd', 'VELTIS', 'United Kingdom', 'veltis.example',
    'TRIAL', 'GROWTH', 25, 2, 2, 4, 17, 990.00, 78,
    TIMESTAMP '2026-05-20 09:00:00', 'finance@veltis.example', 'GB492381760',
    'CARD', 'ENABLED', 'OPTIONAL', 'OPTIONAL', 'AUTHENTICATOR_APP', '2_HOURS',
    5, 12, '180_DAYS', 'Trial tenant with an active field-services opportunity pipeline.',
    false, false, true, false, false, false, true
);

-- Users required before departments so department manager relationships can be inserted.
INSERT INTO users (
    full_name, email, password_hash, role, organisation_id, employee_code,
    department_code, job_title, seniority, hours_per_day, days_per_week,
    workdays, color, internal_user, default_rate, rate_type, currency, notes, active
) VALUES
('Platform Owner', 'owner@ercopac.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PLATFORM_OWNER', NULL,
 'PLT-001', 'PLATFORM', 'Platform Owner', 'EXECUTIVE', 8, 5, 'MON-FRI', '#0f172a', true, 0.00, 'SALARY', 'EUR', 'Demo platform owner.', true),
('Marie Hoffmann', 'marie.hoffmann@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ORG_ADMIN',
 (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-ADM-001', 'ADMIN', 'Organisation Administrator', 'SENIOR', 8, 5, 'MON-FRI', '#7c3aed', true, 98.00, 'HOURLY', 'EUR', 'Tenant administrator and finance sponsor.', true),
('Daniel Keller', 'daniel.keller@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'GENERAL_MANAGER',
 (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-GM-001', 'PMO', 'General Manager', 'EXECUTIVE', 8, 5, 'MON-FRI', '#1d4ed8', true, 140.00, 'HOURLY', 'EUR', 'Portfolio executive and escalation owner.', true),
('Elena Fischer', 'elena.fischer@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DEPARTMENT_MANAGER',
 (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-ENG-001', 'ENG', 'Engineering Manager', 'SENIOR', 8, 5, 'MON-FRI', '#2563eb', true, 112.00, 'HOURLY', 'EUR', 'Manager for automation engineering.', true),
('Lukas Brandt', 'lukas.brandt@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DEPARTMENT_MANAGER',
 (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-OPS-001', 'OPS', 'Operations Manager', 'SENIOR', 8, 5, 'MON-FRI', '#059669', true, 108.00, 'HOURLY', 'EUR', 'Manager for procurement and site operations.', true),
('Sofia Roth', 'sofia.roth@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DEPARTMENT_MANAGER',
 (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-PMO-001', 'PMO', 'PMO Manager', 'SENIOR', 8, 5, 'MON-FRI', '#9333ea', true, 105.00, 'HOURLY', 'EUR', 'Manager for PMO and project controls.', true),
('Rita Collins', 'rita.collins@veltis.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ORG_ADMIN',
 (SELECT id FROM organisations WHERE code = 'VELTIS'), 'VF-ADM-001', 'OPS', 'Operations Director', 'SENIOR', 8, 5, 'MON-FRI', '#0f766e', true, 88.00, 'HOURLY', 'GBP', 'Trial tenant administrator.', true),
('Timo Weber', 'timo.weber@veltis.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'GENERAL_MANAGER',
 (SELECT id FROM organisations WHERE code = 'VELTIS'), 'VF-GM-001', 'OPS', 'Service Delivery Manager', 'SENIOR', 8, 5, 'MON-FRI', '#0369a1', true, 95.00, 'HOURLY', 'GBP', 'Leads the Veltis delivery portfolio.', true);

INSERT INTO departments (code, label, organisation_id, manager_id) VALUES
('ENG', 'Automation Engineering', (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example')),
('OPS', 'Operations & Procurement', (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example')),
('PMO', 'Project Management Office', (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example')),
('OPS', 'Field Operations', (SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'));

INSERT INTO resource_types (code, label, colour, department_id, default_rate, assignable, active, organisation_id) VALUES
('AUTOMATION_ENG', 'Automation Engineer', '#2563eb', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 96.00, true, true, (SELECT id FROM organisations WHERE code = 'NSIA')),
('PLC_DEVELOPER', 'PLC Developer', '#7c3aed', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 92.00, true, true, (SELECT id FROM organisations WHERE code = 'NSIA')),
('PROJECT_CONTROLLER', 'Project Controller', '#9333ea', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 84.00, true, true, (SELECT id FROM organisations WHERE code = 'NSIA')),
('SITE_TECHNICIAN', 'Site Technician', '#059669', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 78.00, true, true, (SELECT id FROM organisations WHERE code = 'NSIA')),
('FIELD_ENGINEER', 'Field Engineer', '#0f766e', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), 72.00, true, true, (SELECT id FROM organisations WHERE code = 'VELTIS'));

INSERT INTO resource_type_dept_map (resource_type_id, department_id, colour, default_rate, organisation_id) VALUES
((SELECT id FROM resource_types WHERE code = 'AUTOMATION_ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), '#2563eb', 96.00, (SELECT id FROM organisations WHERE code = 'NSIA')),
((SELECT id FROM resource_types WHERE code = 'PLC_DEVELOPER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), '#7c3aed', 92.00, (SELECT id FROM organisations WHERE code = 'NSIA')),
((SELECT id FROM resource_types WHERE code = 'PROJECT_CONTROLLER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), '#9333ea', 84.00, (SELECT id FROM organisations WHERE code = 'NSIA')),
((SELECT id FROM resource_types WHERE code = 'SITE_TECHNICIAN' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), '#059669', 78.00, (SELECT id FROM organisations WHERE code = 'NSIA')),
((SELECT id FROM resource_types WHERE code = 'FIELD_ENGINEER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), '#0f766e', 72.00, (SELECT id FROM organisations WHERE code = 'VELTIS'));

INSERT INTO users (
    full_name, email, password_hash, role, organisation_id, employee_code,
    department_code, department_id, job_title, resource_type_id, seniority,
    hours_per_day, days_per_week, workdays, color, internal_user, default_rate,
    rate_type, currency, notes, active
) VALUES
('Amir Benali', 'amir.benali@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-ENG-014', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'Senior Automation Engineer', (SELECT id FROM resource_types WHERE code = 'AUTOMATION_ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'SENIOR', 8, 5, 'MON-FRI', '#3b82f6', true, 96.00, 'HOURLY', 'EUR', 'Leads controls-hardware engineering.', true),
('Nina Vogel', 'nina.vogel@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-ENG-021', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'PLC Developer', (SELECT id FROM resource_types WHERE code = 'PLC_DEVELOPER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'MID', 8, 5, 'MON-FRI', '#8b5cf6', true, 92.00, 'HOURLY', 'EUR', 'Owns PLC configuration and FAT test scripts.', true),
('Jonas Meier', 'jonas.meier@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-PMO-008', 'PMO', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'Project Controller', (SELECT id FROM resource_types WHERE code = 'PROJECT_CONTROLLER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'MID', 8, 5, 'MON-FRI', '#a855f7', true, 84.00, 'HOURLY', 'EUR', 'Maintains cost reporting and forecasting.', true),
('Marta Klein', 'marta.klein@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code = 'NSIA'), 'NS-OPS-011', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'Site Technician', (SELECT id FROM resource_types WHERE code = 'SITE_TECHNICIAN' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'MID', 8, 5, 'MON-FRI', '#10b981', true, 78.00, 'HOURLY', 'EUR', 'Coordinates on-site acceptance activities.', true),
('Oliver Grant', 'oliver.grant@veltis.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code = 'VELTIS'), 'VF-OPS-004', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), 'Field Engineer', (SELECT id FROM resource_types WHERE code = 'FIELD_ENGINEER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), 'MID', 8, 5, 'MON-FRI', '#14b8a6', true, 72.00, 'HOURLY', 'GBP', 'Assigned to Delta Water remote monitoring rollout.', true);

-- Licences and permissions
INSERT INTO admin_licence_assignments (organisation_id, user_id, licence_type, created_at, updated_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'marie.hoffmann@northstar-ia.example'), 'ADMIN', TIMESTAMP '2025-11-04 09:15:00', TIMESTAMP '2025-11-04 09:15:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), 'PM', TIMESTAMP '2025-11-04 09:15:00', TIMESTAMP '2025-11-04 09:15:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), 'DEPT_MANAGER', TIMESTAMP '2025-11-04 09:15:00', TIMESTAMP '2025-11-04 09:15:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example'), 'DEPT_MANAGER', TIMESTAMP '2025-11-04 09:15:00', TIMESTAMP '2025-11-04 09:15:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), 'DEPT_MANAGER', TIMESTAMP '2025-11-04 09:15:00', TIMESTAMP '2025-11-04 09:15:00'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM users WHERE email = 'rita.collins@veltis.example'), 'ADMIN', TIMESTAMP '2026-05-20 09:15:00', TIMESTAMP '2026-05-20 09:15:00'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), 'PM', TIMESTAMP '2026-05-20 09:15:00', TIMESTAMP '2026-05-20 09:15:00');

INSERT INTO role_permissions (organisation_id, role, module, can_read, can_write)
SELECT o.id, p.role, p.module, p.can_read, p.can_write
FROM organisations o
CROSS JOIN (
    VALUES
    ('ORG_ADMIN', 'GM_DASHBOARD', true, true), ('ORG_ADMIN', 'CRM', true, true),
    ('ORG_ADMIN', 'PROJECTS', true, true), ('ORG_ADMIN', 'PLANNING', true, true),
    ('ORG_ADMIN', 'TASKS', true, true), ('ORG_ADMIN', 'FINANCE', true, true),
    ('ORG_ADMIN', 'FORECAST', true, true), ('ORG_ADMIN', 'RISKS', true, true),
    ('ORG_ADMIN', 'CHANGE_REQUESTS', true, true), ('ORG_ADMIN', 'ACTIONS', true, true),
    ('ORG_ADMIN', 'RESOURCES', true, true), ('ORG_ADMIN', 'SUPPLIERS', true, true),
    ('GENERAL_MANAGER', 'GM_DASHBOARD', true, true), ('GENERAL_MANAGER', 'PROJECTS', true, true),
    ('GENERAL_MANAGER', 'FINANCE', true, true), ('GENERAL_MANAGER', 'FORECAST', true, true),
    ('GENERAL_MANAGER', 'RISKS', true, true), ('DEPARTMENT_MANAGER', 'DEPARTMENT_DASHBOARD', true, true),
    ('DEPARTMENT_MANAGER', 'TASKS', true, true), ('EMPLOYEE', 'EMPLOYEE_DASHBOARD', true, true),
    ('EMPLOYEE', 'TASKS', true, false)
) AS p(role, module, can_read, can_write)
WHERE o.code = 'NSIA';

-- Customers, suppliers, and project master data
INSERT INTO customers (organisation_id, customer_code, name, country, town, address, vat_tax_id, contact_person, email, phone, erp_id, active) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'BAY-100', 'Bayer AG', 'Germany', 'Leverkusen', 'Kaiser-Wilhelm-Allee 1, 51373 Leverkusen', 'DE815000018', 'Sabine Krueger', 'sabine.krueger@bayer.example', '+49 214 30 0', 'ERP-BAY-100', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'KRO-220', 'Krones AG', 'Germany', 'Neutraubling', 'Boehmerwaldstrasse 5, 93073 Neutraubling', 'DE811141198', 'Markus Eberl', 'markus.eberl@krones.example', '+49 9401 70 0', 'ERP-KRO-220', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'BOS-330', 'Bosch Rexroth AG', 'Germany', 'Schweinfurt', 'Zum Eisengiesser 1, 97816 Lohr am Main', 'DE811128135', 'Laura Werner', 'laura.werner@boschrexroth.example', '+49 9352 18 0', 'ERP-BOS-330', true),
((SELECT id FROM organisations WHERE code = 'VELTIS'), 'DWA-010', 'Delta Water Authority', 'United Kingdom', 'Leeds', 'Civic House, Leeds LS1 4DY', 'GB349820114', 'Helen Price', 'helen.price@delta-water.example', '+44 113 555 0120', 'DWA-010', true);

INSERT INTO suppliers (organisation_id, name, short_code, country, contact, website, departments_csv, resource_types_csv, notes, active) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Siemens AG', 'SIE', 'Germany', 'Katja Neumann', 'https://www.siemens.com', 'ENG,OPS', 'AUTOMATION_ENG,PLC_DEVELOPER', 'Controls hardware and Siemens PLC components.', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Festo SE & Co. KG', 'FES', 'Germany', 'Oliver Braun', 'https://www.festo.com', 'ENG,OPS', 'SITE_TECHNICIAN', 'Pneumatic equipment and commissioning support.', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'TUV SUD Industrie Service GmbH', 'TUV', 'Germany', 'Petra Kraus', 'https://www.tuvsud.com', 'PMO,OPS', 'PROJECT_CONTROLLER', 'Independent safety review and site acceptance support.', true);

INSERT INTO project_categories (organisation_id, name, code, description, icon, color, active) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Industrial Automation', 'IND_AUT', 'Automation and controls delivery programs.', 'factory', '#2563eb', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Digital Operations', 'DIG_OPS', 'Connected operations and monitoring initiatives.', 'activity', '#7c3aed', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Service Improvement', 'SERVICE', 'Operational service and maintenance improvements.', 'wrench', '#059669', true);

INSERT INTO project_types (organisation_id, name, code, description, icon, color, billable, active) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Turnkey Delivery', 'TURNKEY', 'End-to-end customer delivery including commissioning.', 'briefcase', '#2563eb', true, true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Engineering Change', 'ENG_CHANGE', 'Scoped engineering change with controlled approval.', 'settings', '#f59e0b', true, true),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Internal Improvement', 'INTERNAL', 'Internal capability and process improvement project.', 'trending-up', '#64748b', false, true);

INSERT INTO projects (
    code, name, organisation_id, short_name, portfolio, org_assignment, country,
    project_type, project_phase, priority, planned_start, planned_end,
    project_budget, total_project_budget, project_manager_id, customer, category,
    risk_level, estimated_cost, project_manager_name, program_manager_name,
    sales_manager_name, archived, progress, application_type, comment
) VALUES
(
    'NSC-26-001', 'Bayer Leverkusen Packaging Line Modernisation', (SELECT id FROM organisations WHERE code = 'NSIA'),
    'Bayer Line 4', 'Life Sciences', 'DACH Operations', 'Germany', 'Turnkey Delivery', 'EXECUTION', 'HIGH',
    DATE '2026-05-04', DATE '2026-12-18', 1850000.00, 2075000.00,
    (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), 'Bayer AG', 'Industrial Automation', 'MEDIUM',
    1695000.00, 'Daniel Keller', 'Daniel Keller', 'Marie Hoffmann', false, 56, 'PROJECTUM',
    'Modernisation of packaging Line 4 controls, safety systems, and production reporting.'
),
(
    'NSC-26-014', 'Krones Condition Monitoring Pilot', (SELECT id FROM organisations WHERE code = 'NSIA'),
    'Krones CM', 'Industrial Innovation', 'DACH Operations', 'Germany', 'Engineering Change', 'PLANNING', 'MEDIUM',
    DATE '2026-08-03', DATE '2026-11-27', 420000.00, 470000.00,
    (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), 'Krones AG', 'Digital Operations', 'LOW',
    390000.00, 'Sofia Roth', 'Daniel Keller', 'Marie Hoffmann', false, 12, 'PROJECTUM',
    'Pilot for vibration monitoring, edge connectivity, and maintenance dashboards.'
),
(
    'VFS-26-003', 'Delta Water Remote Monitoring Rollout', (SELECT id FROM organisations WHERE code = 'VELTIS'),
    'Delta Water', 'Utilities', 'UK Field Services', 'United Kingdom', 'Service Improvement', 'EXECUTION', 'HIGH',
    DATE '2026-06-15', DATE '2026-10-30', 285000.00, 310000.00,
    (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), 'Delta Water Authority', 'Service Improvement', 'MEDIUM',
    268000.00, 'Timo Weber', 'Timo Weber', 'Rita Collins', false, 38, 'PROJECTUM',
    'Remote telemetry rollout across 14 pumping stations.'
);

-- Planning, calendars, baselines, and templates
INSERT INTO project_planning (project_id, expected_start, expected_end, project_calendar, probability, keywords, subcontractors) VALUES
((SELECT id FROM projects WHERE code = 'NSC-26-001'), DATE '2026-05-04', DATE '2026-12-18', 'Germany Standard 8h', 90, 'packaging, safety PLC, OEE, traceability', 'Siemens AG; Festo SE; TUV SUD'),
((SELECT id FROM projects WHERE code = 'NSC-26-014'), DATE '2026-08-03', DATE '2026-11-27', 'Germany Standard 8h', 65, 'condition monitoring, IIoT, predictive maintenance', 'Siemens AG'),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), DATE '2026-06-15', DATE '2026-10-30', 'UK Field Services 8h', 85, 'remote monitoring, telemetry, pumping stations', 'Delta Water civil works contractor');

INSERT INTO project_calendars (organisation_id, project_id, name, working_days, hours_per_day, start_time, is_default) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'Germany Standard 8h', '1,2,3,4,5', 8, '08:00', true),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-014'), 'Germany Standard 8h', '1,2,3,4,5', 8, '08:00', true),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), 'UK Field Services 8h', '1,2,3,4,5', 8, '08:00', true);

INSERT INTO project_baselines (organisation_id, project_id, name, created_at, snapshot_json) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'Approved Baseline - May 2026', TIMESTAMP '2026-05-08 16:30:00', '{"version":1,"approvedOn":"2026-05-08","schedule":"Bayer Line 4 approved baseline"}'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-014'), 'Planning Baseline - July 2026', TIMESTAMP '2026-07-01 11:00:00', '{"version":1,"approvedOn":"2026-07-01","schedule":"Krones pilot planning baseline"}');

INSERT INTO project_templates (organisation_id, project_id, name, scope, description, created_at, snapshot_json) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'Turnkey Packaging Line Delivery', 'all', 'Reusable task and WBS structure for regulated packaging-line programmes.', TIMESTAMP '2026-05-10 09:30:00', '{"template":"Turnkey Packaging Line Delivery","includes":"tasks,dependencies,assignments"}');

-- Project schedule
INSERT INTO project_tasks (
    project_id, organisation_id, name, description, duration_days, planned_start,
    planned_end, baseline_start, baseline_end, actual_start, actual_end,
    percent_complete, allocation_percent, planned_hours, actual_hours, priority,
    schedule_mode, status, color, active, display_order, outline_level, task_type,
    wbs_code, department_code, department_id, customer_milestone, assigned_user_id, resource_type
) VALUES
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Project Management', 'Governance, customer reporting, and overall delivery control.', 229, DATE '2026-05-04', DATE '2026-12-18', DATE '2026-05-04', DATE '2026-12-18', DATE '2026-05-04', NULL, 56, 100, 1040.00, 590.00, 100, 'AUTO', 'IN_PROGRESS', '#1d4ed8', true, 10, 1, 'SUMMARY', '1', 'PMO', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), 'PROJECT_CONTROLLER'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Requirements and Safety Review', 'Validate user requirements, safety concept, and customer acceptance criteria.', 25, DATE '2026-05-04', DATE '2026-05-29', DATE '2026-05-04', DATE '2026-05-29', DATE '2026-05-04', DATE '2026-05-28', 100, 100, 200.00, 196.00, 180, 'AUTO', 'COMPLETED', '#2563eb', true, 20, 2, 'ACTIVITY', '1.1', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Detailed Solution Design', 'Issue detailed controls, electrical, and software design package.', 35, DATE '2026-06-01', DATE '2026-07-03', DATE '2026-06-01', DATE '2026-07-03', DATE '2026-06-01', DATE '2026-07-08', 100, 100, 280.00, 304.00, 170, 'AUTO', 'COMPLETED', '#2563eb', true, 30, 2, 'ACTIVITY', '1.2', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), true, (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Engineering and Build', 'Controls hardware build, PLC configuration, and internal integration testing.', 76, DATE '2026-07-06', DATE '2026-09-18', DATE '2026-07-06', DATE '2026-09-18', DATE '2026-07-06', NULL, 56, 100, 1240.00, 692.00, 160, 'AUTO', 'IN_PROGRESS', '#7c3aed', true, 40, 1, 'SUMMARY', '2', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Controls Hardware Build', 'Complete panel modifications and internal electrical checks.', 30, DATE '2026-07-06', DATE '2026-08-04', DATE '2026-07-06', DATE '2026-08-04', DATE '2026-07-06', NULL, 75, 100, 420.00, 320.00, 150, 'AUTO', 'IN_PROGRESS', '#2563eb', true, 50, 2, 'ACTIVITY', '2.1', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'PLC Configuration', 'Develop and test line controls software against the approved design.', 43, DATE '2026-08-05', DATE '2026-09-16', DATE '2026-08-05', DATE '2026-09-16', NULL, NULL, 50, 100, 560.00, 280.00, 145, 'AUTO', 'IN_PROGRESS', '#7c3aed', true, 60, 2, 'ACTIVITY', '2.2', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), 'PLC_DEVELOPER'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Factory Acceptance Test', 'Execute FAT with Bayer engineering and close test observations.', 10, DATE '2026-09-21', DATE '2026-09-30', DATE '2026-09-21', DATE '2026-09-30', NULL, NULL, 0, 100, 260.00, 0.00, 140, 'AUTO', 'NOT_STARTED', '#f59e0b', true, 70, 2, 'MILESTONE', '2.3', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), true, (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Commissioning and Handover', 'On-site commissioning, SAT, training, and project close-out.', 57, DATE '2026-10-05', DATE '2026-11-30', DATE '2026-10-05', DATE '2026-11-30', NULL, NULL, 0, 100, 720.00, 0.00, 130, 'AUTO', 'NOT_STARTED', '#059669', true, 80, 1, 'SUMMARY', '3', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example'), 'SITE_TECHNICIAN'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Site Acceptance Test', 'Commission Line 4 and complete customer site acceptance test.', 15, DATE '2026-10-19', DATE '2026-11-02', DATE '2026-10-19', DATE '2026-11-02', NULL, NULL, 0, 100, 300.00, 0.00, 120, 'AUTO', 'NOT_STARTED', '#059669', true, 90, 2, 'MILESTONE', '3.1', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), true, (SELECT id FROM users WHERE email = 'marta.klein@northstar-ia.example'), 'SITE_TECHNICIAN'),
((SELECT id FROM projects WHERE code = 'NSC-26-014'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Pilot Mobilisation', 'Confirm pilot scope, assets, and customer stakeholders.', 10, DATE '2026-08-03', DATE '2026-08-14', DATE '2026-08-03', DATE '2026-08-14', NULL, NULL, 0, 100, 80.00, 0.00, 100, 'AUTO', 'NOT_STARTED', '#1d4ed8', true, 10, 1, 'SUMMARY', '1', 'PMO', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), 'PROJECT_CONTROLLER'),
((SELECT id FROM projects WHERE code = 'NSC-26-014'), (SELECT id FROM organisations WHERE code = 'NSIA'), 'Sensor Survey', 'Assess selected production assets and instrumentation points.', 15, DATE '2026-08-17', DATE '2026-08-31', DATE '2026-08-17', DATE '2026-08-31', NULL, NULL, 0, 100, 120.00, 0.00, 90, 'AUTO', 'NOT_STARTED', '#2563eb', true, 20, 1, 'ACTIVITY', '2', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'AUTOMATION_ENG'),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM organisations WHERE code = 'VELTIS'), 'Field Survey', 'Survey 14 pumping stations for telemetry installation.', 20, DATE '2026-06-15', DATE '2026-07-03', DATE '2026-06-15', DATE '2026-07-03', DATE '2026-06-15', DATE '2026-07-07', 100, 100, 160.00, 176.00, 100, 'AUTO', 'COMPLETED', '#0f766e', true, 10, 1, 'ACTIVITY', '1', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), false, (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'), 'FIELD_ENGINEER'),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM organisations WHERE code = 'VELTIS'), 'Telemetry Installation', 'Install and commission remote telemetry units.', 45, DATE '2026-07-06', DATE '2026-08-21', DATE '2026-07-06', DATE '2026-08-21', DATE '2026-07-06', NULL, 38, 100, 420.00, 160.00, 90, 'AUTO', 'IN_PROGRESS', '#0f766e', true, 20, 1, 'ACTIVITY', '2', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), false, (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'), 'FIELD_ENGINEER'),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM organisations WHERE code = 'VELTIS'), 'Operations Handover', 'Train the operations team and hand over monitoring dashboards.', 15, DATE '2026-09-07', DATE '2026-09-18', DATE '2026-09-07', DATE '2026-09-18', NULL, NULL, 0, 100, 120.00, 0.00, 80, 'AUTO', 'NOT_STARTED', '#14b8a6', true, 30, 1, 'MILESTONE', '3', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), true, (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), 'FIELD_ENGINEER');

-- Parent relationships are inserted as a separate INSERT-free dependency-safe task structure:
-- children are created with their parent_id from the already inserted WBS parent.
INSERT INTO project_tasks (
    project_id, organisation_id, parent_id, name, description, duration_days, planned_start,
    planned_end, baseline_start, baseline_end, percent_complete, allocation_percent,
    planned_hours, actual_hours, priority, schedule_mode, status, color, active,
    display_order, outline_level, task_type, wbs_code, department_code, department_id,
    customer_milestone, assigned_user_id, resource_type
) VALUES
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1'), 'Weekly Customer Reporting', 'Publish delivery, risk, and forecast report to Bayer.', 229, DATE '2026-05-04', DATE '2026-12-18', DATE '2026-05-04', DATE '2026-12-18', 56, 50, 160.00, 90.00, 110, 'AUTO', 'IN_PROGRESS', '#1d4ed8', true, 15, 2, 'ACTIVITY', '1.0', 'PMO', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'jonas.meier@northstar-ia.example'), 'PROJECT_CONTROLLER'),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2'), 'Integration Test Preparation', 'Prepare the integrated test environment, procedures, and test data.', 15, DATE '2026-09-01', DATE '2026-09-15', DATE '2026-09-01', DATE '2026-09-15', 20, 100, 180.00, 36.00, 142, 'AUTO', 'IN_PROGRESS', '#7c3aed', true, 65, 2, 'ACTIVITY', '2.2.1', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), false, (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), 'PLC_DEVELOPER');

INSERT INTO task_dependencies (project_id, predecessor_task_id, successor_task_id, dependency_type, lag_days) VALUES
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.1'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.2'), 'FS', 0),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.2'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), 'FS', 0),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), 'SS', 10),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2.1'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.3'), 'FS', 0),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '1'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), 'FS', 0);

INSERT INTO task_resource_assignments (project_id, task_id, assigned_user_id, resource_type, assignment_name, quantity, units_percent, cost) VALUES
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'AUTOMATION_ENG', 'Amir Benali', 1, 100, 40320.00),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), 'PLC_DEVELOPER', 'Nina Vogel', 1, 80, 41216.00),
((SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '3.1'), (SELECT id FROM users WHERE email = 'marta.klein@northstar-ia.example'), 'SITE_TECHNICIAN', 'Marta Klein', 1, 100, 11700.00),
((SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'), 'FIELD_ENGINEER', 'Oliver Grant', 1, 100, 30240.00);

INSERT INTO project_task_history (organisation_id, project_id, task_id, task_name, field_name, old_value, new_value, changed_by_user_id, changed_by_name, changed_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), 'Controls Hardware Build', 'percentComplete', '50', '75', (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), 'Amir Benali', TIMESTAMP '2026-07-08 15:10:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), 'PLC Configuration', 'percentComplete', '25', '50', (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), 'Nina Vogel', TIMESTAMP '2026-07-10 11:40:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.2'), 'Detailed Solution Design', 'status', 'IN_PROGRESS', 'COMPLETED', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), 'Elena Fischer', TIMESTAMP '2026-07-08 16:25:00'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), 'Telemetry Installation', 'plannedEnd', '2026-08-21', '2026-08-28', (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), 'Timo Weber', TIMESTAMP '2026-07-09 09:00:00');

INSERT INTO task_console_config (organisation_id, project_id, task_id, checkpoint25, checkpoint50, checkpoint75, channel, notify_pm, notify_owner, notify_dept_manager, notify_all, updated_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), true, true, true, 'BOTH', true, true, true, false, TIMESTAMP '2026-07-10 11:40:00'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), true, true, true, 'APP_ALERT', true, true, false, false, TIMESTAMP '2026-07-09 09:00:00');

INSERT INTO task_console_log (organisation_id, project_id, task_id, message, severity, channel, notify_target, created_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), 'Task reached 50% completion', 'INFO', 'BOTH', 'PM,OWNER,DEPARTMENT_MANAGER', TIMESTAMP '2026-07-10 11:40:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), 'Controls hardware build is tracking five days behind the original plan.', 'WARNING', 'APP_ALERT', 'PM,OWNER', TIMESTAMP '2026-07-10 14:20:00'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), 'Telemetry installation reached 25% completion.', 'INFO', 'APP_ALERT', 'PM,OWNER', TIMESTAMP '2026-07-09 09:00:00');

-- Finance configuration, WBS, actuals, and forecast
INSERT INTO finance_settings (organisation_id, default_hourly_rate) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 85.00),
((SELECT id FROM organisations WHERE code = 'VELTIS'), 72.00);

INSERT INTO finance_hourly_rates (organisation_id, resource_type, hourly_rate) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'AUTOMATION_ENG', 96.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'PLC_DEVELOPER', 92.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'PROJECT_CONTROLLER', 84.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'SITE_TECHNICIAN', 78.00),
((SELECT id FROM organisations WHERE code = 'VELTIS'), 'FIELD_ENGINEER', 72.00);

INSERT INTO finance_owner_mappings (organisation_id, owner_key, resource_type, role_filter, notes) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'ENGINEERING', 'AUTOMATION_ENG', 'EMPLOYEE', 'Engineering labour is owned by the engineering cost centre.'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'SOFTWARE', 'PLC_DEVELOPER', 'EMPLOYEE', 'PLC development labour is tracked separately.'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'PMO', 'PROJECT_CONTROLLER', 'EMPLOYEE', 'Project control effort is held by the PMO cost centre.');

INSERT INTO finance_wbs_template_rows (organisation_id, sort_order, level_no, code_template, description, type, owner_key, hour_rate) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 10, 1, '1', 'Project Management', 'SUMMARY', 'PMO', NULL),
((SELECT id FROM organisations WHERE code = 'NSIA'), 20, 2, '1.1', 'Project Control Labour', 'HOUR', 'PMO', 84.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), 30, 1, '2', 'Engineering and Build', 'SUMMARY', 'ENGINEERING', NULL),
((SELECT id FROM organisations WHERE code = 'NSIA'), 40, 2, '2.1', 'Automation Engineering Labour', 'HOUR', 'ENGINEERING', 96.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), 50, 2, '2.2', 'Controls Hardware', 'COST', 'ENGINEERING', NULL);

INSERT INTO finance_entries (owner_name, owner_id, organisation_id, project_id, project_task_id, wbs_code, description, level, sales, budget, commitment, actual_cost, forecast, owner_key, row_type, hour_rate, is_summary) VALUES
('Daniel Keller', (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), NULL, '1', 'Project Management', 1, 215000.00, 190000.00, 105000.00, 89200.00, 186000.00, 'PMO', 'SUMMARY', NULL, true),
('Jonas Meier', (SELECT id FROM users WHERE email = 'jonas.meier@northstar-ia.example'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.0'), '1.1', 'Project control labour', 2, 125000.00, 104000.00, 56000.00, 48720.00, 101000.00, 'PMO', 'HOUR', 84.00, false),
('Elena Fischer', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), NULL, '2', 'Engineering and Build', 1, 1120000.00, 980000.00, 620000.00, 451800.00, 1015000.00, 'ENGINEERING', 'SUMMARY', NULL, true),
('Amir Benali', (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), '2.1', 'Automation engineering labour', 2, 420000.00, 336000.00, 214000.00, 179400.00, 345600.00, 'ENGINEERING', 'HOUR', 96.00, false),
('Lukas Brandt', (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example'), (SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '3'), '3', 'Commissioning and Handover', 1, 515000.00, 525000.00, 188000.00, 75200.00, 512000.00, 'OPERATIONS', 'COST', NULL, true);

INSERT INTO forecast_entries (organisation_id, project_id, finance_entry_id, wbs_code, period_key, amount) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.1'), '1.1', '2026-08', 15800.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), '2.1', '2026-08', 60400.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), '2.1', '2026-09', 51800.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '3'), '3', '2026-10', 168000.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '3'), '3', '2026-11', 132000.00),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM finance_entries WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1'), '1', '2026-12', 22000.00);

-- Risks, approvals, change control, and actions
INSERT INTO risk_items (organisation_id, project_id, risk_type, state, description, input_date, due_date, mitigation, resource_type_id, owner_user_id, wbs_code, impact, probability, variance_status, approved_by, approved_at, notes) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'risk', 'managing', 'Late delivery of safety PLC components could delay the factory acceptance test.', DATE '2026-06-18', DATE '2026-08-14', 'Secure alternate supplier allocation and hold weekly supplier recovery calls.', (SELECT id FROM resource_types WHERE code = 'PLC_DEVELOPER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), '2.2', '4', 60, NULL, NULL, NULL, 'Supplier recovery plan is active; next review on 2026-07-17.'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'risk', 'variance', 'Additional safety validation is required after the revised customer guard layout.', DATE '2026-07-06', DATE '2026-07-24', 'Submit cost and schedule impact for customer approval before releasing work.', (SELECT id FROM resource_types WHERE code = 'AUTOMATION_ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), '1.2', '3', 50, 'approved', 'Daniel Keller', DATE '2026-07-09', 'Approved internal variance pending formal customer change request.'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-014'), 'opportunity', 'new', 'Use existing edge gateway stock to reduce pilot mobilisation lead time.', DATE '2026-07-08', DATE '2026-08-07', 'Validate compatibility during the sensor survey.', (SELECT id FROM resource_types WHERE code = 'AUTOMATION_ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), '2', '2', 70, NULL, NULL, NULL, 'Potential cost and schedule improvement.'),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), 'risk', 'managing', 'Two remote pumping stations have restricted access windows.', DATE '2026-07-01', DATE '2026-07-22', 'Coordinate access permits with the authority and reserve a weekend crew.', (SELECT id FROM resource_types WHERE code = 'FIELD_ENGINEER' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'), '2', '3', 40, NULL, NULL, NULL, 'Access coordinator confirmed provisional dates.');

INSERT INTO risk_approval_rules (organisation_id, project_id, risk_level, min_risk_value, approver_role, approver_user_id) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), NULL, 'low', 0, 'Project Manager', (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example')),
((SELECT id FROM organisations WHERE code = 'NSIA'), NULL, 'med', 5, 'Senior Manager', (SELECT id FROM users WHERE email = 'marie.hoffmann@northstar-ia.example')),
((SELECT id FROM organisations WHERE code = 'NSIA'), NULL, 'hi', 10, 'Director', (SELECT id FROM users WHERE email = 'marie.hoffmann@northstar-ia.example')),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'crit', 17, 'Project Sponsor', (SELECT id FROM users WHERE email = 'marie.hoffmann@northstar-ia.example'));

INSERT INTO change_requests (organisation_id, project_id, title, status, request_date, value_amount, cost_amount, owner, requester_id, approver_id, affected_task_id, note) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'CR-01 Revised Safety Guard Layout', 'accepted', DATE '2026-07-07', 68000.00, 41200.00, 'Elena Fischer', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.2'), 'Additional validation and panel modification to accommodate the revised safety guard layout.'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'CR-02 Extended Site Acceptance Support', 'submitted', DATE '2026-07-10', 42500.00, 27500.00, 'Lukas Brandt', (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example'), NULL, (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '3.1'), 'Request for two additional site-support days during the customer acceptance window.');

INSERT INTO change_request_history (change_request_id, action, performed_by, performed_by_user_id, created_at) VALUES
((SELECT id FROM change_requests WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND title = 'CR-01 Revised Safety Guard Layout'), 'Created', 'Elena Fischer', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), TIMESTAMP '2026-07-07 10:15:00'),
((SELECT id FROM change_requests WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND title = 'CR-01 Revised Safety Guard Layout'), 'Accepted after internal variance approval', 'Daniel Keller', (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), TIMESTAMP '2026-07-09 14:40:00'),
((SELECT id FROM change_requests WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND title = 'CR-02 Extended Site Acceptance Support'), 'Submitted for customer review', 'Lukas Brandt', (SELECT id FROM users WHERE email = 'lukas.brandt@northstar-ia.example'), TIMESTAMP '2026-07-10 15:20:00');

INSERT INTO action_items (organisation_id, project_id, title, description, action_type, department_code, department_id, priority, status, customer_visible, inserted_date, due_date, owner_id, risk_id, change_request_id, project_task_id) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'Confirm alternate safety PLC allocation', 'Obtain supplier confirmation for the alternate safety PLC allocation and revised delivery date.', 'action', 'ENG', (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'high', 'doing', false, DATE '2026-07-08', DATE '2026-07-17', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), (SELECT id FROM risk_items WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND description = 'Late delivery of safety PLC components could delay the factory acceptance test.'), NULL, (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2')),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), 'Issue revised safety validation pack', 'Prepare the customer-facing validation pack for the accepted safety guard change.', 'action', 'PMO', (SELECT id FROM departments WHERE code = 'PMO' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), 'medium', 'review', true, DATE '2026-07-09', DATE '2026-07-22', (SELECT id FROM users WHERE email = 'jonas.meier@northstar-ia.example'), (SELECT id FROM risk_items WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND description = 'Additional safety validation is required after the revised customer guard layout.'), (SELECT id FROM change_requests WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND title = 'CR-01 Revised Safety Guard Layout'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '1.2')),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), 'Secure station access permits', 'Confirm access permits for the two restricted pumping stations before field deployment.', 'issue', 'OPS', (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), 'high', 'blocked', false, DATE '2026-07-05', DATE '2026-07-22', (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), (SELECT id FROM risk_items WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND description = 'Two remote pumping stations have restricted access windows.'), NULL, (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'));

INSERT INTO action_assignees (action_item_id, assignee_name, assignee_user_id) VALUES
((SELECT id FROM action_items WHERE title = 'Confirm alternate safety PLC allocation'), 'Nina Vogel', (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example')),
((SELECT id FROM action_items WHERE title = 'Issue revised safety validation pack'), 'Jonas Meier', (SELECT id FROM users WHERE email = 'jonas.meier@northstar-ia.example')),
((SELECT id FROM action_items WHERE title = 'Secure station access permits'), 'Oliver Grant', (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'));

INSERT INTO action_comments (action_item_id, author, author_user_id, text, created_at) VALUES
((SELECT id FROM action_items WHERE title = 'Confirm alternate safety PLC allocation'), 'Nina Vogel', (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), 'Supplier has reserved the required safety PLCs; written confirmation is expected Friday.', TIMESTAMP '2026-07-10 10:05:00'),
((SELECT id FROM action_items WHERE title = 'Issue revised safety validation pack'), 'Jonas Meier', (SELECT id FROM users WHERE email = 'jonas.meier@northstar-ia.example'), 'Validation pack has been checked against the approved variance and is ready for customer issue.', TIMESTAMP '2026-07-10 16:10:00');

-- Notifications and department availability
INSERT INTO notifications (organisation_id, project_id, task_id, recipient_user_id, recipient_email, channel, status, severity, subject, message, html_body, retry_count, error_message, created_at, sent_at, next_retry_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.1'), (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), 'daniel.keller@northstar-ia.example', 'APP_ALERT', 'SENT', 'WARNING', 'Controls hardware build is at 75%', 'Controls hardware build has reached 75% complete and remains five days behind plan.', NULL, 0, NULL, TIMESTAMP '2026-07-10 14:20:00', TIMESTAMP '2026-07-10 14:20:01', NULL),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'NSC-26-001') AND wbs_code = '2.2'), (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example'), 'elena.fischer@northstar-ia.example', 'EMAIL', 'PENDING', 'INFO', 'PLC configuration reached 50%', 'PLC configuration has reached the configured 50% completion checkpoint.', '<p>PLC configuration has reached the configured 50% completion checkpoint.</p>', 0, NULL, TIMESTAMP '2026-07-10 11:40:00', NULL, TIMESTAMP '2026-07-11 08:00:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), NULL, (SELECT id FROM users WHERE email = 'marie.hoffmann@northstar-ia.example'), 'marie.hoffmann@northstar-ia.example', 'APP_ALERT', 'SENT', 'INFO', 'Change request accepted', 'CR-01 Revised Safety Guard Layout was accepted for execution.', NULL, 0, NULL, TIMESTAMP '2026-07-09 14:40:00', TIMESTAMP '2026-07-09 14:40:01', NULL),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM projects WHERE code = 'VFS-26-003'), (SELECT id FROM project_tasks WHERE project_id = (SELECT id FROM projects WHERE code = 'VFS-26-003') AND wbs_code = '2'), (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'), 'timo.weber@veltis.example', 'APP_ALERT', 'SENT', 'WARNING', 'Station access risk requires attention', 'Two stations require confirmed access permits before the next installation wave.', NULL, 0, NULL, TIMESTAMP '2026-07-10 09:15:00', TIMESTAMP '2026-07-10 09:15:01', NULL);

INSERT INTO department_holidays (organisation_id, department_id, member_id, from_date, to_date, note, created_by) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM users WHERE email = 'amir.benali@northstar-ia.example'), DATE '2026-08-10', DATE '2026-08-14', 'Planned summer leave.', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example')),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM departments WHERE code = 'ENG' AND organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA')), (SELECT id FROM users WHERE email = 'nina.vogel@northstar-ia.example'), DATE '2026-09-07', DATE '2026-09-11', 'Planned annual leave after FAT preparation.', (SELECT id FROM users WHERE email = 'elena.fischer@northstar-ia.example')),
((SELECT id FROM organisations WHERE code = 'VELTIS'), (SELECT id FROM departments WHERE code = 'OPS' AND organisation_id = (SELECT id FROM organisations WHERE code = 'VELTIS')), (SELECT id FROM users WHERE email = 'oliver.grant@veltis.example'), DATE '2026-08-24', DATE '2026-08-28', 'Scheduled leave; contingency field engineer arranged.', (SELECT id FROM users WHERE email = 'timo.weber@veltis.example'));

-- CRM pipeline, leads, opportunities, and activity
INSERT INTO crm_pipeline_stages (organisation_id, name, color, display_order, is_won, is_lost, created_at, updated_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Qualification', '#64748b', 10, false, false, TIMESTAMP '2025-11-04 09:30:00', TIMESTAMP '2025-11-04 09:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Technical Proposal', '#2563eb', 20, false, false, TIMESTAMP '2025-11-04 09:30:00', TIMESTAMP '2025-11-04 09:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Commercial Review', '#f59e0b', 30, false, false, TIMESTAMP '2025-11-04 09:30:00', TIMESTAMP '2025-11-04 09:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Won', '#16a34a', 40, true, false, TIMESTAMP '2025-11-04 09:30:00', TIMESTAMP '2025-11-04 09:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Lost', '#dc2626', 50, false, true, TIMESTAMP '2025-11-04 09:30:00', TIMESTAMP '2025-11-04 09:30:00');

INSERT INTO crm_leads (organisation_id, full_name, company, email, phone, source, status, owner_id, converted, converted_at, notes, active, created_at, updated_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Dr. Andreas Mueller', 'Krones AG', 'andreas.mueller@krones.example', '+49 9401 70 411', 'TRADE_FAIR', 'CONTACTED', (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), false, NULL, 'Met at Automatica; interested in a condition-monitoring pilot.', true, TIMESTAMP '2026-06-17 10:00:00', TIMESTAMP '2026-07-08 14:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Sabine Krueger', 'Bayer AG', 'sabine.krueger@bayer.example', '+49 214 30 1234', 'CUSTOMER', 'CONVERTED', (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), true, TIMESTAMP '2026-04-22 15:00:00', 'Converted into the Bayer Line 4 delivery programme.', true, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-04-22 15:00:00');

INSERT INTO crm_opportunities (organisation_id, name, account_name, stage_id, value, currency, probability, closing_date, owner_id, lead_id, project_id, is_won, is_lost, notes, created_at, updated_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Krones Condition Monitoring Pilot', 'Krones AG', (SELECT id FROM crm_pipeline_stages WHERE organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA') AND name = 'Technical Proposal'), 420000.00, 'EUR', 65, DATE '2026-08-28', (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), (SELECT id FROM crm_leads WHERE email = 'andreas.mueller@krones.example'), (SELECT id FROM projects WHERE code = 'NSC-26-014'), false, false, 'Technical workshop completed; proposal is being finalised.', TIMESTAMP '2026-06-24 11:00:00', TIMESTAMP '2026-07-08 14:30:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), 'Bayer Packaging Line Modernisation', 'Bayer AG', (SELECT id FROM crm_pipeline_stages WHERE organisation_id = (SELECT id FROM organisations WHERE code = 'NSIA') AND name = 'Won'), 1850000.00, 'EUR', 100, DATE '2026-04-22', (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), (SELECT id FROM crm_leads WHERE email = 'sabine.krueger@bayer.example'), (SELECT id FROM projects WHERE code = 'NSC-26-001'), true, false, 'Won programme now in execution.', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-04-22 15:00:00');

INSERT INTO crm_activities (organisation_id, user_id, activity_type, description, lead_id, opportunity_id, metadata, created_at) VALUES
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), 'LEAD_CREATED', 'Krones condition-monitoring lead recorded after Automatica meeting.', (SELECT id FROM crm_leads WHERE email = 'andreas.mueller@krones.example'), NULL, 'source=Automatica', TIMESTAMP '2026-06-17 10:00:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'sofia.roth@northstar-ia.example'), 'OPPORTUNITY_CREATED', 'Krones pilot opportunity moved to technical proposal.', (SELECT id FROM crm_leads WHERE email = 'andreas.mueller@krones.example'), (SELECT id FROM crm_opportunities WHERE name = 'Krones Condition Monitoring Pilot'), 'probability=65', TIMESTAMP '2026-06-24 11:00:00'),
((SELECT id FROM organisations WHERE code = 'NSIA'), (SELECT id FROM users WHERE email = 'daniel.keller@northstar-ia.example'), 'DEAL_WON', 'Bayer Line 4 programme converted into an active delivery project.', (SELECT id FROM crm_leads WHERE email = 'sabine.krueger@bayer.example'), (SELECT id FROM crm_opportunities WHERE name = 'Bayer Packaging Line Modernisation'), 'project=NSC-26-001', TIMESTAMP '2026-04-22 15:00:00');

COMMIT;

/*
Compatibility report (entity schema reviewed from src/main/java):

INSERTED
  admin_licence_assignments       7
  customers                       4
  project_categories              3
  project_types                   3
  crm_activities                  3
  crm_leads                       2
  crm_opportunities               2
  crm_pipeline_stages             5
  departments                     4
  department_holidays             3
  notifications                   4
  organisations                   2
  project_baselines               2
  project_calendars               3
  project_planning                3
  project_templates               1
  role_permissions               21
  projects                        3
  action_assignees                3
  action_comments                 2
  action_items                    3
  change_request_history          3
  change_requests                 2
  finance_entries                 5
  finance_hourly_rates            5
  finance_owner_mappings          3
  finance_settings                2
  finance_wbs_template_rows       5
  forecast_entries                6
  risk_approval_rules             4
  risk_items                      4
  project_task_history            4
  project_tasks                  16
  task_console_config             2
  task_console_log                3
  task_dependencies               5
  task_resource_assignments       4
  users                          13
  resource_types                  5
  resource_type_dept_map          5
  suppliers                       3

SKIPPED
  password_reset_requests         0  Operational security workflow; no synthetic reset tokens.
  action_attachments              0  Requires binary attachment content; no fake files are seeded.
  change_request_attachments      0  Requires binary attachment content; no fake files are seeded.
*/
