package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;

class CalendrierCongeTemplateTest {

    @Test
    void leCalendrierRendLesSemainesEtLesDialoguesSansEvenements() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/resources/templates/conges/calendrier.html"));
        int debutTableau = source.indexOf("<tbody>");
        int finTableau = source.indexOf("</tbody>", debutTableau) + "</tbody>".length();
        String fragment = source.substring(debutTableau, finTableau);

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        YearMonth mois = YearMonth.of(2026, 11);
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();
        Context context = new Context(Locale.FRENCH);
        context.setVariable("semaines", 6);
        context.setVariable("offset", debut.getDayOfWeek().getValue() - 1);
        context.setVariable("joursDansMois", fin.getDayOfMonth());
        context.setVariable("debut", debut);
        Map<Integer, List<String>> evenementsParJour = new LinkedHashMap<>();
        List<LocalDate> jours = new ArrayList<>();
        for (LocalDate jour = debut; !jour.isAfter(fin); jour = jour.plusDays(1)) {
            jours.add(jour);
            evenementsParJour.put(jour.getDayOfMonth(), List.of());
        }
        context.setVariable("absencesParJour", evenementsParJour);
        context.setVariable("demandesParJour", evenementsParJour);

        String rendu = engine.process(fragment, context);
        int lignesSemaines = rendu.split("<tr", -1).length - 1;

        assertEquals(6, lignesSemaines);

        context.setVariable("jours", jours);
        int debutDialogues = source.indexOf("<div th:each=\"jour : ${jours}\"");
        int finDialogues = source.indexOf(
                "<div class=\"grid grid-cols-1 md:grid-cols-2 gap-4\">", debutDialogues);
        String fragmentDialogues = source.substring(debutDialogues, finDialogues);
        String renduDialogues = engine.process(fragmentDialogues, context);

        assertEquals(30, renduDialogues.split("<div", -1).length - 1);
    }
}
