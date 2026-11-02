CREATE TABLE type_document(
    id_type_document SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO type_document(libelle)
VALUES ('Contrat signé'), ('Pièce d''identité'), ('RIB'), ('Autre');