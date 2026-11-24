package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

class StatutBadgeFiltreTemplateTest {

    @Test
    void leBadgeDabsenceEstUnLienDeFiltreEtConserveLesAutresCriteres() throws IOException {
        assertBadgeFiltrable(
                "conges/absences/index.html",
                "a.statut",
                "/conges/absences",
                List.of(
                        "employeId=${filter.employeId}",
                        "typeId=${filter.typeId}",
                        "statut=${a.statut}",
                        "debut=${filter.debut}",
                        "fin=${filter.fin}"));
    }

    @Test
    void leBadgeDeDemandeEstUnLienDeFiltreEtConserveLesAutresCriteres() throws IOException {
        assertBadgeFiltrable(
                "conges/demandes/index.html",
                "demande.statut",
                "/conges/demandes",
                List.of(
                        "employeId=${filter.employeId}",
                        "typeId=${filter.typeId}",
                        "statut=${demande.statut}",
                        "debut=${filter.debut}",
                        "fin=${filter.fin}"));
    }

    @Test
    void leBadgeDeValidationEstUnLienDeFiltreDansSaCellule() throws IOException {
        assertBadgeFiltrable(
                "conges/validations/index.html",
                "demande.statut",
                "/conges/validations",
                List.of("statut=${demande.statut}"));
    }

    @Test
    void leBadgeDeMesDemandesEstUnLienDeFiltreDansSaCellule() throws IOException {
        assertBadgeFiltrable(
                "conges/mes/demandes.html",
                "demande.statut",
                "/mes-conges/demandes",
                List.of("statut=${demande.statut}"));
    }

    @Test
    void leBadgeDuTableauDeBordOuvreLesDemandesAvecLeStatutSelectionne() throws IOException {
        assertBadgeFiltrable(
                "conges/mes/index.html",
                "demande.statut",
                "/mes-conges/demandes",
                List.of("statut=${demande.statut}"));
    }

    @Test
    void leBadgeDeMesAbsencesFiltreLhistoriqueDansSaCellule() throws IOException {
        assertBadgeFiltrable(
                "conges/mes/absences.html",
                "a.statut",
                "/mes-conges/absences",
                List.of("statut=${a.statut}"));
    }

    @Test
    void leBadgeDeDemandeDansLeDetailOuvreLaListeFiltree() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", "conges/demandes/detail.html")).replaceAll("\\s+", " ");

        assertTrue(source.contains("<a th:href=\"@{${filtreStatutBase}(statut=${demande.statut})}\""),
                "Le badge du détail doit ouvrir la liste de demandes filtrée");
        assertTrue(source.contains("<span th:replace=\"~{conges/fragments/statut :: badge(${demande.statut})}\"></span> </a>"),
                "Le badge du détail doit être contenu dans le lien de filtre");
    }

    @Test
    void leBadgeDabsenceDansLeDetailOuvreLaListeFiltree() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", "conges/absences/detail.html")).replaceAll("\\s+", " ");

        assertTrue(source.contains("<a th:href=\"@{/conges/absences(statut=${absence.statut})}\""),
                "Le badge du détail doit ouvrir la liste d'absences filtrée");
        assertTrue(source.contains("<span th:replace=\"~{conges/fragments/statut :: badge(${absence.statut})}\"></span> </a>"),
                "Le badge du détail doit être contenu dans le lien de filtre");
    }

    @Test
    void leLienDuStatutDansLeDetailGenereUneUrlDeDemandeFiltrees() throws IOException {
        String rendu = rendreLienStatut(
                "conges/demandes/detail.html",
                "th:href=\"@{${filtreStatutBase}(statut=${demande.statut})}\"",
                "demande.statut",
                Map.of("filtreStatutBase", "/mes-conges/demandes",
                        "demande", Map.of("statut", "REFUSEE")));

        assertTrue(rendu.contains("href=\"/mes-conges/demandes?statut=REFUSEE\""));
    }

    @Test
    void leLienDuStatutDansLeDetailGenereUneUrlDabsenceFiltree() throws IOException {
        String rendu = rendreLienStatut(
                "conges/absences/detail.html",
                "th:href=\"@{/conges/absences(statut=${absence.statut})}\"",
                "absence.statut",
                Map.of("absence", Map.of("statut", "ANNULEE")));

        assertTrue(rendu.contains("href=\"/conges/absences?statut=ANNULEE\""));
    }

    @Test
    void leStatutDincidentEstUnLienVersLeFiltreExistant() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", "discipline/index.html")).replaceAll("\\s+", " ");

        assertTrue(source.contains("<td> <a th:href=\"@{/discipline(statut=${incident.statutInstruction})}\""));
        assertTrue(source.contains("th:if=\"${statutFiltre != null}\" th:href=\"@{/discipline}\""));
    }

    @Test
    void leStatutDeRuptureEstUnLienVersUnFiltreReinitialisable() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", "offboarding/index.html")).replaceAll("\\s+", " ");

        assertTrue(source.contains("<td> <a th:href=\"@{/offboarding(statut=${r.statutRupture})}\""));
        assertTrue(source.contains("<div th:if=\"${statutFiltre != null}\""));
        assertTrue(source.contains("<a th:href=\"@{/offboarding}\""));
    }

    private static String rendreLienStatut(String templatePath, String hrefExpression,
            String statutExpression, Map<String, Object> variables) throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", templatePath)).replaceAll("\\s+", " ");
        int debutLien = source.indexOf("<a " + hrefExpression);
        int finLien = source.indexOf("</a>", debutLien) + "</a>".length();
        String badge = "<span th:replace=\"~{conges/fragments/statut :: badge(${"
                + statutExpression + "})}\"></span>";
        String fragment = source.substring(debutLien, finLien).replace(badge, "badge");

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        var webExchange = JakartaServletWebApplication.buildApplication(servletContext)
                .buildExchange(request, response);
        WebContext context = new WebContext(webExchange, Locale.FRENCH, variables);
        return engine.process(fragment, context);
    }

    private static void assertBadgeFiltrable(String templatePath, String statutExpression,
            String route, List<String> preservedParameters) throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates", templatePath)).replaceAll("\\s+", " ");
        String badgeMarker = "th:replace=\"~{conges/fragments/statut :: badge(${"
                + statutExpression + "})}\"";
        int markerIndex = source.indexOf(badgeMarker);
        assertTrue(markerIndex >= 0, "Le badge de statut doit être présent dans " + templatePath);

        int cellStart = source.lastIndexOf("<td", markerIndex);
        int cellEnd = source.indexOf("</td>", markerIndex);
        assertTrue(cellStart >= 0 && cellEnd >= 0,
                "Le badge doit rester dans une cellule de tableau dans " + templatePath);
        String cell = source.substring(cellStart, cellEnd + "</td>".length());

        assertAll(
                () -> assertTrue(cell.startsWith("<td>"),
                        "La cellule doit conserver son élément td dans " + templatePath),
                () -> assertTrue(cell.contains("<a th:href=\"@{" + route + "("),
                        "Le badge doit pointer vers le filtre de statut dans " + templatePath),
                () -> preservedParameters.forEach(parameter -> assertTrue(cell.contains(parameter),
                        "Le lien doit conserver le paramètre " + parameter + " dans " + templatePath)));
    }
}
