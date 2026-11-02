package com.example.sirh.controller;

import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.PosteRepository;
import com.example.sirh.service.GrilleSalarialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/grilles")
@RequiredArgsConstructor
public class GrilleSalarialController {

    private final GrilleSalarialService grilleService;
    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("grilles", grilleService.lister());
        return "grille/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("grille", new GrilleSalarial());
        model.addAttribute("postes", posteRepository.findAll());
        model.addAttribute("departements", departementRepository.findAll());
        return "grille/form";
    }

    @PostMapping
    public String store(
            @RequestParam Integer idPoste,
            @RequestParam Integer idDepartement,
            @RequestParam BigDecimal salaireMin,
            @RequestParam BigDecimal salaireMax,
            @RequestParam BigDecimal salaireMoyen,
            RedirectAttributes redirect) {

        try {
            GrilleSalarial grille = grilleService.creer(
                    idPoste,
                    idDepartement,
                    salaireMin,
                    salaireMax,
                    salaireMoyen);

            return "redirect:/grilles/" + grille.getIdGrille();

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/grilles/create";
        }
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        model.addAttribute("grille", grilleService.trouver(id));
        return "grille/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("grille", grilleService.trouver(id));
        model.addAttribute("postes", posteRepository.findAll());
        model.addAttribute("departements", departementRepository.findAll());
        return "grille/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam Integer idPoste,
            @RequestParam Integer idDepartement,
            @RequestParam BigDecimal salaireMin,
            @RequestParam BigDecimal salaireMax,
            @RequestParam BigDecimal salaireMoyen,
            RedirectAttributes redirect) {

        try {
            grilleService.modifier(
                    id,
                    idPoste,
                    idDepartement,
                    salaireMin,
                    salaireMax,
                    salaireMoyen);

            return "redirect:/grilles/" + id;

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/grilles/" + id + "/edit";
        }
    }
}