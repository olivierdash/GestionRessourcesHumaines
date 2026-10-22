-- ==========================================
-- 1. NETTOYAGE DES TABLES (ATTENTION: SUPPRIME LES DONNÉES EXISTANTES)
-- ==========================================
TRUNCATE TABLE evaluation, candidature, offre_emploi, candidat, demande_recrutement, poste, departement, utilisateur, roles, statut_offre_emploie, statut_candidature, statut_demande_recrutement, type_evaluation RESTART IDENTITY CASCADE;

-- ==========================================
-- 2. LES RÔLES ET UTILISATEURS
-- ==========================================
INSERT INTO roles (libelle) VALUES ('MANAGER'), ('RH'), ('FINANCE'), ('DG');

INSERT INTO utilisateur (nom, email, mot_de_passe, id_role) VALUES 
('Alice RH', 'alice@entreprise.com', 'password123', 2),
('Bob Manager', 'bob@entreprise.com', 'password123', 1),
('Claire DG', 'claire@entreprise.com', 'password123', 4);

-- ==========================================
-- 3. STRUCTURE DE L'ENTREPRISE
-- ==========================================
INSERT INTO departement (titre, id_manager) VALUES 
('Informatique', 2), -- Bob est manager de l'IT
('Ressources Humaines', 1);

INSERT INTO poste (id_departement, intitule, lieu, duree_hebdo) VALUES 
(1, 'Développeur Java Senior', 'Antananarivo', 40),
(2, 'Chargé de recrutement', 'Antananarivo', 35);

-- ==========================================
-- 4. BLOC 1 : DEMANDE DE RECRUTEMENT
-- ==========================================
INSERT INTO statut_demande_recrutement (libelle) VALUES ('BROUILLON'), ('RECRUTEMENT_OUVERT'), ('CLOTUREE');

-- Bob fait une demande pour un Dev Java (Statut: RECRUTEMENT_OUVERT = ID 2)
INSERT INTO demande_recrutement (id_manager, id_poste, effectif, motif, id_statut_demande_recrutement) VALUES 
(2, 1, 2, 'Remplacement d''un départ', 2);

-- ==========================================
-- 5. BLOC 2 : OFFRES ET CANDIDATS
-- ==========================================
INSERT INTO statut_offre_emploie (libelle) VALUES ('BROUILLON'), ('PUBLIEE'), ('CLOTUREE');

-- Création d'une offre PUBLIEE (ID 2)
INSERT INTO offre_emploi (id_demande_recrutement, titre, description, date_publication, id_statut_offre_emploie) VALUES 
(1, 'Développeur Java Spring Boot', 'Nous recherchons un dev Java pour rejoindre notre équipe IT.', NOW(), 2);

-- Création de 3 candidats
INSERT INTO candidat (nom, prenom, telephone, email, cv_url) VALUES 
('Dupont', 'Jean', '0341234567', 'jean.dupont@email.com', 'https://example.com/cv_jean.pdf'),
('Curie', 'Marie', '0329876543', 'marie.curie@email.com', 'https://example.com/cv_marie.pdf'),
('Lefevre', 'Sophie', '0335556667', 'sophie.lefevre@email.com', 'https://example.com/cv_sophie.pdf');

INSERT INTO statut_candidature (libelle) VALUES ('RECU'), ('EN_EVALUATION'), ('RETENU'), ('REFUSE');

-- Les 3 candidats postulent à l'offre #1
INSERT INTO candidature (id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES 
(1, 1, 1, 1), -- Jean postule (RECU = ID 1)
(2, 1, 1, 1), -- Marie postule (RECU = ID 1)
(3, 1, 1, 1); -- Sophie postule (RECU = ID 1)

-- ==========================================
-- 6. BLOC 2 : ÉVALUATIONS
-- ==========================================
INSERT INTO type_evaluation (libelle) VALUES ('TEST_TECHNIQUE'), ('ENTRETIEN_RH'), ('ENTRETIEN_MANAGER');

-- Jean passe un test technique (Note 15/20)
INSERT INTO evaluation (id_candidature, id_evaluateur, id_type_evaluation, evaluation, note) VALUES 
(1, 1, 1, 'Très bon niveau en Java. Maîtrise bien Spring Boot.', 15.50),
(2, 1, 1, 'Connaissances techniques moyennes, à revoir sur SQL.', 9.00);

-- Mise à jour du statut de Jean et Marie car ils ont été évalués (EN_EVALUATION = ID 2)
UPDATE candidature SET id_statut_candidature = 2 WHERE id_candidature IN (1, 2);

-- Sophie reste au statut "RECU" (pas encore évaluée)