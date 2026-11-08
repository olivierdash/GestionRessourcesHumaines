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

INSERT INTO lieu (libelle)
SELECT DISTINCT lieu FROM poste
WHERE lieu IS NOT NULL
  AND lieu NOT IN (SELECT libelle FROM lieu)
ON CONFLICT (libelle) DO NOTHING;

ALTER TABLE poste ADD COLUMN IF NOT EXISTS id_lieu INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_poste_lieu'
    ) THEN
        ALTER TABLE poste
            ADD CONSTRAINT fk_poste_lieu FOREIGN KEY (id_lieu) REFERENCES lieu(id_lieu);
    END IF;
END $$;

UPDATE poste
SET id_lieu = (SELECT id_lieu FROM lieu WHERE lieu.libelle = poste.lieu)
WHERE lieu IS NOT NULL AND id_lieu IS NULL;

CREATE INDEX IF NOT EXISTS idx_poste_lieu ON poste(id_lieu);

ALTER TABLE poste DROP COLUMN lieu;