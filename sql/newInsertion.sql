-- ============================================================
-- 1. INSERTION DES DONNÉES DE RÉFÉRENCE
-- ============================================================
INSERT INTO statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (1, 'BROUILLON'), (2, 'PUBLIEE'), (3, 'CLOTUREE');
INSERT INTO statut_candidature (id_statut_candidature, libelle) VALUES (1, 'RECU'), (2, 'EN_EVALUATION'), (3, 'RETENU'), (4, 'REFUSE');
INSERT INTO statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (1, 'BROUILLON'), (2, 'RECRUTEMENT_OUVERT'), (3, 'CLOTUREE');

-- 🔥 Fusion des statuts de proposition (ceux de base + ceux de ton coéquipier)
INSERT INTO statut_proposition (id_statut_proposition, libelle) VALUES 
(1, 'EN_ATTENTE'), (2, 'ACCEPTE'), (3, 'REFUSE'), 
(4, 'SOUMISE'), (5, 'APPROUVEE'), (6, 'EXPIREE');

INSERT INTO type_evaluation (id_type_evaluation, libelle) VALUES (1, 'TEST_TECHNIQUE'), (2, 'ENTRETIEN_RH'), (3, 'ENTRETIEN_MANAGER');
INSERT INTO type_contrat (id_type_contrat, libelle) VALUES (1, 'CDI'), (2, 'CDD'), (3, 'CE'), (4, 'STAGE');
INSERT INTO type_notification (id_type_notification, libelle) VALUES (1, 'INFO'), (2, 'ALERTE');

INSERT INTO roles (id_role, libelle) VALUES (1, 'MANAGER'), (2, 'RH'), (3, 'FINANCE'), (4, 'DG');

-- ============================================================
-- 2. UTILISATEURS
-- ============================================================
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES 
(1, 'Alice RH', 'alice@sirh.com', '123', 2),
(2, 'Bob Manager', 'bob@sirh.com', '123', 1),
(3, 'Claire DG', 'claire@sirh.com', '123', 4);

-- ============================================================
-- 3. DÉPARTEMENTS (Données de ton coéquipier)
-- ============================================================
INSERT INTO departement (id_departement, titre, id_manager) VALUES 
(1, 'Informatique', 2), (2, 'Data & Analytics', 2), (3, 'Infrastructure & Sécurité', 2), 
(4, 'Design & UX', 2), (5, 'Gestion de Projet', 2), (6, 'Finance & Comptabilité', 2), 
(7, 'Ressources Humaines', 2), (8, 'Commercial & Ventes', 2), (9, 'Marketing & Communication', 2), 
(10, 'Juridique', 2), (11, 'Achats & Logistique', 2), (12, 'Industrie & Production', 2), 
(13, 'BTP & Construction', 2), (14, 'Restauration & Hôtellerie', 2), 
(15, 'Retail & Distribution', 2), (16, 'Administration & Support', 2);

-- ============================================================
-- 4. POSTES (Données de ton coéquipier)
-- ============================================================
INSERT INTO poste (id_poste, id_departement, date_effet, intitule, lieu, duree_hebdo) VALUES
(1, 1, CURRENT_DATE, 'Développeur Java', 'Antananarivo', 40),
(2, 1, CURRENT_DATE, 'Développeur Front-end', 'Antananarivo', 40),
(3, 2, CURRENT_DATE, 'Data Analyst', 'Antananarivo', 40),
(4, 3, CURRENT_DATE, 'Administrateur Systèmes', 'Antananarivo', 40),
(5, 4, CURRENT_DATE, 'Designer UX', 'Antananarivo', 40),
(6, 5, CURRENT_DATE, 'Chef de projet', 'Antananarivo', 40),
(7, 6, CURRENT_DATE, 'Comptable', 'Antananarivo', 40),
(8, 7, CURRENT_DATE, 'Chargé de recrutement', 'Antananarivo', 40),
(9, 8, CURRENT_DATE, 'Commercial', 'Antananarivo', 40);

INSERT INTO grille_salarial (id_poste, id_departement, salaire_min, salaire_max, salaire_moyen)
SELECT id_poste, id_departement, 800000, 3000000, 1800000 FROM poste;

-- ============================================================
-- 5. DONNÉES DE TEST POUR TON BLOC 2
-- ============================================================
-- Demande de recrutement liée au Poste 1 (Développeur Java)
INSERT INTO demande_recrutement (id_demande_recrutement, id_manager, id_poste, effectif, motif, id_statut_demande_recrutement) 
VALUES (1, 2, 1, 2, 'Augmentation de la charge de travail', 2); -- 2 = RECRUTEMENT_OUVERT

-- Candidats
INSERT INTO candidat (id_candidat, nom, prenom, email, cv_url) VALUES 
(1, 'Dupont', 'Jean', 'jean@gmail.com', '/cv/jean.pdf'),
(2, 'Martin', 'Sophie', 'sophie@gmail.com', '/cv/sophie.pdf'),
(3, 'Durand', 'Paul', 'paul@gmail.com', '/cv/paul.pdf');

-- Offre d'emploi (Publiée)
INSERT INTO offre_emploi (id_offre, id_demande_recrutement, titre, description, date_publication, id_statut_offre_emploie) 
VALUES (1, 1, 'Développeur Java Senior', 'Rejoignez notre équipe IT !', CURRENT_TIMESTAMP, 2); -- 2 = PUBLIEE

-- Candidatures (3 candidats postulent)
INSERT INTO candidature (id_candidature, id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES 
(1, 1, 1, 1, 1), -- Jean (RECU)
(2, 2, 1, 1, 1), -- Sophie (RECU)
(3, 3, 1, 1, 1); -- Paul (RECU)

-- ============================================================
-- 6. RÉINITIALISATION DES SÉQUENCES (Évite le bug des ID déjà existants)
-- ============================================================
SELECT setval(pg_get_serial_sequence('utilisateur', 'id_utilisateur'), COALESCE(MAX(id_utilisateur), 1)) FROM utilisateur;
SELECT setval(pg_get_serial_sequence('departement', 'id_departement'), COALESCE(MAX(id_departement), 1)) FROM departement;
SELECT setval(pg_get_serial_sequence('poste', 'id_poste'), COALESCE(MAX(id_poste), 1)) FROM poste;
SELECT setval(pg_get_serial_sequence('demande_recrutement', 'id_demande_recrutement'), COALESCE(MAX(id_demande_recrutement), 1)) FROM demande_recrutement;
SELECT setval(pg_get_serial_sequence('candidat', 'id_candidat'), COALESCE(MAX(id_candidat), 1)) FROM candidat;
SELECT setval(pg_get_serial_sequence('offre_emploi', 'id_offre'), COALESCE(MAX(id_offre), 1)) FROM offre_emploi;
SELECT setval(pg_get_serial_sequence('candidature', 'id_candidature'), COALESCE(MAX(id_candidature), 1)) FROM candidature;