--- admin@sirh.com / admin123
--- rh@sirh.com, finance@sirh.com, dg@sirh.com, manager@sirh.com / password123

INSERT INTO roles (libelle)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE libelle = 'ADMIN');

INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT v.nom, v.email, v.mdp, r.id_role
FROM (VALUES
  ('Admin',              'admin@sirh.com',   '$2a$10$N/hjpkwuqW0z5oEqj.xv4ufpdQdLUuh56fPgAC7R4JcsCAQcxk7ky', 'ADMIN'),
  ('Responsable RH',     'rh@sirh.com',      '$2a$10$CutwwJNFePQXiIGZOH.gpuI8Tm78MVlU9/y2sD4WD.TfmUE2iDDne', 'RH'),
  ('Contrôleur Finance', 'finance@sirh.com', '$2a$10$wdux5sLa0t6CKBaKNorXN.KkdM7jkagRv1vJopoenKr4Fsw7Rnkhy', 'FINANCE'),
  ('Directeur Général',  'dg@sirh.com',      '$2a$10$gwsBgjnG1kny3w9INSjuaOAumspARUeJRPTOa7FxxpLo0vXUoSmRS', 'DG'),
  ('Manager Équipe',     'manager@sirh.com', '$2a$10$dhqexL4OSOAY62LBlwR5eet0ZhuwOcSj9v5ICIv6.BT3ahBtN7lYq', 'MANAGER')
) AS v(nom, email, mdp, role)
JOIN roles r ON r.libelle = v.role
ON CONFLICT (email) DO NOTHING;
