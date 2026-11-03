CREATE TABLE document(
    id_document SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_type_document INTEGER NOT NULL,
    id_tache INTEGER NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    fichier_url VARCHAR(500) NOT NULL,
    date_depot TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_depose_par INTEGER NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer),
    FOREIGN KEY (id_type_document) REFERENCES type_document(id_type_document),
    FOREIGN KEY (id_tache) REFERENCES onboarding_tache(id_tache),
    FOREIGN KEY (id_depose_par) REFERENCES utilisateur(id_utilisateur)
);