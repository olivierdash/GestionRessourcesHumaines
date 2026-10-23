INSERT INTO statut_proposition (libelle) VALUES ('SOUMISE'), ('APPROUVEE'), ('EXPIREE')
ON CONFLICT (libelle) DO NOTHING;

ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS remarque TEXT;


