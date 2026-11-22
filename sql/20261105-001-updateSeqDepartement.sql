-- Après ton INSERT INTO poste ... VALUES (1, ...), ..., (9, ...)
SELECT setval('departement_id_departement_seq', (SELECT MAX(id_departement) FROM departement));