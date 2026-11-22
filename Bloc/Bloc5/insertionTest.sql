
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
