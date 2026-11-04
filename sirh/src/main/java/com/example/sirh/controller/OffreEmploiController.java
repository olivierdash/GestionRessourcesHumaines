package com.example.sirh.controller;

import com.example.sirh.dto.DemandeRecrutementFilter;
import com.example.sirh.dto.OffreFilter;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.DemandeRecrutementService;
import com.example.sirh.service.OffreEmploieService;
import com.example.sirh.service.StatutOffreEmploieService;
import com.example.sirh.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/offres")
@RequiredArgsConstructor
public class OffreEmploiController {

    private final OffreEmploieService offreService;
    private final UtilisateurService utilisateurService;
    private final StatutOffreEmploieService statutService;
    private final DemandeRecrutementService demandeService;

    @GetMapping
    public String listeOffres(
            @ModelAttribute("filter") OffreFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100), Sort.by("idOffre").descending());
        Page<OffreEmploi> offresPage = offreService.getOffresFiltrees(filter, pageable);

        // On récupère uniquement les managers et RH pour le select
        List<Utilisateur> managers = utilisateurService.getManagersEtRH();

        model.addAttribute("offres", offresPage);
        model.addAttribute("managers", managers);
        model.addAttribute("statuts", statutService.getAllStatuts()); // À ajouter si n'existe pas

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