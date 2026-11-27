CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Test Financier', 'jack@sirh.com', crypt('123', gen_salt('bf', 10)), r.id_role
FROM roles r
WHERE r.libelle = 'FINANCE'
ON CONFLICT (email) DO NOTHING;
