ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS horaire_travail_pause_minutes_check;

-- 1. Supprimer l'ancienne contrainte d'unicité
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_unite;

-- 2. Renommer la colonne
ALTER TABLE horaire_travail RENAME COLUMN unite TO dayweek;

-- 3. Convertir les anciennes données (String 'MONDAY' -> Integer 1)
ALTER TABLE horaire_travail ALTER COLUMN dayweek TYPE INTEGER USING (
    CASE dayweek
        WHEN 'MONDAY' THEN 1
        WHEN 'TUESDAY' THEN 2
        WHEN 'WEDNESDAY' THEN 3
        WHEN 'THURSDAY' THEN 4
        WHEN 'FRIDAY' THEN 5
        ELSE 1
    END
);