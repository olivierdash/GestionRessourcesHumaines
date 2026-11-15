-- Utilisateurs (mots de passe BCrypt) : 123 pour alice/bob/claire/david/jack,
-- admin123 pour admin, password123 pour rh/finance/dg/manager
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT v.nom, v.email, v.mdp, r.id_role
FROM (VALUES
  ('Alice RH',           'alice@sirh.com',   crypt('123', gen_salt('bf',10)), 'RH'),
  ('Bob Manager',        'bob@sirh.com',     crypt('123', gen_salt('bf',10)), 'MANAGER'),
  ('Claire DG',          'claire@sirh.com',  crypt('123', gen_salt('bf',10)), 'DG'),
  ('David IT',           'david@sirh.com',   crypt('123', gen_salt('bf',10)), 'RESPONSABLE_IT'),
  ('Test Financier',     'jack@sirh.com',    crypt('123', gen_salt('bf',10)), 'FINANCE'),
  ('Admin',              'admin@sirh.com',   '$2a$10$N/hjpkwuqW0z5oEqj.xv4ufpdQdLUuh56fPgAC7R4JcsCAQcxk7ky', 'ADMIN'),
  ('Responsable RH',     'rh@sirh.com',      '$2a$10$CutwwJNFePQXiIGZOH.gpuI8Tm78MVlU9/y2sD4WD.TfmUE2iDDne', 'RH'),
  ('Contrôleur Finance', 'finance@sirh.com', '$2a$10$wdux5sLa0t6CKBaKNorXN.KkdM7jkagRv1vJopoenKr4Fsw7Rnkhy', 'FINANCE'),
  ('Directeur Général',  'dg@sirh.com',      '$2a$10$gwsBgjnG1kny3w9INSjuaOAumspARUeJRPTOa7FxxpLo0vXUoSmRS', 'DG'),
  ('Manager Équipe',     'manager@sirh.com', '$2a$10$dhqexL4OSOAY62LBlwR5eet0ZhuwOcSj9v5ICIv6.BT3ahBtN7lYq', 'MANAGER')
) AS v(nom, email, mdp, role)
JOIN roles r ON r.libelle = v.role
ON CONFLICT (email) DO NOTHING;
