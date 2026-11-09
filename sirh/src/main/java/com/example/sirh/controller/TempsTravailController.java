package com.example.sirh.controller;

import com.example.sirh.model.RegimeTravail;
import com.example.sirh.model.Pointage;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.TempsTravailService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Controller
@RequestMapping("/temps-travail")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG')")
public class TempsTravailController {
    private final TempsTravailService service;
    private final EmployeService employeService;

    @GetMapping("/regimes")
    public String regimes(Model model) {
        model.addAttribute("regimes", service.regimes());
        return "temps-travail/regimes";
    }

    @GetMapping("/regimes/nouveau")
    public String nouveauRegime(Model model) {
        model.addAttribute("regime", new RegimeTravail());
        return "temps-travail/regime-form";
    }

    @PostMapping("/regimes")
    public String enregistrerRegime(@RequestParam(required = false) Integer id,
                                    @RequestParam String libelle,
                                    @RequestParam(required = false) String description,
                                    @RequestParam Integer heuresHebdomadaires,
                                    @RequestParam Integer joursHebdomadaires,
                                    RedirectAttributes ra) {
        try {
            RegimeTravail regime = service.enregistrerRegime(id, libelle, description,
                    heuresHebdomadaires, joursHebdomadaires);
            return "redirect:/temps-travail/regimes/" + regime.getId() + "/edit";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/temps-travail/regimes/nouveau";
        }
    }

    @GetMapping("/regimes/{id}/edit")
    public String modifierRegime(@PathVariable Integer id, Model model) {
        model.addAttribute("regime", service.regime(id));
        model.addAttribute("jours", Arrays.asList(DayOfWeek.values()));
        return "temps-travail/regime-form";
    }

    @PostMapping("/regimes/{id}/horaires")
    public String enregistrerHoraire(@PathVariable Integer id,
                                     @RequestParam DayOfWeek jour,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDebut,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureFin,
                                     @RequestParam Integer pauseMinutes,
                                     RedirectAttributes ra) {
        try {
            service.enregistrerHoraire(id, jour, heureDebut, heureFin, pauseMinutes);
            ra.addFlashAttribute("succes", "Horaire enregistré.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/regimes/" + id + "/edit";
    }

    @GetMapping("/plannings")
    public String plannings(Model model) {
        model.addAttribute("plannings", service.plannings());
        return "temps-travail/plannings";
    }

    @GetMapping("/plannings/nouveau")
    public String nouveauPlanning(Model model) {
        model.addAttribute("planning", null);
        model.addAttribute("regimes", service.regimes());
        return "temps-travail/planning-form";
    }

    @PostMapping("/plannings")
    public String enregistrerPlanning(@RequestParam(required = false) Integer id,
                                      @RequestParam String libelle,
                                      @RequestParam Integer regimeId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                      RedirectAttributes ra) {
        try {
            service.enregistrerPlanning(id, libelle, regimeId, dateDebut, dateFin);
            ra.addFlashAttribute("succes", "Planning enregistré.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/plannings";
    }

    @GetMapping("/plannings/{id}")
    public String detailPlanning(@PathVariable Integer id, Model model) {
        model.addAttribute("planning", service.planning(id));
        model.addAttribute("affectations", service.affectations(id));
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/planning-detail";
    }

    @PostMapping("/plannings/{id}/affectations")
    public String affecterPlanning(@PathVariable Integer id,
                                   @RequestParam Integer employeId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                   RedirectAttributes ra) {
        try {
            service.affecterPlanning(id, employeId, dateDebut, dateFin);
            ra.addFlashAttribute("succes", "Planning affecté au salarié.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/plannings/" + id;
    }

    @GetMapping("/pointages")
    public String pointages(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Integer employeId,
            Model model) {
        LocalDate debut = dateDebut == null ? LocalDate.now().withDayOfMonth(1) : dateDebut;
        LocalDate fin = dateFin == null ? debut.withDayOfMonth(debut.lengthOfMonth()) : dateFin;
        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        model.addAttribute("pointages", service.pointages(debut, fin, employeId));
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("dateDebut", debut);
        model.addAttribute("dateFin", fin);
        model.addAttribute("employeId", employeId);
        return "temps-travail/pointages";
    }

    @GetMapping("/pointages/nouveau")
    public String nouveauPointage(Model model) {
        model.addAttribute("pointage", Pointage.builder().datePointage(LocalDate.now()).build());
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/pointage-form";
    }

    @GetMapping("/pointages/{id}/edit")
    public String modifierPointage(@PathVariable Integer id, Model model) {
        model.addAttribute("pointage", service.pointage(id));
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/pointage-form";
    }

    @PostMapping("/pointages")
    public String enregistrerPointage(
            @RequestParam(required = false) Integer id,
            @RequestParam Integer employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePointage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureEntree,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureSortie,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes ra) {
        try {
            service.enregistrerPointage(id, employeId, datePointage, heureEntree, heureSortie, commentaire);
            ra.addFlashAttribute("succes", "Pointage enregistré et calculé.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/pointages";
    }
}
