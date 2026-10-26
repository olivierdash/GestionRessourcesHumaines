CREATE TABLE onboarding_tache(
    id_tache SERIAL PRIMARY KEY,
    id_onboarding INTEGER NOT NULL,
    titre VARCHAR(255) NOT NULL,
    id_responsable INTEGER NOT NULL,
    date_echeance DATE,
    est_obligatoire BOOLEAN NOT NULL DEFAULT TRUE,
    id_statut_tache_onboarding INTEGER NOT NULL,
    FOREIGN KEY (id_onboarding) REFERENCES onboarding(id_onboarding) ON DELETE CASCADE,
    FOREIGN KEY (id_responsable) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_statut_tache_onboarding) REFERENCES statut_tache_onboarding(id_statut_tache_onboarding)
);