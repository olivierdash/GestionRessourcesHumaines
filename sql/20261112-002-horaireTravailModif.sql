-- 1. Supprimer les anciennes contraintes
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS horaire_travail_pause_minutes_check;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_jour; -- Le vrai nom d'origine
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_unite;

-- 2. Renommer et convertir (on suppose que la colonne d'origine s'appelait jour_semaine)
-- Si votre colonne s'appelle déjà 'unite' (entier), passez directement à la ligne 3.
ALTER TABLE horaire_travail RENAME COLUMN jour_semaine TO dayweek;

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

-- 3. Ajouter les nouvelles contraintes (Check et Unicité)
ALTER TABLE horaire_travail ADD CONSTRAINT ck_horaire_dayweek CHECK (dayweek BETWEEN 1 AND 5);
ALTER TABLE horaire_travail ADD CONSTRAINT uq_horaire_regime_dayweek UNIQUE (id_regime_travail, dayweek);