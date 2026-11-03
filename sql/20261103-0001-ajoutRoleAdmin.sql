INSERT INTO roles (libelle) VALUES ('ADMIN');

--- email : admin@sirh.com
--- mot_de_passe : admin123

-- autres mot_de_passe pour les autres utilisateurs : password123

INSERT into utilisateur (email, mot_de_passe, role_id) VALUES ('admin@sirh.com','$2a$10$N/hjpkwuqW0z5oEqj.xv4ufpdQdLUuh56fPgAC7R4JcsCAQcxk7ky', (SELECT id FROM roles WHERE libelle = 'ADMIN')),	
 ('Responsable RH','rh@sirh.com','$2a$10$CutwwJNFePQXiIGZOH.gpuI8Tm78MVlU9/y2sD4WD.TfmUE2iDDne',(SELECT id FROM roles WHERE libelle LIKE 'RH')),
	 ('Contrôleur Finance','finance@sirh.com','$2a$10$wdux5sLa0t6CKBaKNorXN.KkdM7jkagRv1vJopoenKr4Fsw7Rnkhy',(SELECT id FROM roles WHERE libelle LIKE 'FINANCE')),
	 ('Directeur Général','dg@sirh.com','$2a$10$gwsBgjnG1kny3w9INSjuaOAumspARUeJRPTOa7FxxpLo0vXUoSmRS',(SELECT id FROM roles WHERE libelle LIKE 'DG')),
	 ('Manager Équipe','manager@sirh.com','$2a$10$dhqexL4OSOAY62LBlwR5eet0ZhuwOcSj9v5ICIv6.BT3ahBtN7lYq',(SELECT id FROM roles WHERE libelle LIKE 'MANAGER'));