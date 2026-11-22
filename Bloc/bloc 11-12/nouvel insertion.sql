-- Données de test SIRH (à exécuter APRÈS 01_schema_sirh.sql)
-- psql -v ON_ERROR_STOP=1 -U <user> -d sirh -f 02_donnees_test.sql

-- ======================================================================
-- FICHIER : (préparation)
-- Le module congés a inséré le rôle EMPLOYE avec id=1, ce qui entrerait en collision
-- avec les rôles explicites (1..6) de test.sql. On le retire puis on le réinsère après.
-- ======================================================================
DELETE FROM roles WHERE libelle = 'EMPLOYE';

-- ======================================================================
-- FICHIER : test.sql
-- ======================================================================
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

-- ======================================================================
-- FICHIER : insertionTest.sql
-- ======================================================================
-- Candidats / candidatures supplémentaires (poste Data Analyst)
INSERT INTO demande_recrutement (id_demande_recrutement, id_manager, id_poste, effectif, id_etat_demande, id_motif_demande, id_type_contrat, id_statut_demande_recrutement, commentaires)
VALUES (2, 2, 2, 2, 5, 1, 1, 3, 'Recrutement Data Analyst');

INSERT INTO offre_emploi (id_offre, id_demande_recrutement, titre, description, date_publication, date_cloture, id_statut_offre_emploie)
VALUES (2, 2, 'Data Analyst', 'Poste data analyst', CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 3);

INSERT INTO candidat (id_candidat, nom, prenom, telephone, email, cv_url, mot_de_passe) VALUES
(3, 'Rakoto', 'Hery', '0341234567', 'hery@gmail.com', '/cv/hery.pdf', '123'),
(4, 'Rabe', 'Lova', '0331234567', 'lova@gmail.com', '/cv/lova.pdf', '123');

