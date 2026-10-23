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
import com.example.sirh.repository.TypeDocumentRepository;
import com.example.sirh.repository.UtilisateurRepository;
import com.example.sirh.service.DocumentService;
import com.example.sirh.service.OnboardingService;

@Controller
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final DocumentService documentService;
    private final TypeDocumentRepository typeDocumentRepository;
    private final UtilisateurRepository utilisateurRepository;

    public OnboardingController(OnboardingService onboardingService,
            DocumentService documentService,
            TypeDocumentRepository typeDocumentRepository,UtilisateurRepository utilisateurRepository) {
        this.onboardingService = onboardingService;
        this.documentService = documentService;
        this.typeDocumentRepository = typeDocumentRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/taches/{idTache}/documents")
    public String deposer(@PathVariable Integer idTache,
            @RequestParam Integer idEmploye,
            @RequestParam Integer idTypeDocument,
            @RequestParam("fichier") MultipartFile fichier,
            Authentication auth,
            RedirectAttributes redirect) {
        try {
            Integer idUtilisateur = utilisateurRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable"))
                    .getIdUtilisateur();

            documentService.deposer(idEmploye, idTypeDocument, idTache, idUtilisateur, fichier);
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
        model.addAttribute("typesDocument", typeDocumentRepository.findAll());
        return "bloc4/onboarding/checklist";
    }
}