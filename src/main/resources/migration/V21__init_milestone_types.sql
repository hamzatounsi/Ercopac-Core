-- 1. Créer la table des types de milestones si elle n'existe pas
CREATE TABLE IF NOT EXISTS milestone_type (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    letter_code VARCHAR(10) NOT NULL
);


-- 3. Ajouter les colonnes manquantes à la table des tâches si elles n'existent pas
ALTER TABLE project_task ADD COLUMN IF NOT EXISTS is_milestone BOOLEAN DEFAULT FALSE;
ALTER TABLE project_task ADD COLUMN IF NOT EXISTS milestone_type_id BIGINT REFERENCES milestone_type(id);

-- 4. Lier automatiquement toutes les tâches "milestone" existantes au type "LAU" par défaut
UPDATE project_task
SET milestone_type_id = (SELECT id FROM milestone_type WHERE code = 'LAU')
WHERE is_milestone = TRUE AND milestone_type_id IS NULL;