INSERT INTO candidature (id_candidature, id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES
(3, 3, 2, 2, 3), -- RETENU
(4, 4, 2, 2, 3); -- RETENU

-- Les 2 candidatures initiales deviennent aussi "RETENU" puisqu'elles vont donner des employés
UPDATE candidature SET id_statut_candidature = 3 WHERE id_candidature IN (1, 2);

-- Employés
INSERT INTO employer (id_employer, id_candidature, hiring_date, status_employer, statut_employe_id) VALUES
(1, 1, CURRENT_DATE - 400, 'ACTIF', 1),
(2, 2, CURRENT_DATE - 200, 'ACTIF', 1),
(3, 3, CURRENT_DATE - 20, 'EN_ONBOARDING', 3),
(4, 4, CURRENT_DATE - 5, 'EN_ONBOARDING', 3);
-- matricule généré automatiquement par le trigger trg_set_matricule

-- Contrats
INSERT INTO contract (id_contract, id_employer, id_poste, id_type_contrat, id_statut_contrat, date_debut, date_fin, salaire_mensuel) VALUES
(1, 1, 1, 1, 1, CURRENT_DATE - 400, NULL, 2500000),
(2, 2, 1, 2, 1, CURRENT_DATE - 200, CURRENT_DATE + 140, 1800000),
(3, 3, 2, 1, 1, CURRENT_DATE - 20, NULL, 2000000),
(4, 4, 2, 4, 1, CURRENT_DATE - 5, CURRENT_DATE + 175, 900000);

-- Affectations
INSERT INTO affectation (id_affectation, id_employer, id_poste, date_debut, date_fin) VALUES
(1, 1, 1, CURRENT_DATE - 400, NULL),
(2, 2, 1, CURRENT_DATE - 200, NULL),
(3, 3, 2, CURRENT_DATE - 20, NULL),
(4, 4, 2, CURRENT_DATE - 5, NULL);

-- Onboarding (uniquement pour les 2 employés EN_ONBOARDING)
INSERT INTO onboarding (id_onboarding, id_employer, date_debut, date_fin) VALUES
(1, 3, CURRENT_DATE - 20, NULL),
(2, 4, CURRENT_DATE - 5, NULL);

INSERT INTO onboarding_tache (id_tache, id_onboarding, titre, id_responsable, date_echeance, est_obligatoire, id_statut_tache_onboarding) VALUES
(1, 1, 'Signature du contrat', 1, CURRENT_DATE - 15, TRUE, 3),
(2, 1, 'Remise badge accès', 1, CURRENT_DATE - 10, TRUE, 3),
(3, 1, 'Formation sécurité', 1, CURRENT_DATE + 5, TRUE, 2),
(4, 2, 'Signature du contrat', 1, CURRENT_DATE + 2, TRUE, 1),
(5, 2, 'Remise matériel informatique', 1, CURRENT_DATE + 3, TRUE, 1);

-- Coordonnées (une par employé, unique)
INSERT INTO coordonnees_employe (id_coordonnees, id_employer, adresse, code_postal, ville, pays, situation_matrimoniale, nombre_enfants, telephone_personnel) VALUES
(1, 1, '12 Rue des Lilas', '75011', 'Paris', 'France', 'Célibataire', 0, '0601020304'),
(2, 2, '5 Avenue Victor Hugo', '69002', 'Lyon', 'France', 'Marié(e)', 2, '0611223344'),
(3, 3, 'Lot II M 45 Ankadifotsy', '101', 'Antananarivo', 'Madagascar', 'Célibataire', 0, '0341234567'),
(4, 4, 'Lot IVB 12 Ivandry', '101', 'Antananarivo', 'Madagascar', 'Divorcé(e)', 1, '0331234567');

-- Documents (le #2 a une date d'expiration déjà passée : sert à tester
-- l'alerte "documents expirés" de dossier-salarie.html)
INSERT INTO document (id_document, id_employer, id_type_document, id_tache, nom_fichier, fichier_url, date_depot, date_expiration, statut_document, id_depose_par) VALUES
(1, 1, 1, NULL, 'contrat_signe_emp1.pdf', '/documents/emp1/contrat.pdf', CURRENT_TIMESTAMP - INTERVAL '399 days', NULL, 'VALIDE', 1),
(2, 1, 2, NULL, 'cni_emp1.pdf', '/documents/emp1/cni.pdf', CURRENT_TIMESTAMP - INTERVAL '399 days', CURRENT_DATE - 10, 'VALIDE', 1), -- expiré (test alerte)
(3, 2, 1, NULL, 'contrat_signe_emp2.pdf', '/documents/emp2/contrat.pdf', CURRENT_TIMESTAMP - INTERVAL '199 days', NULL, 'VALIDE', 1),
(4, 2, 3, NULL, 'rib_emp2.pdf', '/documents/emp2/rib.pdf', CURRENT_TIMESTAMP - INTERVAL '199 days', NULL, 'VALIDE', 1),
(5, 3, 1, 1, 'contrat_signe_emp3.pdf', '/documents/emp3/contrat.pdf', CURRENT_TIMESTAMP - INTERVAL '15 days', NULL, 'VALIDE', 1),
(6, 4, 2, NULL, 'cni_emp4.pdf', '/documents/emp4/cni.pdf', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_DATE + 300, 'VALIDE', 1);

-- Journal d'audit (quelques entrées d'exemple)
INSERT INTO journal_audit (id_audit, id_utilisateur, action, entite_cible, id_entite_cible, details, date_action) VALUES
(1, 1, 'CONSULTATION_DOSSIER', 'employer', 1, 'Consultation du dossier complet', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(2, 1, 'MODIFICATION_COORDONNEES', 'employer', 2, 'Adresse modifiée', CURRENT_TIMESTAMP - INTERVAL '1 days');

-- Reset des séquences (partie employés)
SELECT setval(pg_get_serial_sequence('demande_recrutement', 'id_demande_recrutement'), COALESCE(MAX(id_demande_recrutement), 1)) FROM demande_recrutement;
SELECT setval(pg_get_serial_sequence('offre_emploi', 'id_offre'), COALESCE(MAX(id_offre), 1)) FROM offre_emploi;
SELECT setval(pg_get_serial_sequence('candidat', 'id_candidat'), COALESCE(MAX(id_candidat), 1)) FROM candidat;
SELECT setval(pg_get_serial_sequence('candidature', 'id_candidature'), COALESCE(MAX(id_candidature), 1)) FROM candidature;
SELECT setval(pg_get_serial_sequence('employer', 'id_employer'), COALESCE(MAX(id_employer), 1)) FROM employer;
SELECT setval(pg_get_serial_sequence('contract', 'id_contract'), COALESCE(MAX(id_contract), 1)) FROM contract;
SELECT setval(pg_get_serial_sequence('affectation', 'id_affectation'), COALESCE(MAX(id_affectation), 1)) FROM affectation;
SELECT setval(pg_get_serial_sequence('onboarding', 'id_onboarding'), COALESCE(MAX(id_onboarding), 1)) FROM onboarding;
SELECT setval(pg_get_serial_sequence('onboarding_tache', 'id_tache'), COALESCE(MAX(id_tache), 1)) FROM onboarding_tache;
SELECT setval(pg_get_serial_sequence('coordonnees_employe', 'id_coordonnees'), COALESCE(MAX(id_coordonnees), 1)) FROM coordonnees_employe;
SELECT setval(pg_get_serial_sequence('document', 'id_document'), COALESCE(MAX(id_document), 1)) FROM document;
SELECT setval(pg_get_serial_sequence('journal_audit', 'id_audit'), COALESCE(MAX(id_audit), 1)) FROM journal_audit;

-- ======================================================================
-- FICHIER : (finalisation)
-- Réinsère EMPLOYE après avoir recalé la séquence des rôles.
-- ======================================================================
SELECT setval(pg_get_serial_sequence('roles', 'id_role'), (SELECT MAX(id_role) FROM roles));
INSERT INTO roles(libelle) VALUES ('EMPLOYE') ON CONFLICT (libelle) DO NOTHING;
