package com.example.sirh.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.AbsenceService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.PlanningCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/calendrier")
@RequiredArgsConstructor
public class CalendrierCongeController {

    private final CongeAccessService accessService;
    private final EmployeService employeService;
    private final AbsenceService absenceService;
    private final PlanningCongeService planningCongeService;

    @GetMapping
    public String calendrier(@RequestParam(required = false) String periode, Model model) {
        YearMonth mois = periode == null || periode.isBlank()
                ? YearMonth.now() : YearMonth.parse(periode);
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();

        Utilisateur utilisateur = accessService.utilisateurCourant();
        List<Employe> employes = employesVisibles(utilisateur);
        List<Integer> employeIds = employes.stream().map(Employe::getIdEmployer).toList();

        List<Absence> absences = absenceService.absencesPeriode(employeIds, debut, fin);
        List<DemandeConge> demandes = planningCongeService.demandesEnAttentePeriode(debut, fin)
                .stream()
                .filter(d -> d.getEmploye() != null
                        && employeIds.contains(d.getEmploye().getIdEmployer()))
                .toList();

        Map<LocalDate, List<Absence>> absencesParJour = new LinkedHashMap<>();
        for (LocalDate jour = debut; !jour.isAfter(fin); jour = jour.plusDays(1)) {
            List<Absence> duJour = new ArrayList<>();
            for (Absence absence : absences) {
                if (!absence.getDateDebut().isAfter(jour) && !absence.getDateFin().isBefore(jour)) {
                    duJour.add(absence);
                }
            }
            absencesParJour.put(jour, duJour);
        }

        Map<LocalDate, List<DemandeConge>> demandesParJour = new LinkedHashMap<>();
        for (LocalDate jour = debut; !jour.isAfter(fin); jour = jour.plusDays(1)) {
            List<DemandeConge> duJour = new ArrayList<>();
            for (DemandeConge demande : demandes) {
                if (!demande.getDateDebut().isAfter(jour) && !demande.getDateFin().isBefore(jour)) {
                    duJour.add(demande);
                }
            }
            demandesParJour.put(jour, duJour);
        }

        int offset = debut.getDayOfWeek().getValue() - 1;
        int joursDansMois = fin.getDayOfMonth();
        int semaines = (int) Math.ceil((offset + joursDansMois) / 7.0);

        model.addAttribute("periode", mois.toString());
        model.addAttribute("mois", mois);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("offset", offset);
        model.addAttribute("joursDansMois", joursDansMois);
        model.addAttribute("semaines", semaines);
        model.addAttribute("jours", absencesParJour.keySet());
        model.addAttribute("absencesParJour", absencesParJour);
        model.addAttribute("demandesParJour", demandesParJour);
        model.addAttribute("employes", employes);
        model.addAttribute("precedent", mois.minusMonths(1).toString());
        model.addAttribute("suivant", mois.plusMonths(1).toString());
        return "conges/calendrier";
    }

    private List<Employe> employesVisibles(Utilisateur utilisateur) {
        List<Employe> tous = employeService.getAllEmployesList();
        if (accessService.estRhOuDg(utilisateur) || accessService.estAdmin(utilisateur)) {
            return tous;
        }
        if (!accessService.estManager(utilisateur) || utilisateur.getEmploye() == null) {
            return List.of();
        }
        var departement = accessService.departementDeLEmploye(utilisateur.getEmploye());
        if (departement.isEmpty()) {
            return List.of();
        }
        Integer departementId = departement.get().getIdDepartement();
        return tous.stream()
                .filter(e -> accessService.departementDeLEmploye(e)
                        .map(d -> departementId.equals(d.getIdDepartement()))
                        .orElse(false))
                .toList();
    }
}
