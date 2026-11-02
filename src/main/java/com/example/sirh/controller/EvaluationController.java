package com.example.sirh.controller;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.EvaluationService;
import com.example.sirh.service.TypeEvaluationService;
import com.example.sirh.service.UtilisateurService;
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
    private final TypeEvaluationService typeEvaluationService; // Injection du Service
    private final UtilisateurService utilisateurService;       // Injection du Service

    // TÂCHE 12 : Afficher le formulaire et l'historique
    @GetMapping("/{idCandidature}/evaluer")
    public String formEvaluation(@PathVariable Integer idCandidature, Model model) {
        model.addAttribute("candidature", candidatureService.getCandidatureById(idCandidature));
        model.addAttribute("typesEvaluation", typeEvaluationService.getAllTypes()); // Appel au Service
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
            // On utilise le service pour récupérer le premier utilisateur (factice)
            Utilisateur evaluateur = utilisateurService.getFirstUtilisateur();

            evaluationService.ajouterEvaluation(idCandidature, evaluateur.getIdUtilisateur(), idTypeEvaluation, commentaire, note);
            redirectAttributes.addFlashAttribute("success", "Évaluation enregistrée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/candidatures/" + idCandidature + "/evaluer";
    }
}