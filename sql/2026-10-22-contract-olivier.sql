CREATE TABLE statut_contrat(
    id_statut_contrat SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO statut_contrat(libelle) VALUES ('actif'), ('terminé');

ALTER TABLE contract
    ADD COLUMN id_statut_contrat INT REFERENCES statut_contrat(id_statut_contrat);

UPDATE contract SET id_statut_contrat =
    (SELECT id_statut_contrat FROM statut_contrat WHERE libelle = 'actif')
WHERE status = 'actif';

-- (même chose pour 'terminé')

ALTER TABLE contract ALTER COLUMN id_statut_contrat SET NOT NULL;
ALTER TABLE contract DROP COLUMN status;