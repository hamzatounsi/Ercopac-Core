-- 1. Créer la table des types de milestones si elle n'existe pas
CREATE TABLE IF NOT EXISTS milestone_type (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    letter_code VARCHAR(10) NOT NULL
);

-- 2. Insérer les types de milestones (valeurs réelles)
INSERT INTO milestone_type (code, label, color, letter_code) VALUES
('FAT', 'Factory Acceptance Test', '#9333ea', 'F'),
('SAT', 'Site Acceptance Test', '#2563eb', 'S'),
('LAU', 'Launch', '#d97706', 'L'),
('KOM', 'Kick-off Meeting', '#dc2626', 'K')
ON CONFLICT (code) DO NOTHING;

-- 3. Ajouter les colonnes manquantes à la table des tâches si elles n'existent pas
ALTER TABLE project_task ADD COLUMN IF NOT EXISTS is_milestone BOOLEAN DEFAULT FALSE;
ALTER TABLE project_task ADD COLUMN IF NOT EXISTS milestone_type_id BIGINT REFERENCES milestone_type(id);

-- 4. FORCER l'attribution du type 'LAU' à TOUTES les tâches milestones qui n'ont pas de type
-- (C'est la ligne magique qui va régler ton problème immédiatement)
UPDATE project_task
SET milestone_type_id = (SELECT id FROM milestone_type WHERE code = 'LAU')
WHERE is_milestone = TRUE AND milestone_type_id IS NULL;