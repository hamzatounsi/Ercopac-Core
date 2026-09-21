-- Mise à jour de la contrainte user_roles pour inclure H24 et H24_LEAD
ALTER TABLE user_roles 
DROP CONSTRAINT IF EXISTS user_roles_role_check;

ALTER TABLE user_roles 
ADD CONSTRAINT user_roles_role_check 
CHECK (
    role IN (
        'PLATFORM_OWNER',
        'ORG_ADMIN',
        'PROJECT_MANAGER',
        'PROJECT_MANAGER_LEAD',
        'MANAGER',
        'DEPARTMENT_MANAGER',
        'EMPLOYEE',
        'SALES_MANAGER_LEAD',
        'SALES_MANAGER',
        'SYSTEM_ENGINEER',
        'CLIENT',
        'H24',
        'H24_LEAD'
    )
);