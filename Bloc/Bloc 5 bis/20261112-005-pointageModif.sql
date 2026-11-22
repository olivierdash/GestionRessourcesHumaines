-- 1. Supprimer les colonnes de calcul (désormais calculées à la volée par PointageService)
ALTER TABLE pointage
    DROP COLUMN IF EXISTS minutes_travaillees,
    DROP COLUMN IF EXISTS minutes_pause,
    DROP COLUMN IF EXISTS minutes_retard,
    DROP COLUMN IF EXISTS minutes_supplementaires;

-- 2. Supprimer l'ancienne contrainte de vérification des heures
ALTER TABLE pointage DROP CONSTRAINT IF EXISTS ck_pointage_heures;
ALTER TABLE pointage ADD COLUMN IF NOT EXISTS saisi_par INTEGER;


DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pointage_saisi_par') THEN
        ALTER TABLE pointage
            ADD CONSTRAINT fk_pointage_saisi_par
            FOREIGN KEY (saisi_par) REFERENCES utilisateur(id_utilisateur);
    END IF;
END $$;

-- 5. Conversion de SERIAL vers IDENTITY (pour coller au standard SQL et à l'entité Java)
ALTER TABLE pointage ALTER COLUMN id_pointage DROP DEFAULT;
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='pointage' AND column_name='id_pointage' AND is_identity='YES'
    ) THEN
        ALTER TABLE pointage ALTER COLUMN id_pointage ADD GENERATED ALWAYS AS IDENTITY;
    END IF;
END $$;

-- 6. Remplacer l'ancien index par le nouvel index combiné
DROP INDEX IF EXISTS idx_pointage_date;

CREATE INDEX IF NOT EXISTS idx_pointage_employe_date
    ON pointage(id_employer, date_pointage);

-- 7. (Optionnel) Nettoyage du script d'origine qui ajoutait minutes_pause
-- Si le script original a été exécuté, la colonne a déjà été supprimée à l'étape 1, 
-- donc cette ligne n'est plus nécessaire.