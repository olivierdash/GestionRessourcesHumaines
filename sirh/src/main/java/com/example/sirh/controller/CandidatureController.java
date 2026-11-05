package com.example.sirh.controller;

import com.example.sirh.model.Candidat;
import com.example.sirh.service.CandidatService;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.OffreEmploieService;
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

    // TÂCHE 10 : Candidatures d'une offre
    @GetMapping("/offres/{idOffre}")
    public String listeCandidatures(@PathVariable Integer idOffre, Model model) {
        model.addAttribute("offre", offreService.getOffreEmploiById(idOffre));
        model.addAttribute("candidatures", candidatureService.listeCandidatureByOffre(idOffre));
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

    @PostMapping("/candidatures/{id}/retenir")
    public String retenirCandidat(
            @PathVariable Integer id, 
            @RequestParam Integer idOffre, 
            RedirectAttributes redirectAttributes) {
        candidatureService.changerStatutCandidature(id, 3); // 3 = RETENU
        redirectAttributes.addFlashAttribute("success", "Le candidat a été retenu !");
        return "redirect:/offres/" + idOffre + "/candidatures";
    }

    // TÂCHE 12 bis : Refuser un candidat
    @PostMapping("/candidatures/{id}/refuser")
    public String refuserCandidat(
        @PathVariable Integer id, 
        @RequestParam Integer idOffre, 
        RedirectAttributes redirectAttributes) {
        candidatureService.changerStatutCandidature(id, 4); // 4 = REFUSE
        redirectAttributes.addFlashAttribute("success", "Le candidat a été refusé.");
        return "redirect:/offres/" + idOffre + "/candidatures";
    }
}