-- Départements, postes et grille salariale (rejouable)
-- Nécessite 20261114-001 (lieux) et 20261114-002 (bob@sirh.com)
INSERT INTO departement (titre, id_manager)
SELECT v.titre, (SELECT id_utilisateur FROM utilisateur WHERE email = 'bob@sirh.com')
FROM (VALUES
  ('Informatique'),('Data & Analytics'),('Infrastructure & Sécurité'),
  ('Design & UX'),('Gestion de Projet'),('Finance & Comptabilité'),
  ('Ressources Humaines'),('Commercial & Ventes'),('Marketing & Communication'),
  ('Juridique'),('Achats & Logistique'),('Industrie & Production'),
  ('BTP & Construction'),('Restauration & Hôtellerie'),
  ('Retail & Distribution'),('Administration & Support')
) AS v(titre)
WHERE NOT EXISTS (SELECT 1 FROM departement d WHERE d.titre = v.titre);

INSERT INTO poste (id_departement, date_effet, intitule, id_lieu, duree_hebdo)
SELECT d.id_departement, CURRENT_DATE, v.intitule, l.id_lieu, 40
FROM (VALUES
  ('Informatique',              'Développeur Java'),
  ('Informatique',              'Développeur Front-end'),
  ('Data & Analytics',          'Data Analyst'),
  ('Infrastructure & Sécurité', 'Administrateur Systèmes'),
  ('Design & UX',               'Designer UX'),
  ('Gestion de Projet',         'Chef de projet'),
  ('Finance & Comptabilité',    'Comptable'),
  ('Ressources Humaines',       'Chargé de recrutement'),
  ('Commercial & Ventes',       'Commercial')
) AS v(departement, intitule)
JOIN departement d ON d.titre = v.departement
JOIN lieu l ON l.libelle = 'Antananarivo'
WHERE NOT EXISTS (
  SELECT 1 FROM poste p WHERE p.intitule = v.intitule AND p.id_departement = d.id_departement
);

INSERT INTO grille_salarial (id_poste, id_departement, salaire_min, salaire_max)
SELECT p.id_poste, p.id_departement, 800000, 3000000
FROM poste p
WHERE p.intitule IN ('Développeur Java','Développeur Front-end','Data Analyst','Administrateur Systèmes','Designer UX','Chef de projet','Comptable','Chargé de recrutement','Commercial')
  AND NOT EXISTS (SELECT 1 FROM grille_salarial g WHERE g.id_poste = p.id_poste);
