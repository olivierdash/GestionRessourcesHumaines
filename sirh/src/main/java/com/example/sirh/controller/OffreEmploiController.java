package com.example.sirh.controller;

import com.example.sirh.dto.DemandeRecrutementFilter;
import com.example.sirh.dto.OffreFilter;
import com.example.sirh.exception.ValidationMetierException;
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
        List<Utilisateur> managers = utilisateurService.getByRolesList(List.of("MANAGER", "RH"));

        model.addAttribute("offres", offresPage);
        model.addAttribute("managers", managers);
        model.addAttribute("statuts", statutService.getAllStatuts()); // À ajouter si n'existe pas

        return "offres/liste";
    }

       // TÂCHE 9 : Formulaire de création
    @GetMapping("/creer")
    public String formCreerOffre(@ModelAttribute("offre") OffreEmploi offre, Model model) {
        // Spring injecte le flash attribute (retour après erreur) ou crée un objet vide
        model.addAttribute("offre", offre);
        model.addAttribute("demandes", demandeService.getAllDemandeValide(new DemandeRecrutementFilter()));
        return "offres/form";
    }

    // TÂCHE 9 : Enregistrement de l'offre
    @PostMapping("/creer")
    public String creerOffre(
            @ModelAttribute("offre") OffreEmploi offre,
            @RequestParam("action") String action,
            RedirectAttributes redirectAttributes) {

        try {

            // 1. Déterminer le statut selon le choix de RH
            Integer idStatut;

            if ("brouillon".equals(action)) {
                idStatut = 1; // BROUILLON
            } else if ("publier".equals(action)) {
                idStatut = 2; // PUBLIEE
            } else {
                throw new IllegalArgumentException(
                        "Action inconnue. Choisissez brouillon ou publier.");
            }

            // 2. Créer l'offre avec le statut choisi
            offreService.createNewOffer(offre, idStatut);

            // 3. Message de confirmation
            if (idStatut == 1) {
                redirectAttributes.addFlashAttribute(
                        "success",
                        "Offre enregistrée en brouillon avec succès !");
            } else {
                redirectAttributes.addFlashAttribute(
                        "success",
                        "Offre publiée avec succès ! Elle sera visible à sa date de début.");
            }

            return "redirect:/offres";

        } catch (ValidationMetierException e) {

            redirectAttributes.addFlashAttribute("errors", e.getErrors());
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Veuillez corriger les erreurs du formulaire.");

            redirectAttributes.addFlashAttribute("offre", offre);

            return "redirect:/offres/creer";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Erreur lors de la création : " + e.getMessage());

            redirectAttributes.addFlashAttribute("offre", offre);

            return "redirect:/offres/creer";
        }
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