CREATE TABLE statut_tache_onboarding(
    id_statut_tache_onboarding SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO statut_tache_onboarding(libelle)
VALUES ('à faire'), ('en cours'), ('terminée');