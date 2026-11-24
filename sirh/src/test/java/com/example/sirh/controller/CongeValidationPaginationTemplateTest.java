package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CongeValidationPaginationTemplateTest {

    @Test
    void afficheLesElementsEtLesControlesDePaginationEnConservantLeFiltre() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", "conges/validations/index.html")).replaceAll("\\s+", " ");

        assertAll(
                () -> assertTrue(source.contains("demandes.totalElements : enAttente"),
                        "Le compteur doit utiliser le total de la page filtrée"),
                () -> assertTrue(source.contains("${#lists.isEmpty(demandes.content)}"),
                        "L'état vide doit vérifier le contenu de la page"),
                () -> assertTrue(source.contains("th:each=\"demande : ${demandes.content}\""),
                        "Le tableau doit afficher uniquement le contenu de la page"),
                () -> assertTrue(source.contains("<form th:action=\"@{/conges/validations}\" method=\"get\" class="),
                        "La navigation doit repasser par la liste de validation"),
                () -> assertTrue(source.contains("th:if=\"${filtreStatut != null}\" type=\"hidden\" name=\"statut\" th:value=\"${filtreStatut}\""),
                        "La navigation doit conserver le filtre de statut"),
                () -> assertTrue(source.contains("name=\"page\" th:value=\"${demandes.number - 1}\""),
                        "Un contrôle doit permettre de revenir à la page précédente"),
                () -> assertTrue(source.contains("name=\"page\" th:value=\"${demandes.number + 1}\""),
                        "Un contrôle doit permettre d'avancer à la page suivante"),
                () -> assertTrue(source.contains("${demandes.totalPages}"),
                        "La navigation doit afficher le nombre total de pages"));
    }
}
