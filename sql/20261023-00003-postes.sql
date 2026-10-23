INSERT INTO poste (id_departement, date_effet, intitule, lieu, duree_hebdo) VALUES
(1, CURRENT_DATE, 'Développeur Java', 'Antananarivo', 40),
(1, CURRENT_DATE, 'Développeur Front-end', 'Antananarivo', 40),
(2, CURRENT_DATE, 'Data Analyst', 'Antananarivo', 40),
(3, CURRENT_DATE, 'Administrateur Systèmes', 'Antananarivo', 40),
(4, CURRENT_DATE, 'Designer UX', 'Antananarivo', 40),
(5, CURRENT_DATE, 'Chef de projet', 'Antananarivo', 40),
(6, CURRENT_DATE, 'Comptable', 'Antananarivo', 40),
(7, CURRENT_DATE, 'Chargé de recrutement', 'Antananarivo', 40),
(8, CURRENT_DATE, 'Commercial', 'Antananarivo', 40);

INSERT INTO grille_salarial (id_poste, id_departement, salaire_min, salaire_max, salaire_moyen)
SELECT id_poste, id_departement, 800000, 3000000, 1800000 FROM poste;
