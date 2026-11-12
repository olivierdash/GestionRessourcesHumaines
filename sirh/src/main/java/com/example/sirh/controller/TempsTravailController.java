package com.example.sirh.controller;

import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.dto.PointageFilter;
import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.HoraireTravail;
import com.example.sirh.model.Planning;
import com.example.sirh.model.Pointage;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.service.AffectationPlanningService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.PlanningService;
import com.example.sirh.service.PointageService;
import com.example.sirh.service.RegimeTravailService;
import com.example.sirh.service.TypeDecompteService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/temps-travail")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG')")
public class TempsTravailController {

    private final RegimeTravailService regimeTravailService;
    private final PlanningService planningService;
    private final AffectationPlanningService affectationPlanningService;
    private final PointageService pointageService;
    private final TypeDecompteService typeDecompteService;
    private final EmployeService employeService;

    // =========================================================
    // RÉGIME DE TRAVAIL
    // =========================================================

    @GetMapping("/regimes")
    public String regimes(Model model) {
        model.addAttribute("regimes", regimeTravailService.getAll());
        return "temps-travail/regimes/index";
    }

    @GetMapping("/regimes/nouveau")
    public String nouveauRegime(@ModelAttribute("regime") RegimeTravail regime, Model model) {
        model.addAttribute("typeDecomptes", typeDecompteService.getAll());
        return "temps-travail/regimes/form";
    }

    @PostMapping("/regimes")
    public String enregistrerRegime(@ModelAttribute("regime") RegimeTravail regime,
            RedirectAttributes ra) {
        try {
            RegimeTravail saved = regimeTravailService.enregistrer(regime);
            ra.addFlashAttribute("succes", "Régime enregistré.");
            return "redirect:/temps-travail/regimes/" + saved.getId() + "/edit";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            ra.addFlashAttribute("regime", regime);
            return "redirect:/temps-travail/regimes/nouveau";
        }
    }

    @GetMapping("/regimes/{id}/edit")
    public String modifierRegime(@PathVariable Integer id, Model model) {
        RegimeTravail regime = regimeTravailService.getById(id);

        Map<Integer, HoraireTravail> horairesParJour = new HashMap<>();
        for (HoraireTravail h : regime.getHoraires()) {
            horairesParJour.put(h.getDayweek(), h);
        }

        Map<Integer, String> jours = new LinkedHashMap<>();
        jours.put(1, "Lundi");
        jours.put(2, "Mardi");
        jours.put(3, "Mercredi");
        jours.put(4, "Jeudi");
        jours.put(5, "Vendredi");

        model.addAttribute("regime", regime);
        model.addAttribute("typeDecomptes", typeDecompteService.getAll());
        model.addAttribute("jours", jours);
        model.addAttribute("horairesParJour", horairesParJour);
        return "temps-travail/regimes/form";
    }

    @PostMapping("/regimes/{id}/horaires")
    public String enregistrerHoraire(@PathVariable Integer id,
            @ModelAttribute("horaire") HoraireTravail horaire,
            RedirectAttributes ra) {
        try {
            RegimeTravail stub = new RegimeTravail();
            stub.setId(id);
            horaire.setRegimeTravail(stub);

            regimeTravailService.enregistrerHoraire(horaire);
            ra.addFlashAttribute("succes", "Horaire enregistré.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/regimes/" + id + "/edit";
    }

    // =========================================================
    // PLANNING
    // =========================================================

    @GetMapping("/plannings")
    public String plannings(Model model) {
        model.addAttribute("plannings", planningService.getAll());
        return "temps-travail/plannings/index";
    }

    @GetMapping("/plannings/nouveau")
    public String nouveauPlanning(@ModelAttribute("planning") Planning planning, Model model) {
        model.addAttribute("regimes", regimeTravailService.getAll());
        return "temps-travail/plannings/form";
    }

    @PostMapping("/plannings")
    public String enregistrerPlanning(@ModelAttribute("planning") Planning planning,
            RedirectAttributes ra) {
        try {
            planningService.enregistrer(planning);
            ra.addFlashAttribute("succes", "Planning enregistré.");
            return "redirect:/temps-travail/plannings";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            ra.addFlashAttribute("planning", planning);
            return "redirect:/temps-travail/plannings/nouveau";
        }
    }

    @GetMapping("/plannings/{id}")
    public String detailPlanning(@PathVariable Integer id,
            @ModelAttribute("affectation") AffectationPlanning affectation,
            Model model) {
        model.addAttribute("planning", planningService.getById(id));
        model.addAttribute("affectations", affectationPlanningService.listerParPlanning(id));
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/plannings/detail";
    }

    @PostMapping("/plannings/{id}/affectations")
    public String affecterPlanning(@PathVariable Integer id,
            @ModelAttribute("affectation") AffectationPlanning affectation,
            RedirectAttributes ra) {
        try {
            Planning stub = new Planning();
            stub.setId(id);
            affectation.setPlanning(stub);

            affectationPlanningService.enregistrer(affectation);
            ra.addFlashAttribute("succes", "Planning affecté au salarié.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/temps-travail/plannings/" + id;
    }

    // =========================================================
    // POINTAGE
    // =========================================================

    @GetMapping("/pointages")
    public String pointages(@ModelAttribute("filter") PointageFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 100),
                Sort.by("datePointage").descending().and(Sort.by("id").descending()));

        Page<Pointage> pageResult = pointageService.lister(filter, pageable);

        Map<Integer, PointageCalculDto> calculs = new HashMap<>();
        for (Pointage p : pageResult.getContent()) {
            calculs.put(p.getId(), pointageService.calculer(p));
        }

        model.addAttribute("pointages", pageResult);
        model.addAttribute("calculs", calculs);
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("per_page", List.of(10, 25, 50));
        return "temps-travail/pointages/index";
    }

    @GetMapping("/pointages/nouveau")
    public String nouveauPointage(@ModelAttribute("pointage") Pointage pointage, Model model) {
        if (pointage.getDatePointage() == null) {
            pointage.setDatePointage(LocalDate.now());
        }
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/pointages/form";
    }

    @GetMapping("/pointages/{id}/edit")
    public String modifierPointage(@PathVariable Integer id, Model model) {
        model.addAttribute("pointage", pointageService.getById(id));
        model.addAttribute("employes", employeService.getAllEmployesList());
        return "temps-travail/pointages/form";
    }

    @PostMapping("/pointages")
    public String enregistrerPointage(@ModelAttribute("pointage") Pointage pointage,
            RedirectAttributes ra) {
        try {
            pointageService.enregistrer(pointage);
            ra.addFlashAttribute("succes", "Pointage enregistré.");
            return "redirect:/temps-travail/pointages";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            ra.addFlashAttribute("pointage", pointage);
            if (pointage.getId() != null) {
                return "redirect:/temps-travail/pointages/" + pointage.getId() + "/edit";
            }
            return "redirect:/temps-travail/pointages/nouveau";
        }
    }

    @GetMapping("/pointages/{id}/calcul")
    @ResponseBody
    public PointageCalculDto calculPointage(@PathVariable Integer id) {
        return pointageService.calculer(pointageService.getById(id));
    }
}