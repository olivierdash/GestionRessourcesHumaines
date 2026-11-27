CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. Référence
-- ============================================================
INSERT INTO statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (1,'BROUILLON'),(2,'PUBLIEE'),(3,'CLOTUREE');
INSERT INTO statut_candidature (id_statut_candidature, libelle) VALUES (1,'RECU'),(2,'EN_EVALUATION'),(3,'RETENU'),(4,'REFUSE');
INSERT INTO statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (1,'BROUILLON'),(2,'RECRUTEMENT_OUVERT'),(3,'CLOTUREE');
INSERT INTO statut_proposition (id_statut_proposition, libelle) VALUES
(1,'EN_ATTENTE'),(2,'ACCEPTE'),(3,'REFUSE'),(4,'SOUMISE'),(5,'APPROUVEE'),(6,'EXPIREE');
INSERT INTO type_evaluation (id_type_evaluation, libelle) VALUES (1,'TEST_TECHNIQUE'),(2,'ENTRETIEN_RH'),(3,'ENTRETIEN_MANAGER');
INSERT INTO type_contrat (id_type_contrat, libelle) VALUES (1,'CDI'),(2,'CDD'),(3,'CE'),(4,'STAGE');
INSERT INTO type_notification (id_type_notification, libelle) VALUES (1,'INFO'),(2,'ALERTE');
INSERT INTO roles (id_role, libelle) VALUES (1,'MANAGER'),(2,'RH'),(3,'FINANCE'),(4,'DG'),(5,'ADMIN'),(6,'RESPONSABLE_IT');

INSERT INTO lieu (id_lieu, libelle) VALUES
(1,'Antananarivo'),(2,'Siège Paris'),(3,'Agence Lyon'),(4,'Agence Marseille'),(5,'Télétravail');
INSERT INTO etat_demande (id_etat_demande, intitule) VALUES
(1,'Brouillon'),(2,'Cree'),(3,'ValideRH'),(4,'ValideFinance'),(5,'Valide'),(6,'Refuse');
INSERT INTO motif_demande (id_motif_demande, libelle) VALUES
(1,'Creation'),(2,'Remplacement d''un poste');

-- ============================================================
-- 2. Utilisateurs (BCrypt)
-- ============================================================
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES
(1,'Alice RH','alice@sirh.com',crypt('123',gen_salt('bf',10)),2),
(2,'Bob Manager','bob@sirh.com',crypt('123',gen_salt('bf',10)),1),
(3,'Claire DG','claire@sirh.com',crypt('123',gen_salt('bf',10)),4),
(4,'Administrateur','admin@sirh.mg',crypt('admin123',gen_salt('bf',10)),5),
(5,'David IT','david@sirh.com',crypt('123',gen_salt('bf',10)),6);

-- ============================================================
-- 3. Départements
-- ============================================================
INSERT INTO departement (id_departement, titre, id_manager) VALUES
(1,'Informatique',2),(2,'Data & Analytics',2),(3,'Infrastructure & Sécurité',2),
(4,'Design & UX',2),(5,'Gestion de Projet',2),(6,'Finance & Comptabilité',2),
(7,'Ressources Humaines',2),(8,'Commercial & Ventes',2),(9,'Marketing & Communication',2),
(10,'Juridique',2),(11,'Achats & Logistique',2),(12,'Industrie & Production',2),
(13,'BTP & Construction',2),(14,'Restauration & Hôtellerie',2),
(15,'Retail & Distribution',2),(16,'Administration & Support',2);

-- ============================================================
-- 4. Postes et grille salariale
-- ============================================================
INSERT INTO poste (id_poste, id_departement, date_effet, intitule, id_lieu, duree_hebdo) VALUES
(1,1,CURRENT_DATE,'Développeur Java',1,40),
(2,1,CURRENT_DATE,'Développeur Front-end',1,40),
(3,2,CURRENT_DATE,'Data Analyst',1,40),
(4,3,CURRENT_DATE,'Administrateur Systèmes',1,40),
(5,4,CURRENT_DATE,'Designer UX',1,40),
(6,5,CURRENT_DATE,'Chef de projet',1,40),
(7,6,CURRENT_DATE,'Comptable',1,40),
(8,7,CURRENT_DATE,'Chargé de recrutement',1,40),
(9,8,CURRENT_DATE,'Commercial',1,40);

INSERT INTO grille_salarial (id_poste, id_departement, salaire_min, salaire_max)
SELECT id_poste, id_departement, 800000, 3000000 FROM poste;

-- ============================================================
-- 5. Données de test
-- ============================================================
INSERT INTO demande_recrutement (id_demande_recrutement, id_manager, id_poste, effectif, id_motif_demande, id_etat_demande, id_type_contrat, date_creation)
VALUES (1, 2, 1, 2, 1, 5, 1, CURRENT_TIMESTAMP);

INSERT INTO candidat (id_candidat, nom, prenom, email, cv_url) VALUES
(1,'Dupont','Jean','jean@gmail.com','/cv/jean.pdf'),
(2,'Martin','Sophie','sophie@gmail.com','/cv/sophie.pdf'),
(3,'Durand','Paul','paul@gmail.com','/cv/paul.pdf');

INSERT INTO offre_emploi (id_offre, id_demande_recrutement, titre, description, date_publication, id_statut_offre_emploie)
VALUES (1, 1, 'Développeur Java Senior', 'Rejoignez notre équipe IT !', CURRENT_TIMESTAMP, 2);

