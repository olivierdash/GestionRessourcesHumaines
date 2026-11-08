CREATE TABLE historique_candidat_identifiants (
    id_historique                 SERIAL PRIMARY KEY,
    id_candidat                   INTEGER NOT NULL,
    date_changement               TIMESTAMP NOT NULL DEFAULT NOW(),
    ancien_nom                    VARCHAR(255),
    ancien_prenom                 VARCHAR(255),
    ancien_email                  VARCHAR(255),
    CONSTRAINT fk_hist_cand_ident FOREIGN KEY (id_candidat)
        REFERENCES candidat(id_candidat)
);

CREATE INDEX idx_hist_cand_ident
    ON historique_candidat_identifiants(id_candidat, date_changement DESC);
