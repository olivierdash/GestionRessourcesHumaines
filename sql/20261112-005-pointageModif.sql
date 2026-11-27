ALTER TABLE pointage
    DROP COLUMN IF EXISTS minutes_travaillees,
    DROP COLUMN IF EXISTS minutes_pause,
    DROP COLUMN IF EXISTS minutes_retard,
    DROP COLUMN IF EXISTS minutes_supplementaires;

ALTER TABLE pointage DROP CONSTRAINT IF EXISTS ck_pointage_heures;

ALTER TABLE pointage
    ADD COLUMN IF NOT EXISTS saisi_par INTEGER;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pointage_saisi_par')
       AND NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'pointage'::regclass AND c.contype = 'f' AND a.attname = 'saisi_par'
    ) THEN
        ALTER TABLE pointage
            ADD CONSTRAINT fk_pointage_saisi_par
            FOREIGN KEY (saisi_par) REFERENCES utilisateur(id_utilisateur);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pointage' AND column_name = 'id_pointage' AND is_identity = 'YES'
    ) THEN
        ALTER TABLE pointage ALTER COLUMN id_pointage DROP DEFAULT;
        ALTER TABLE pointage ALTER COLUMN id_pointage ADD GENERATED ALWAYS AS IDENTITY;
    END IF;
END $$;

DROP INDEX IF EXISTS idx_pointage_date;

CREATE INDEX IF NOT EXISTS idx_pointage_employe_date
    ON pointage(id_employer, date_pointage);
