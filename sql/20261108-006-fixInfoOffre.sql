CREATE TABLE IF NOT EXISTS lieu (
    id_lieu  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle  VARCHAR(150) NOT NULL UNIQUE
);

INSERT INTO lieu (libelle) VALUES
('Siège Paris'),
('Agence Lyon'),
('Agence Marseille'),
('Télétravail')
ON CONFLICT (libelle) DO NOTHING;

ALTER TABLE poste ADD COLUMN IF NOT EXISTS id_lieu INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'poste'::regclass AND c.contype = 'f' AND a.attname = 'id_lieu'
    ) THEN
        ALTER TABLE poste
            ADD CONSTRAINT fk_poste_lieu FOREIGN KEY (id_lieu) REFERENCES lieu(id_lieu);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'poste' AND column_name = 'lieu') THEN
        EXECUTE 'INSERT INTO lieu (libelle) SELECT DISTINCT lieu FROM poste WHERE lieu IS NOT NULL AND lieu NOT IN (SELECT libelle FROM lieu) ON CONFLICT (libelle) DO NOTHING';
        EXECUTE 'UPDATE poste SET id_lieu = (SELECT id_lieu FROM lieu WHERE lieu.libelle = poste.lieu) WHERE lieu IS NOT NULL AND id_lieu IS NULL';
        EXECUTE 'ALTER TABLE poste DROP COLUMN lieu';
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_poste_lieu ON poste(id_lieu);
