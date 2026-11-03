package com.example.sirh.controller;

import com.example.sirh.dto.DemandeRecrutementFilter;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.service.DemandeRecrutementService;
import com.example.sirh.service.OffreEmploieService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/offres")
@RequiredArgsConstructor
public class OffreEmploiController {

    private final OffreEmploieService offreService;
    private final DemandeRecrutementService demandeService; // On injecte le Service, plus le Repository

    @GetMapping
    public String listeOffres(Model model) {
        model.addAttribute("offres", offreService.listAllEmploie());
        return "offres/liste";
    }

    // TÂCHE 9 : Formulaire de création
    @GetMapping("/creer")
    public String formCreerOffre(Model model) {
        model.addAttribute("offre", new OffreEmploi());
        // APPEL AU SERVICE au lieu du Repository
        model.addAttribute("demandes", demandeService.getAllDemandeValide(new DemandeRecrutementFilter()));
        return "offres/form";
    }

    // TÂCHE 9 : Enregistrement de l'offre
    @PostMapping("/creer")
    public String creerOffre(@ModelAttribute OffreEmploi offre,
            RedirectAttributes redirectAttributes) {
        try {
            offreService.createNewOffer(offre, 2);
            redirectAttributes.addFlashAttribute("success", "Offre créée avec succès");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : Une offre existe déjà pour cette demande.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/offres";
    }

    // TÂCHE 9 bis : Publication de l'offre
    @PostMapping("/{id}/publier")
    public String publierOffre(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            offreService.publicationOffre(id);
            redirectAttributes.addFlashAttribute("success", "Offre publiée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/offres";
    }
}