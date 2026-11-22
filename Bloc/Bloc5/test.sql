-- ============================================================
-- 1. DONNÉES DE RÉFÉRENCE
-- ============================================================
INSERT INTO statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (1, 'BROUILLON'), (2, 'PUBLIEE'), (3, 'CLOTUREE');
INSERT INTO statut_candidature (id_statut_candidature, libelle) VALUES (1, 'RECU'), (2, 'EN_EVALUATION'), (3, 'RETENU'), (4, 'REFUSE');
INSERT INTO statut_proposition (id_statut_proposition, libelle) VALUES (1, 'EN_ATTENTE'), (2, 'ACCEPTE'), (3, 'REFUSE'), (4, 'SOUMISE'), (5, 'APPROUVEE'), (6, 'EXPIREE');
INSERT INTO etat_demande (id_etat_demande, intitule) VALUES (1, 'Brouillon'), (2, 'Cree'), (3, 'ValideRH'), (4, 'ValideFinance'), (5, 'Valide'), (6, 'Refuse');
INSERT INTO motif_demande (id_motif_demande, libelle) VALUES (1, 'Creation'), (2, 'Remplacement d''un poste');
INSERT INTO statut_contrat (id_statut_contrat, libelle) VALUES (1, 'actif'), (2, 'terminé');
INSERT INTO statut_tache_onboarding (id_statut_tache_onboarding, libelle) VALUES (1, 'à faire'), (2, 'en cours'), (3, 'terminée');
INSERT INTO type_document (id_type_document, libelle) VALUES (1, 'Contrat signé'), (2, 'Pièce d''identité'), (3, 'RIB'), (4, 'Autre');
INSERT INTO type_evaluation (id_type_evaluation, libelle) VALUES (1, 'TEST_TECHNIQUE'), (2, 'ENTRETIEN_RH'), (3, 'ENTRETIEN_MANAGER');
INSERT INTO type_notification (id_type_notification, libelle) VALUES (1, 'INFO'), (2, 'ALERTE');
INSERT INTO statut_employe (libelle) VALUES ('ACTIF'), ('INACTIF'), ('EN_ONBOARDING');
INSERT INTO lieu (libelle) VALUES ('Siège Paris'), ('Agence Lyon'), ('Agence Marseille'), ('Télétravail');
INSERT INTO type_contrat (id_type_contrat, libelle, duree_max_mois) VALUES 
(1, 'CDI', NULL), (2, 'CDD', 18), (3, 'CE', NULL), (4, 'STAGE', 6);
INSERT INTO roles (id_role, libelle) VALUES 
(1, 'MANAGER'), (2, 'RH'), (3, 'FINANCE'), (4, 'DG'), (5, 'RESPONSABLE_IT'), (6, 'ADMIN');
INSERT INTO statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (1, 'BROUILLON'), (2, 'RECRUTEMENT_OUVERT'), (3, 'CLOTUREE');

-- ============================================================
-- 2. UTILISATEURS
-- ============================================================
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES 
(1, 'Alice RH', 'alice@sirh.com', '$2a$10$CutwwJNFePQXiIGZOH.gpuI8Tm78MVlU9/y2sD4WD.TfmUE2iDDne', 2), -- password123
(2, 'Bob Manager', 'bob@sirh.com', '$2a$10$dhqexL4OSOAY62LBlwR5eet0ZhuwOcSj9v5ICIv6.BT3ahBtN7lYq', 1), -- password123
(3, 'Claire DG', 'claire@sirh.com', '$2a$10$gwsBgjnG1kny3w9INSjuaOAumspARUeJRPTOa7FxxpLo0vXUoSmRS', 4), -- password123
(4, 'Super Admin', 'admin@sirh.com', '$2a$10$N/hjpkwuqW0z5oEqj.xv4ufpdQdLUuh56fPgAC7R4JcsCAQcxk7ky', 6); -- admin123

-- ============================================================
-- 3. STRUCTURE
-- ============================================================
INSERT INTO departement (id_departement, titre, id_manager) VALUES (1, 'Informatique', 2), (2, 'Data', 2);
INSERT INTO poste (id_poste, id_departement, date_effet, intitule, id_lieu, duree_hebdo) VALUES
(1, 1, CURRENT_DATE, 'Développeur Java', 1, 40),
(2, 2, CURRENT_DATE, 'Data Analyst', 4, 40);
INSERT INTO grille_salarial (id_poste, id_departement, salaire_min, salaire_max)
SELECT id_poste, id_departement, 800000, 3000000 FROM poste;

-- ============================================================
-- 4. DONNÉES DE TEST
-- ============================================================
INSERT INTO demande_recrutement (id_demande_recrutement, id_manager, id_poste, effectif, id_etat_demande, id_motif_demande, id_type_contrat, id_statut_demande_recrutement, commentaires) 
VALUES (1, 2, 1, 2, 2, 1, 1, 2, 'Recrutement pour nouveau projet');

INSERT INTO candidat (id_candidat, nom, prenom, email, cv_url, mot_de_passe) VALUES 
(1, 'Dupont', 'Jean', 'jean@gmail.com', '/cv/jean.pdf', '123'),
(2, 'Martin', 'Sophie', 'sophie@gmail.com', '/cv/sophie.pdf', '123');

INSERT INTO offre_emploi (id_offre, id_demande_recrutement, titre, description, date_publication, id_statut_offre_emploie) 
VALUES (1, 1, 'Développeur Java Senior', 'Rejoignez-nous !', CURRENT_TIMESTAMP, 2);

INSERT INTO candidature (id_candidature, id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES 
(1, 1, 1, 1, 1), (2, 2, 1, 1, 1);

-- Reset des séquences
SELECT setval(pg_get_serial_sequence('utilisateur', 'id_utilisateur'), COALESCE(MAX(id_utilisateur), 1)) FROM utilisateur;
SELECT setval(pg_get_serial_sequence('departement', 'id_departement'), COALESCE(MAX(id_departement), 1)) FROM departement;
SELECT setval(pg_get_serial_sequence('poste', 'id_poste'), COALESCE(MAX(id_poste), 1)) FROM poste;
SELECT setval(pg_get_serial_sequence('demande_recrutement', 'id_demande_recrutement'), COALESCE(MAX(id_demande_recrutement), 1)) FROM demande_recrutement;
SELECT setval(pg_get_serial_sequence('candidat', 'id_candidat'), COALESCE(MAX(id_candidat), 1)) FROM candidat;
SELECT setval(pg_get_serial_sequence('offre_emploi', 'id_offre'), COALESCE(MAX(id_offre), 1)) FROM offre_emploi;
SELECT setval(pg_get_serial_sequence('candidature', 'id_candidature'), COALESCE(MAX(id_candidature), 1)) FROM candidature;