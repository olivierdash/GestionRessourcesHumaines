package com.example.sirh.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.dto.AjustementSoldeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.SoldeConge;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.SoldeCongeService;
import com.example.sirh.service.TypeCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/soldes")
@RequiredArgsConstructor
public class SoldeCongeController {

    private final SoldeCongeService soldeCongeService;
    private final EmployeService employeService;
    private final TypeCongeService typeCongeService;
    private final CongeAccessService accessService;

    @GetMapping
    public String index(@RequestParam(required = false) Integer annee,
                        @RequestParam(required = false) Integer employeId,
                        @RequestParam(required = false) Integer typeId,
                        Model model) {
        int anneeCible = annee == null ? LocalDate.now().getYear() : annee;
        var soldes = soldeCongeService.rechercher(anneeCible, employeId, typeId);
        Map<Integer, java.math.BigDecimal> disponibles = new LinkedHashMap<>();
        for (SoldeConge solde : soldes) {
            disponibles.put(solde.getId(), soldeCongeService.disponible(solde));
        }

        model.addAttribute("soldes", soldes);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("annee", anneeCible);
        model.addAttribute("employeId", employeId);
        model.addAttribute("typeId", typeId);
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("types", typeCongeService.listerCongesAvecSoldeActifs());
        return "conges/soldes/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        SoldeConge solde = soldeCongeService.getById(id);
        model.addAttribute("solde", solde);
        model.addAttribute("disponible", soldeCongeService.disponible(solde));
        model.addAttribute("mouvements", soldeCongeService.mouvements(id));
        model.addAttribute("ajustement", new AjustementSoldeForm());
        return "conges/soldes/detail";
    }

    @PostMapping("/{id}/ajuster")
    public String ajuster(@PathVariable Integer id,
                          @ModelAttribute AjustementSoldeForm form,
                          RedirectAttributes redirect) {
        try {
            soldeCongeService.ajuster(id, form.getJours(), form.getMotif(),
                    form.isForcer(), accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Solde ajusté.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/soldes/" + id;
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
