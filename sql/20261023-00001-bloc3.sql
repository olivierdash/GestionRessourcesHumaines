CREATE SEQUENCE IF NOT EXISTS matricule_seq START 1;

CREATE TABLE IF NOT EXISTS affectation (
    id_affectation SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);
