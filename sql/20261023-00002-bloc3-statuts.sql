-- 1) Recaler toutes les séquences sur le plus grand identifiant existant
DO $$
DECLARE r RECORD;
BEGIN
  FOR r IN
    SELECT table_name, column_name
    FROM information_schema.columns
    WHERE table_schema = 'public' AND column_default LIKE 'nextval%'
  LOOP
    EXECUTE format(
      'SELECT setval(pg_get_serial_sequence(%L, %L), COALESCE((SELECT MAX(%I) FROM %I), 0) + 1, false)',
      r.table_name, r.column_name, r.column_name, r.table_name);
  END LOOP;
END $$;

-- 2) Nouveaux statuts du bloc 3
INSERT INTO statut_proposition (libelle) VALUES ('SOUMISE'), ('APPROUVEE'), ('EXPIREE')
ON CONFLICT (libelle) DO NOTHING;

-- 3) Colonne pour la remarque du candidat
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS remarque TEXT;