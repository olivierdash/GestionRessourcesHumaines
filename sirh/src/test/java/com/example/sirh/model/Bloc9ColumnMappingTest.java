package com.example.sirh.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

class Bloc9ColumnMappingTest {

    private static final String MODEL_PACKAGE = "com.example.sirh.model.";

    private static final Map<String, Set<String>> TABLE_COLUMNS = Map.ofEntries(
            Map.entry("Absence", Set.of("id_absence", "id_employer", "id_type_conge",
                    "id_demande_conge", "source", "date_debut", "date_fin", "demi_jour_debut",
                    "demi_jour_fin", "nombre_jours", "statut", "motif", "id_utilisateur_createur",
                    "date_limite_justificatif", "date_creation", "date_maj", "version")),
            Map.entry("Objectif", Set.of("id_objectif_evaluation", "id_employer", "titre",
                    "description", "date_debut", "date_fin", "poids", "statut", "id_createur",
                    "date_creation", "date_maj")),
            Map.entry("CritereEvaluation", Set.of("id_critere_evaluation", "code", "libelle",
                    "description", "echelle_min", "echelle_max", "poids", "actif",
                    "date_creation", "date_maj")),
            Map.entry("ObjectifCritereEvaluation", Set.of("id_objectif_evaluation",
                    "id_critere_evaluation", "poids")),
            Map.entry("ResultatCritereEvaluation", Set.of("id_resultat_critere", "id_objectif_evaluation",
                    "id_critere_evaluation", "id_evaluateur", "note", "commentaire", "date_evaluation")),
            Map.entry("Competence", Set.of("id_competence", "code", "libelle", "description",
                    "niveau_max", "actif", "date_creation", "date_maj")),
            Map.entry("CompetenceAttendue", Set.of("id_competence_attendue", "id_employer", "id_poste",
                    "id_competence", "niveau_attendu", "date_debut", "date_fin", "date_creation", "date_maj")),
            Map.entry("EmployeCompetence", Set.of("id_competence_acquise", "id_employer", "id_competence",
                    "niveau_acquis", "date_acquisition", "id_evaluateur", "source_evaluation",
                    "id_evaluation", "commentaire", "date_creation", "date_maj")),
            Map.entry("EcartCompetence", Set.of("id_ecart_competence", "id_employer", "id_competence",
                    "niveau_attendu", "niveau_acquis", "ecart", "niveau_ecart", "date_analyse",
                    "commentaire", "id_analyste", "date_creation")),
            Map.entry("BesoinFormation", Set.of("id_besoin_formation", "id_employer", "id_competence",
                    "id_ecart_competence", "titre", "description", "priorite", "statut",
                    "date_identification", "date_souhaitee", "id_demandeur", "id_validateur",
                    "date_validation", "commentaire_validation", "date_creation", "date_maj")),
            Map.entry("DeclenchementBesoinFormation", Set.of("id_declenchement", "id_besoin_formation",
                    "motif", "message", "id_utilisateur", "date_declenchement")),
            Map.entry("HistoriqueBesoinFormation", Set.of("id_historique_besoin", "id_besoin_formation",
                    "statut_avant", "statut_apres", "commentaire", "id_utilisateur", "date_changement")),
            Map.entry("Formation", Set.of("id_formation", "id_besoin_formation", "titre", "organisme",
                    "mode_formation", "date_debut", "date_fin", "duree_heures", "statut", "commentaire",
                    "date_creation", "date_maj")),
            Map.entry("ParticipationFormation", Set.of("id_participation", "id_formation", "id_employer",
                    "statut", "taux_presence", "note_finale", "certificat_obtenu", "commentaire",
                    "date_participation", "date_maj")),
            Map.entry("MesureEvolutionFormation", Set.of("id_mesure_evolution", "id_participation",
                    "id_competence", "niveau_avant", "niveau_apres", "niveau_attendu", "progression",
                    "taux_atteinte", "mesure", "date_mesure_avant", "date_mesure_apres", "commentaire",
                    "id_evaluateur", "date_creation", "date_maj")),
            Map.entry("HoraireTravail", Set.of("id_horaire_travail", "id_regime_travail", "dayweek",
                    "heure_debut", "heure_fin", "pause_minutes")),
            Map.entry("TypeConge", Set.of("id_type_conge", "code", "libelle", "description", "categorie",
                    "mode_decompte", "paye", "consomme_solde", "justificatif_requis",
                    "delai_justificatif_jours", "duree_max_jours", "report_autorise", "plafond_report_jours",
                    "demande_employe_autorisee", "actif", "ordre_affichage", "couleur", "date_creation",
                    "date_maj")),
            Map.entry("DemandeConge", Set.of("id_demande_conge", "reference", "id_employer",
                    "id_type_conge", "id_utilisateur_createur", "date_debut", "date_fin", "demi_jour_debut",
                    "demi_jour_fin", "nombre_jours", "solde_requis", "motif", "statut", "manager_requis",
                    "id_manager_attendu", "id_validateur_manager", "date_validation_manager",
                    "commentaire_manager", "id_validateur_rh", "date_validation_rh", "commentaire_rh",
                    "date_limite_justificatif", "date_soumission", "date_creation", "date_maj", "version")));

