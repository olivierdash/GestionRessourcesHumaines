-- Migration SQL manuelle : colonnes attendues par les entités JPA mais
-- absentes du dump actuel de la base.
-- Exécuter une seule fois (les ADD COLUMN IF NOT EXISTS rendent le script rejouable).
BEGIN;

ALTER TABLE type_conge
    ADD COLUMN IF NOT EXISTS mode_decompte VARCHAR(20) NOT NULL DEFAULT 'OUVRES',
    ADD COLUMN IF NOT EXISTS paye BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS delai_justificatif_jours INTEGER;

-- Les services calculent et renseignent ces durées lors des nouvelles écritures.
-- Les colonnes restent nullables pour ne pas inventer de durée pour l'historique.
ALTER TABLE demande_conge
    ADD COLUMN IF NOT EXISTS nombre_jours NUMERIC(7,2);

ALTER TABLE absence
    ADD COLUMN IF NOT EXISTS nombre_jours NUMERIC(7,2),
    ADD COLUMN IF NOT EXISTS date_limite_justificatif DATE;

COMMIT;
