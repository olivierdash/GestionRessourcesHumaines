-- Données de référence (rejouable : n'ajoute que ce qui manque)
-- Valeurs alignées sur ConstantSirh.java
DO $$
DECLARE r record;
BEGIN
  FOR r IN SELECT * FROM (VALUES
    ('roles','libelle','MANAGER'),('roles','libelle','RH'),('roles','libelle','FINANCE'),
    ('roles','libelle','DG'),('roles','libelle','ADMIN'),('roles','libelle','RESPONSABLE_IT'),
    ('lieu','libelle','Antananarivo'),('lieu','libelle','Siège Paris'),('lieu','libelle','Agence Lyon'),
    ('lieu','libelle','Agence Marseille'),('lieu','libelle','Télétravail'),
    ('etat_demande','intitule','Brouillon'),('etat_demande','intitule','Cree'),
    ('etat_demande','intitule','ValideRH'),('etat_demande','intitule','ValideFinance'),
    ('etat_demande','intitule','Valide'),('etat_demande','intitule','Refuse'),
    ('motif_demande','libelle','Creation'),('motif_demande','libelle','Remplacement d''un poste'),
    ('type_contrat','libelle','CDI'),('type_contrat','libelle','CDD'),
    ('type_contrat','libelle','CE'),('type_contrat','libelle','STAGE'),
    ('statut_offre_emploie','libelle','BROUILLON'),('statut_offre_emploie','libelle','PUBLIEE'),
    ('statut_offre_emploie','libelle','CLOTUREE'),
    ('statut_candidature','libelle','RECU'),('statut_candidature','libelle','EN_EVALUATION'),
    ('statut_candidature','libelle','RETENU'),('statut_candidature','libelle','REFUSE'),
    ('statut_demande_recrutement','libelle','BROUILLON'),
    ('statut_demande_recrutement','libelle','RECRUTEMENT_OUVERT'),
    ('statut_demande_recrutement','libelle','CLOTUREE'),
    ('statut_proposition','libelle','EN_ATTENTE'),('statut_proposition','libelle','SOUMISE'),
    ('statut_proposition','libelle','APPROUVEE'),('statut_proposition','libelle','ACCEPTE'),
    ('statut_proposition','libelle','REFUSE'),('statut_proposition','libelle','EXPIREE'),
    ('statut_contrat','libelle','actif'),('statut_contrat','libelle','terminé'),
    ('statut_employe','libelle','ACTIF'),('statut_employe','libelle','INACTIF'),
    ('statut_employe','libelle','EN_ONBOARDING'),
    ('type_evaluation','libelle','TEST_TECHNIQUE'),('type_evaluation','libelle','ENTRETIEN_RH'),
    ('type_evaluation','libelle','ENTRETIEN_MANAGER'),
    ('type_notification','libelle','INFO'),('type_notification','libelle','ALERTE'),
    ('statut_tache_onboarding','libelle','à faire'),('statut_tache_onboarding','libelle','en cours'),
    ('statut_tache_onboarding','libelle','terminée'),
    ('type_document','libelle','Contrat signé'),('type_document','libelle','Pièce d''identité'),
    ('type_document','libelle','RIB'),('type_document','libelle','Autre')
  ) AS v(t,c,val) LOOP
    EXECUTE format('INSERT INTO %I (%I) SELECT %L WHERE NOT EXISTS (SELECT 1 FROM %I WHERE %I = %L)',
                   r.t, r.c, r.val, r.t, r.c, r.val);
  END LOOP;
END $$;

INSERT INTO type_decompte (code, libelle)
SELECT v.code, v.libelle
FROM (VALUES ('HEURES','Décompte en heures'),('JOURS','Décompte en jours')) AS v(code, libelle)
WHERE NOT EXISTS (SELECT 1 FROM type_decompte t WHERE t.code = v.code);