    @Test
    void mapsEveryChangedMigrationTableAndColumnFromTheSqlScripts() {
        assertAll(TABLE_COLUMNS.entrySet().stream()
                .map(entry -> () -> assertMapping(entry.getKey(), entry.getValue())));
    }

    private void assertMapping(String modelName, Set<String> expectedColumns) {
        Class<?> model = assertDoesNotThrow(() -> Class.forName(MODEL_PACKAGE + modelName));
        assertNotNull(model.getAnnotation(Entity.class), modelName + " must be a JPA entity");
        Table table = model.getAnnotation(Table.class);
        assertNotNull(table, modelName + " must declare its table");
        assertEquals(tableName(modelName), table.name(), modelName + " table name");
        assertEquals(expectedColumns, mappedColumns(model), modelName + " columns");
    }

    private String tableName(String modelName) {
        return switch (modelName) {
            case "Absence" -> "absence";
            case "Objectif" -> "objectif_evaluation";
            case "CritereEvaluation" -> "critere_evaluation";
            case "ObjectifCritereEvaluation" -> "objectif_critere_evaluation";
            case "ResultatCritereEvaluation" -> "resultat_critere_evaluation";
            case "Competence" -> "competence";
            case "CompetenceAttendue" -> "competence_attendue";
            case "EmployeCompetence" -> "competence_acquise";
            case "EcartCompetence" -> "ecart_competence";
            case "BesoinFormation" -> "besoin_formation";
            case "DeclenchementBesoinFormation" -> "declenchement_besoin_formation";
            case "HistoriqueBesoinFormation" -> "historique_besoin_formation";
            case "Formation" -> "formation";
            case "ParticipationFormation" -> "participation_formation";
            case "MesureEvolutionFormation" -> "mesure_evolution_formation";
            case "HoraireTravail" -> "horaire_travail";
            case "TypeConge" -> "type_conge";
            case "DemandeConge" -> "demande_conge";
            default -> throw new IllegalArgumentException("Unknown model: " + modelName);
        };
    }

    private Set<String> mappedColumns(Class<?> model) {
        return Arrays.stream(model.getDeclaredFields())
                .flatMap(field -> mappedColumns(field).stream())
                .collect(Collectors.toSet());
    }

    private Set<String> mappedColumns(Field field) {
        Set<String> columns = Stream.of(field.getAnnotation(Column.class), field.getAnnotation(JoinColumn.class))
                .filter(annotation -> annotation != null)
                .map(annotation -> annotation instanceof Column column
                        ? column.name() : ((JoinColumn) annotation).name())
                .map(name -> name.isBlank() ? field.getName() : name)
                .collect(Collectors.toSet());
        if (field.isAnnotationPresent(EmbeddedId.class)) {
            columns.addAll(mappedColumns(field.getType()));
        }
        return columns;
    }
}
