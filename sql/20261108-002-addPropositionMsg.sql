CREATE TABLE IF NOT EXISTS proposition_message (
    id_message       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_proposition   INTEGER NOT NULL,
    id_candidat      INTEGER,
    id_utilisateur   INTEGER,
    contenu          TEXT,
    salaire_propose  NUMERIC(12,2),
    date_creation    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_msg_proposition FOREIGN KEY (id_proposition)
        REFERENCES proposition_embauche(id_proposition) ON DELETE CASCADE,
    CONSTRAINT fk_msg_candidat FOREIGN KEY (id_candidat)
        REFERENCES candidat(id_candidat) ON DELETE RESTRICT,
    CONSTRAINT fk_msg_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur) ON DELETE RESTRICT,
    CONSTRAINT chk_msg_auteur CHECK (
        (id_candidat IS NOT NULL AND id_utilisateur IS NULL) OR
        (id_candidat IS NULL AND id_utilisateur IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_msg_proposition
    ON proposition_message(id_proposition, date_creation);