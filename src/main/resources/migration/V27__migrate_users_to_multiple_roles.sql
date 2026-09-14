-- Migrate the legacy users.role value into a normalized role collection.
-- Run this migration before deploying the multi-role application build.
BEGIN;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(40) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

INSERT INTO user_roles (user_id, role)
SELECT id, role
FROM users
WHERE role IS NOT NULL
ON CONFLICT (user_id, role) DO NOTHING;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM users)
       AND EXISTS (SELECT 1 FROM users WHERE role IS NOT NULL)
       AND EXISTS (
           SELECT 1
           FROM users u
           WHERE u.role IS NOT NULL
             AND NOT EXISTS (
                 SELECT 1 FROM user_roles ur
                 WHERE ur.user_id = u.id AND ur.role = u.role
             )
       ) THEN
        RAISE EXCEPTION 'Role migration verification failed; users.role will not be dropped';
    END IF;
END $$;

ALTER TABLE users DROP COLUMN role;

CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles (role);

COMMIT;
