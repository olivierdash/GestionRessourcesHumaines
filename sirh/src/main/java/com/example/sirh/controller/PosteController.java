package com.example.sirh.controller;

import com.example.sirh.dto.PosteFilter;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Poste;
import com.example.sirh.service.DepartementService;
import com.example.sirh.service.LieuService;
import com.example.sirh.service.PosteReferentielService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/postes")
@RequiredArgsConstructor
public class PosteController {

    private final PosteReferentielService posteService;
    private final DepartementService departementService;
    private final LieuService lieuService;

    @GetMapping
    public String index(
            @ModelAttribute("filter") PosteFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 100),
                Sort.by("intitule").ascending());

        model.addAttribute("postes", posteService.listerPostes(filter, pageable));
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("lieux", lieuService.getAll());
        model.addAttribute("per_page", List.of(5, 10, 25));

        return "poste/index";
    }

    @GetMapping("/create")
    public String create(@ModelAttribute("poste") Poste flashPoste, Model model) {
        if (flashPoste.getIdPoste() == null && flashPoste.getIntitule() == null) {
            model.addAttribute("poste", new Poste());
        } else {
            model.addAttribute("poste", flashPoste);
        }
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("lieux", lieuService.getAll());
        return "poste/form";
    }

    @PostMapping
    public String store(@ModelAttribute("poste") Poste poste, RedirectAttributes redirect) {
        try {
            Poste created = posteService.creerPoste(poste);
            return "redirect:/postes/" + created.getIdPoste();
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("errorMessage", "Veuillez corriger les erreurs ci-dessous.");
            redirect.addFlashAttribute("poste", poste);
            return "redirect:/postes/create";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("poste", poste);
            return "redirect:/postes/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id,
            @ModelAttribute("poste") Poste flashPoste,
            Model model) {
        if (flashPoste.getIdPoste() != null) {
            model.addAttribute("poste", flashPoste);
        } else {
            model.addAttribute("poste", posteService.trouverPoste(id));
        }
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("lieux", lieuService.getAll());
        return "poste/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
            @ModelAttribute("poste") Poste poste,
            RedirectAttributes redirect) {
        try {
            posteService.modifierPoste(id, poste);
            return "redirect:/postes/" + id;
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("errorMessage", "Veuillez corriger les erreurs ci-dessous.");
            poste.setIdPoste(id);
            redirect.addFlashAttribute("poste", poste);
            return "redirect:/postes/" + id + "/edit";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            poste.setIdPoste(id);
            redirect.addFlashAttribute("poste", poste);
            return "redirect:/postes/" + id + "/edit";
        }
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Poste poste = posteService.trouverPoste(id);
        model.addAttribute("poste", poste);
        model.addAttribute("missions", posteService.getMissionsByPosteId(id));
        return "poste/show";
    }

    @PostMapping("/{id}/missions")
    public String addMission(@PathVariable Integer id,
            @RequestParam String titre,
            RedirectAttributes redirect) {
        try {
            posteService.ajouterMission(id, titre);
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/postes/" + id;
    }

    @PostMapping("/{id}/missions/{missionId}/delete")
    public String deleteMission(@PathVariable Integer id,
            @PathVariable Integer missionId,
            RedirectAttributes redirect) {
        try {
            posteService.supprimerMission(id, missionId);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/postes/" + id;
    }

    @GetMapping("/by-departement/{id}")
    @ResponseBody
    public List<Map<String, Object>> postesByDepartement(@PathVariable Integer id) {
        return posteService.getByDepartement(id).stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getIdPoste(),
                        "libelle", p.getIntitule()))
                .toList();
    }
}