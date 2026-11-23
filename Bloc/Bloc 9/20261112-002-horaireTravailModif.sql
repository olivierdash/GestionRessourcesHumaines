-- 1. Supprimer les anciennes contraintes
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS horaire_travail_pause_minutes_check;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_jour;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_unite;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS ck_horaire_dayweek;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_dayweek;

-- 2. Renommer la colonne uniquement si elle utilise encore l'ancien nom
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'horaire_travail'
          AND column_name = 'jour_semaine'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'horaire_travail'
          AND column_name = 'dayweek'
    ) THEN
        ALTER TABLE horaire_travail RENAME COLUMN jour_semaine TO dayweek;
    END IF;
END $$;

-- 3. Convertir les anciens textes vers des entiers, ou conserver les entiers existants
DO $$
DECLARE
    v_type TEXT;
BEGIN
    SELECT data_type
    INTO v_type
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'horaire_travail'
      AND column_name = 'dayweek';

    IF v_type IN ('character varying', 'character', 'text') THEN
        ALTER TABLE horaire_travail
            ALTER COLUMN dayweek TYPE INTEGER
            USING (
                CASE UPPER(dayweek)
                    WHEN 'MONDAY' THEN 1
                    WHEN 'TUESDAY' THEN 2
                    WHEN 'WEDNESDAY' THEN 3
                    WHEN 'THURSDAY' THEN 4
                    WHEN 'FRIDAY' THEN 5
                    ELSE dayweek::INTEGER
                END
            );
    ELSIF v_type <> 'integer' THEN
        ALTER TABLE horaire_travail
            ALTER COLUMN dayweek TYPE INTEGER
            USING dayweek::TEXT::INTEGER;
    END IF;
END $$;

-- 4. Ajouter les nouvelles contraintes
ALTER TABLE horaire_travail
    ADD CONSTRAINT ck_horaire_dayweek
    CHECK (dayweek BETWEEN 1 AND 5);

ALTER TABLE horaire_travail
    ADD CONSTRAINT uq_horaire_regime_dayweek
    UNIQUE (id_regime_travail, dayweek);