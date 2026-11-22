INSERT INTO statut_proposition (libelle) VALUES
('EN_ATTENTE'),
('SOUMISE'),
('APPROUVEE'),
('ACCEPTE'),
('REFUSE'),
('EXPIREE')
ON CONFLICT (libelle) DO NOTHING;