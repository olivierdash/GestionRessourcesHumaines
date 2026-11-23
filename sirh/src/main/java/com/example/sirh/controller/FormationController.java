package com.example.sirh.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.Formation;
import com.example.sirh.repository.FormationRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement/formations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class FormationController {

    private final FormationRepository formationRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("formations", formationRepository.findAll());
        model.addAttribute("formation", new Formation());
        return "developpement/formations";
    }

    @PostMapping
    public String enregistrer(Formation formation,
            RedirectAttributes redirectAttributes) {
        formationRepository.save(formation);
        redirectAttributes.addFlashAttribute("success", "Formation enregistrée.");
        return "redirect:/developpement/formations";
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Integer id,
            @RequestParam String statut,
            RedirectAttributes redirectAttributes) {
        Formation formation = formationRepository.findById(id).orElseThrow();
        formation.setStatut(statut);
        formationRepository.save(formation);
        redirectAttributes.addFlashAttribute("success", "Statut de la formation mis à jour.");
        return "redirect:/developpement/formations";
    }
}
