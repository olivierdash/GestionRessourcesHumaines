ALTER TABLE demande_recrutement ADD COLUMN IF NOT EXISTS id_type_contrat INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_demande_type_contrat'
    ) THEN
        ALTER TABLE demande_recrutement
            ADD CONSTRAINT fk_demande_type_contrat
            FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat);
    END IF;
END $$;

-- Valeur par défaut pour les demandes existantes (CDI = le plus courant)
UPDATE demande_recrutement
SET id_type_contrat = (SELECT id FROM type_contrat WHERE libelle = 'CDI' LIMIT 1)
WHERE id_type_contrat IS NULL;

ALTER TABLE demande_recrutement ALTER COLUMN id_type_contrat SET NOT NULL;