package com.example.sirh.controller;

import com.example.sirh.model.Departement;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.service.DepartementService;
import com.example.sirh.service.GrilleSalarialService;
import com.example.sirh.service.PosteService;
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
    private final PosteService posteService;
    private final DepartementService departementService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("grilles", grilleService.lister());
        return "grille/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("grille")) {
            GrilleSalarial grille = new GrilleSalarial();
            grille.setPoste(new Poste());
            grille.setDepartement(new Departement());
            model.addAttribute("grille", grille);
        }
        model.addAttribute("postes", posteService.getAllPostes());
        model.addAttribute("departements", departementService.getAllDepartements());
        return "grille/form";
    }

    @PostMapping
    public String store(GrilleSalarial grille, RedirectAttributes redirect) {
        try {
            Poste poste = posteService.getById(grille.getPoste().getIdPoste());
            Departement departement = departementService.getById(grille.getDepartement().getIdDepartement());

            grille.setPoste(poste);
            grille.setDepartement(departement);

            GrilleSalarial saved = grilleService.creer(grille);
            return "redirect:/grilles/" + saved.getIdGrille();

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("grille", grille);
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
        model.addAttribute("postes", posteService.getAllPostes());
        model.addAttribute("departements", departementService.getAllDepartements());
        return "grille/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            GrilleSalarial grille,
            RedirectAttributes redirect) {

        try {
            Poste poste = posteService.getById(grille.getPoste().getIdPoste());
            Departement departement = departementService.getById(grille.getDepartement().getIdDepartement());

            grilleService.modifier(grille);
            return "redirect:/grilles/" + id;

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/grilles/" + id + "/edit";
        }
    }
}