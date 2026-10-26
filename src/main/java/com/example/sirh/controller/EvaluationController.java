package com.example.sirh.controller;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.TypeEvaluationRepository;
import com.example.sirh.repository.UtilisateurRepository;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/candidatures")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final CandidatureService candidatureService;
    private final TypeEvaluationRepository typeEvalRepo;
    private final UtilisateurRepository utilisateurRepo;

    // TÂCHE 12 : Afficher le formulaire et l'historique
    @GetMapping("/{idCandidature}/evaluer")
    public String formEvaluation(@PathVariable Integer idCandidature, Model model) {
        model.addAttribute("candidature", candidatureService.getCandidatureById(idCandidature));
        model.addAttribute("typesEvaluation", typeEvalRepo.findAll());
        model.addAttribute("evaluations", evaluationService.listerEvaluationsParCandidature(idCandidature));
        return "candidatures/evaluation";
    }

    // TÂCHE 12 : Enregistrer l'évaluation
    @PostMapping("/{idCandidature}/evaluer")
    public String enregistrerEvaluation(
            @PathVariable Integer idCandidature,
            @RequestParam Integer idTypeEvaluation,
            @RequestParam(required = false) Double note,
            @RequestParam String commentaire,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Prend le premier utilisateur factice
            Utilisateur evaluateur = utilisateurRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur en base pour évaluer."));

            evaluationService.ajouterEvaluation(idCandidature, evaluateur.getIdUtilisateur(), idTypeEvaluation, commentaire, note);
            redirectAttributes.addFlashAttribute("success", "Évaluation enregistrée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/candidatures/" + idCandidature + "/evaluer";
    }
}
