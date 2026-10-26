INSERT INTO statut_offre_emploie (id_statut_offre_emploie, libelle) VALUES (1, 'BROUILLON'), (2, 'PUBLIEE'), (3, 'CLOTUREE');
INSERT INTO statut_candidature (id_statut_candidature, libelle) VALUES (1, 'RECU'), (2, 'EN_EVALUATION'), (3, 'RETENU'), (4, 'REFUSE');
INSERT INTO statut_demande_recrutement (id_statut_demande_recrutement, libelle) VALUES (1, 'BROUILLON'), (2, 'RECRUTEMENT_OUVERT'), (3, 'CLOTUREE');
INSERT INTO statut_proposition (id_statut_proposition, libelle) VALUES (1, 'EN_ATTENTE'), (2, 'ACCEPTE'), (3, 'REFUSE');
INSERT INTO type_evaluation (id_type_evaluation, libelle) VALUES (1, 'TEST_TECHNIQUE'), (2, 'ENTRETIEN_RH'), (3, 'ENTRETIEN_MANAGER');
INSERT INTO type_contrat (id_type_contrat, libelle) VALUES (1, 'CDI'), (2, 'CDD'), (3, 'CE'), (4, 'STAGE');
INSERT INTO type_notification (id_type_notification, libelle) VALUES (1, 'INFO'), (2, 'ALERTE');
INSERT INTO roles (id_role, libelle) VALUES (1, 'MANAGER'), (2, 'RH'), (3, 'FINANCE'), (4, 'DG');

-- ============================================================
-- 5. INSERTION DES DONNÉES DE TEST
-- ============================================================
-- Utilisateurs
INSERT INTO utilisateur (id_utilisateur, nom, email, mot_de_passe, id_role) VALUES 
(1, 'Alice RH', 'alice@sirh.com', '123', 2),
(2, 'Bob Manager', 'bob@sirh.com', '123', 1),
(3, 'Claire DG', 'claire@sirh.com', '123', 4);

-- Départements et Postes
INSERT INTO departement (id_departement, titre, id_manager) VALUES (1, 'Informatique', 2);
INSERT INTO poste (id_poste, id_departement, intitule, lieu) VALUES 
(1, 1, 'Développeur Fullstack', 'Paris');

-- Demande de recrutement (Déjà validée, statut 2 = RECRUTEMENT_OUVERT)
INSERT INTO demande_recrutement (id_demande_recrutement, id_manager, id_poste, effectif, motif, id_statut_demande_recrutement) 
VALUES (1, 2, 1, 2, 'Augmentation de la charge de travail', 2);

-- Candidats
INSERT INTO candidat (id_candidat, nom, prenom, email, cv_url) VALUES 
(1, 'Dupont', 'Jean', 'jean@gmail.com', '/cv/jean.pdf'),
(2, 'Martin', 'Sophie', 'sophie@gmail.com', '/cv/sophie.pdf'),
(3, 'Durand', 'Paul', 'paul@gmail.com', '/cv/paul.pdf');

-- Offre d'emploi (Déjà publiée, statut 2 = PUBLIEE)
-- ⚠️ On l'insère manuellement pour que tu puisses tester direct les candidatures
INSERT INTO offre_emploi (id_offre, id_demande_recrutement, titre, description, date_publication, id_statut_offre_emploie) 
VALUES (1, 1, 'Développeur Fullstack Java/Angular', 'Rejoignez notre équipe IT !', NOW(), 2);

-- Candidatures (3 candidats postulent à l'offre 1)
INSERT INTO candidature (id_candidature, id_candidat, id_demande_recrutement, id_offre, id_statut_candidature) VALUES 
(1, 1, 1, 1, 1), -- Jean (RECU)
(2, 2, 1, 1, 1), -- Sophie (RECU)
(3, 3, 1, 1, 1); -- Paul (RECU)