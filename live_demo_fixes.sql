-- Idempotent final live-demo additions.
BEGIN;

INSERT INTO users (full_name, email, password_hash, role, organisation_id, employee_code, department_code, department_id, job_title, resource_type_id, seniority, hours_per_day, days_per_week, workdays, color, internal_user, default_rate, rate_type, currency, notes, active)
VALUES
('Leon Hartmann', 'leon.hartmann@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code='NSIA'), 'NS-ENG-028', 'ENG', (SELECT id FROM departments WHERE code='ENG' AND organisation_id=(SELECT id FROM organisations WHERE code='NSIA')), 'Automation Engineer', (SELECT id FROM resource_types WHERE code='AUTOMATION_ENG' AND organisation_id=(SELECT id FROM organisations WHERE code='NSIA')), 'MID', 8, 5, 'MON-FRI', '#0284c7', true, 88.00, 'HOURLY', 'EUR', 'Available for the Bosch Vision recovery plan.', true),
('Sara Neumann', 'sara.neumann@northstar-ia.example', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', (SELECT id FROM organisations WHERE code='NSIA'), 'NS-OPS-019', 'OPS', (SELECT id FROM departments WHERE code='OPS' AND organisation_id=(SELECT id FROM organisations WHERE code='NSIA')), 'Commissioning Technician', (SELECT id FROM resource_types WHERE code='SITE_TECHNICIAN' AND organisation_id=(SELECT id FROM organisations WHERE code='NSIA')), 'MID', 8, 5, 'MON-FRI', '#22c55e', true, 76.00, 'HOURLY', 'EUR', 'Reserved for the commissioning wave.', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO notifications (organisation_id, project_id, task_id, recipient_user_id, recipient_email, channel, status, severity, subject, message, retry_count, created_at, next_retry_at)
SELECT o.id, p.id, t.id, u.id, u.email, 'EMAIL', 'PENDING', 'INFO', 'Commissioning readiness review scheduled', 'The commissioning readiness review is scheduled and awaits acknowledgement.', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days'
FROM organisations o
JOIN projects p ON p.organisation_id=o.id AND p.code='NSC-26-001'
JOIN project_tasks t ON t.project_id=p.id AND t.wbs_code='3.1'
JOIN users u ON u.email='marta.klein@northstar-ia.example'
WHERE o.code='NSIA'
  AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.subject='Commissioning readiness review scheduled');

COMMIT;