INSERT INTO candidature (id_candidature, id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES
(1,1,1,1,1),(2,2,1,1,1),(3,3,1,1,1);

-- ============================================================
-- 6. Séquences (obligatoire après des INSERT avec id explicite)
-- ============================================================
SELECT setval(pg_get_serial_sequence(t,c), m) FROM (VALUES
 ('roles','id_role',(SELECT COALESCE(MAX(id_role),1) FROM roles)),
 ('lieu','id_lieu',(SELECT COALESCE(MAX(id_lieu),1) FROM lieu)),
 ('etat_demande','id_etat_demande',(SELECT COALESCE(MAX(id_etat_demande),1) FROM etat_demande)),
 ('motif_demande','id_motif_demande',(SELECT COALESCE(MAX(id_motif_demande),1) FROM motif_demande)),
 ('type_contrat','id_type_contrat',(SELECT COALESCE(MAX(id_type_contrat),1) FROM type_contrat)),
 ('statut_offre_emploie','id_statut_offre_emploie',(SELECT COALESCE(MAX(id_statut_offre_emploie),1) FROM statut_offre_emploie)),
 ('statut_candidature','id_statut_candidature',(SELECT COALESCE(MAX(id_statut_candidature),1) FROM statut_candidature)),
 ('statut_demande_recrutement','id_statut_demande_recrutement',(SELECT COALESCE(MAX(id_statut_demande_recrutement),1) FROM statut_demande_recrutement)),
 ('statut_proposition','id_statut_proposition',(SELECT COALESCE(MAX(id_statut_proposition),1) FROM statut_proposition)),
 ('type_evaluation','id_type_evaluation',(SELECT COALESCE(MAX(id_type_evaluation),1) FROM type_evaluation)),
 ('type_notification','id_type_notification',(SELECT COALESCE(MAX(id_type_notification),1) FROM type_notification)),
 ('utilisateur','id_utilisateur',(SELECT COALESCE(MAX(id_utilisateur),1) FROM utilisateur)),
 ('departement','id_departement',(SELECT COALESCE(MAX(id_departement),1) FROM departement)),
 ('poste','id_poste',(SELECT COALESCE(MAX(id_poste),1) FROM poste)),
 ('demande_recrutement','id_demande_recrutement',(SELECT COALESCE(MAX(id_demande_recrutement),1) FROM demande_recrutement)),
 ('candidat','id_candidat',(SELECT COALESCE(MAX(id_candidat),1) FROM candidat)),
 ('offre_emploi','id_offre',(SELECT COALESCE(MAX(id_offre),1) FROM offre_emploi)),
 ('candidature','id_candidature',(SELECT COALESCE(MAX(id_candidature),1) FROM candidature))
) AS s(t,c,m);

-- ============================================================
-- 7. Référence reprise des anciens scripts (ids automatiques)
-- ============================================================
INSERT INTO statut_employe (libelle) VALUES ('ACTIF'),('INACTIF'),('EN_ONBOARDING') ON CONFLICT (libelle) DO NOTHING;
INSERT INTO type_decompte (code, libelle) VALUES ('HEURES','Décompte en heures'),('JOURS','Décompte en jours') ON CONFLICT (code) DO NOTHING;
INSERT INTO statut_tache_onboarding (libelle) VALUES ('à faire'),('en cours'),('terminée') ON CONFLICT (libelle) DO NOTHING;
INSERT INTO type_document (libelle) VALUES ('Contrat signé'),('Pièce d''identité'),('RIB'),('Autre') ON CONFLICT (libelle) DO NOTHING;
INSERT INTO statut_contrat (libelle) VALUES ('actif'),('terminé') ON CONFLICT (libelle) DO NOTHING;

-- Comptes de l'ancien script (admin123 pour admin, password123 pour les autres)
INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Admin', 'admin@sirh.com', '$2a$10$N/hjpkwuqW0z5oEqj.xv4ufpdQdLUuh56fPgAC7R4JcsCAQcxk7ky', id_role FROM roles WHERE libelle='ADMIN'
ON CONFLICT (email) DO NOTHING;
INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Responsable RH', 'rh@sirh.com', '$2a$10$CutwwJNFePQXiIGZOH.gpuI8Tm78MVlU9/y2sD4WD.TfmUE2iDDne', id_role FROM roles WHERE libelle='RH'
ON CONFLICT (email) DO NOTHING;
INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Contrôleur Finance', 'finance@sirh.com', '$2a$10$wdux5sLa0t6CKBaKNorXN.KkdM7jkagRv1vJopoenKr4Fsw7Rnkhy', id_role FROM roles WHERE libelle='FINANCE'
ON CONFLICT (email) DO NOTHING;
INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Directeur Général', 'dg@sirh.com', '$2a$10$gwsBgjnG1kny3w9INSjuaOAumspARUeJRPTOa7FxxpLo0vXUoSmRS', id_role FROM roles WHERE libelle='DG'
ON CONFLICT (email) DO NOTHING;
INSERT INTO utilisateur (nom, email, mot_de_passe, id_role)
SELECT 'Manager Équipe', 'manager@sirh.com', '$2a$10$dhqexL4OSOAY62LBlwR5eet0ZhuwOcSj9v5ICIv6.BT3ahBtN7lYq', id_role FROM roles WHERE libelle='MANAGER'
ON CONFLICT (email) DO NOTHING;
