package com.example.sirh.controller;

import com.example.sirh.service.ModeleOnboardingService;
import com.example.sirh.service.RoleService;
import com.example.sirh.service.TypeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/onboarding/modeles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RH')")
public class ModeleOnboardingController {

    private final ModeleOnboardingService modeleService;
    private final RoleService roleService;
    private final TypeDocumentService typeDocumentService;

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("modeles", modeleService.lister());
        return "onboarding/modeles";
    }

    @PostMapping
    public String creer(@RequestParam String nom, RedirectAttributes redirect) {
        try {
            var modele = modeleService.creer(nom);
            redirect.addFlashAttribute("succes", "Modèle créé. Ajoutez ses tâches puis activez-le.");
            return "redirect:/onboarding/modeles/" + modele.getId();
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/onboarding/modeles";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Integer id, Model model) {
        model.addAttribute("modele", modeleService.trouver(id));
        model.addAttribute("taches", modeleService.listerTaches(id));
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("typesDocument", typeDocumentService.getAllTypes());
        return "onboarding/modele-detail";
    }

    @PostMapping("/{id}/renommer")
    public String renommer(@PathVariable Integer id, @RequestParam String nom,
            RedirectAttributes redirect) {
        return traiter(id, redirect, () -> modeleService.renommer(id, nom), "Nom du modèle mis à jour.");
    }

    @PostMapping("/{id}/activer")
    public String activer(@PathVariable Integer id, RedirectAttributes redirect) {
        return traiter(id, redirect, () -> modeleService.activer(id), "Modèle activé pour les futurs onboardings.");
    }

    @PostMapping("/{id}/taches")
    public String ajouterTache(@PathVariable Integer id,
            @RequestParam String titre,
            @RequestParam Integer idRoleResponsable,
            @RequestParam Integer delaiJours,
            @RequestParam(defaultValue = "false") Boolean obligatoire,
            @RequestParam(required = false) Integer idTypeDocumentRequis,
            @RequestParam Integer ordre,
            RedirectAttributes redirect) {
        return traiter(id, redirect,
                () -> modeleService.ajouterTache(id, titre, idRoleResponsable, delaiJours,
                        obligatoire, idTypeDocumentRequis, ordre),
                "Tâche ajoutée au modèle.");
    }

    @PostMapping("/{id}/taches/{idTache}")
    public String modifierTache(@PathVariable Integer id, @PathVariable Integer idTache,
            @RequestParam String titre,
            @RequestParam Integer idRoleResponsable,
            @RequestParam Integer delaiJours,
            @RequestParam(defaultValue = "false") Boolean obligatoire,
            @RequestParam(required = false) Integer idTypeDocumentRequis,
            @RequestParam Integer ordre,
            RedirectAttributes redirect) {
        return traiter(id, redirect,
                () -> modeleService.modifierTache(id, idTache, titre, idRoleResponsable,
                        delaiJours, obligatoire, idTypeDocumentRequis, ordre),
                "Tâche mise à jour.");
    }

    @PostMapping("/{id}/taches/{idTache}/supprimer")
    public String supprimerTache(@PathVariable Integer id, @PathVariable Integer idTache,
            RedirectAttributes redirect) {
        return traiter(id, redirect, () -> modeleService.supprimerTache(id, idTache), "Tâche supprimée.");
    }

    private String traiter(Integer id, RedirectAttributes redirect, Runnable action, String messageSucces) {
        try {
            action.run();
            redirect.addFlashAttribute("succes", messageSucces);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/onboarding/modeles/" + id;
    }
}
