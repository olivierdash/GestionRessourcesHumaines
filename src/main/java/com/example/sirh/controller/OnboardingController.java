package com.example.sirh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.Onboarding;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.DocumentService;
import com.example.sirh.service.OnboardingService;
import com.example.sirh.service.UtilisateurService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/onboarding")
@AllArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final DocumentService documentService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/taches/{idTache}/documents")
    public String deposer(@PathVariable Integer idTache,
            @RequestParam Integer idEmploye,
            @RequestParam Integer idTypeDocument,
            @RequestParam("fichier") MultipartFile fichier,
            Authentication auth,
            RedirectAttributes redirect) {
        try {
            // APPEL AU SERVICE Utilisateur
            Utilisateur utilisateur = utilisateurService.getByEmail(auth.getName());

            documentService.deposer(idEmploye, idTypeDocument, idTache, utilisateur.getIdUtilisateur(), fichier);
            redirect.addFlashAttribute("succes", "Document déposé");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/onboarding/employe/" + idEmploye;
    }

    @PostMapping("/taches/{idTache}/terminer")
    public String terminer(@PathVariable Integer idTache,
            @RequestParam Integer idEmploye,
            RedirectAttributes redirect) {
        try {
            onboardingService.terminerTache(idTache);
            redirect.addFlashAttribute("succes", "Tâche terminée");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/onboarding/employe/" + idEmploye;
    }

    // Page 19 : checklist d'un salarié
    @GetMapping("/employe/{id}")
    public String checklist(@PathVariable Integer id, Model model) {
        Onboarding onboarding = onboardingService.parEmploye(id)
                .orElseThrow(() -> new IllegalArgumentException("Aucun onboarding pour ce salarié"));

        model.addAttribute("idEmploye", id);
        model.addAttribute("onboarding", onboarding);
        model.addAttribute("taches", onboardingService.taches(onboarding.getId()));
        model.addAttribute("progression", onboardingService.progression(onboarding.getId()));
        
        // APPEL AU SERVICE Document (qui gérera les types de documents)
        model.addAttribute("typesDocument", documentService.getAllTypesDocument());
        
        return "onboarding/checklist";
    }
}