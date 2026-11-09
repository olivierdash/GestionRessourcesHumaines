package com.example.sirh.controller;

import com.example.sirh.constants.ConstantSirh.Statut;
import com.example.sirh.model.Candidat;
import com.example.sirh.service.CandidatService;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.EvaluationService;
import com.example.sirh.service.OffreEmploieService;
import com.example.sirh.service.StatutCandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final OffreEmploieService offreService;
    private final CandidatService candidatService; // Injection du Service
    private final EvaluationService evaluationService;
    private final StatutCandidatureService statutCandidatureService;

    // TÂCHE 10 : Candidatures d'une offre
    @GetMapping("/offres/{idOffre}")
    public String listeCandidatures(@PathVariable Integer idOffre, Model model) {
        Integer idRetenu = statutCandidatureService.getByLibelle(Statut.RETENU).getId();
        Integer idRefuse = statutCandidatureService.getByLibelle(Statut.REFUSE).getId();

        model.addAttribute("offre", offreService.getOffreEmploiById(idOffre));
        model.addAttribute("lignes", candidatureService.findAvecPropositionActive(idOffre));
        model.addAttribute("idRetenu", idRetenu);
        model.addAttribute("idRefuse", idRefuse);

        return "candidatures/liste";
    }

    // TÂCHE 11 : Fiche Candidat
    @GetMapping("/candidats/{idCandidat}")
    public String ficheCandidat(@PathVariable Integer idCandidat, Model model) {
        // Appel au Service au lieu du Repository
        Candidat candidat = candidatService.getCandidatById(idCandidat);
        model.addAttribute("candidat", candidat);
        model.addAttribute("candidatures", candidatureService.listeCandidatureByCandidat(idCandidat));
        return "candidats/fiche";
    }

    @GetMapping("/candidatures/{id}")
    public String pageCandidature(@PathVariable Integer id, Model model) {
        model.addAttribute("candidature", candidatureService.getCandidatureById(id));
        model.addAttribute("evaluations", evaluationService.listerEvaluationsParCandidature(id));
        return "candidatures/show";
    }

    @PostMapping("/candidatures/{id}/retenir")
    public String retenirCandidat(
            @PathVariable Integer id,
            @RequestParam Integer idOffre,
            RedirectAttributes redirectAttributes) {
        Integer idRetenu = statutCandidatureService.getByLibelle(Statut.RETENU).getId();
        candidatureService.changerStatutCandidature(id, idRetenu); 
        redirectAttributes.addFlashAttribute("success", "Le candidat a été retenu !");
        return "redirect:/offres/" + idOffre;
    }

    // TÂCHE 12 bis : Refuser un candidat
    @PostMapping("/candidatures/{id}/refuser")
    public String refuserCandidat(
            @PathVariable Integer id,
            @RequestParam Integer idOffre,
            RedirectAttributes redirectAttributes) {
        Integer idRefuse = statutCandidatureService.getByLibelle(Statut.REFUSE).getId();
        candidatureService.changerStatutCandidature(id, idRefuse); 
        redirectAttributes.addFlashAttribute("success", "Le candidat a été refusé.");
        return "redirect:/offres/" + idOffre;
    }
}