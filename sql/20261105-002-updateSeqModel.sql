-- Après ton INSERT INTO poste ... VALUES (1, ...), ..., (9, ...)
SELECT setval('poste_id_poste_seq', (SELECT MAX(id_poste) FROM poste));