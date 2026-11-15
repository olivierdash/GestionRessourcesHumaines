-- 1. Supprimer les anciennes contraintes
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS horaire_travail_pause_minutes_check;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_jour;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_unite;

-- 2. Renommer et convertir jour_semaine en dayweek (seulement si l'ancienne colonne existe encore)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'horaire_travail' AND column_name = 'jour_semaine') THEN
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
    END IF;
END $$;

-- 3. Nouvelles contraintes (seulement si absentes)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_horaire_dayweek') THEN
        ALTER TABLE horaire_travail ADD CONSTRAINT ck_horaire_dayweek CHECK (dayweek BETWEEN 1 AND 5);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_horaire_regime_dayweek') THEN
        ALTER TABLE horaire_travail ADD CONSTRAINT uq_horaire_regime_dayweek UNIQUE (id_regime_travail, dayweek);
    END IF;
END $$;
