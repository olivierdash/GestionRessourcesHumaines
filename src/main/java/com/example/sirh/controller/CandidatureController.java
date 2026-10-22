package com.example.sirh.controller;


import com.example.sirh.model.Candidat;
import com.example.sirh.repository.CandidatRepository;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.OffreEmploieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bloc2")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final OffreEmploieService offreService; // Utile pour récupérer le titre de l'offre
    private final CandidatRepository candidatRepo;

    // TÂCHE 10 : Candidatures d'une offre
    @GetMapping("/offres/{idOffre}/candidatures")
    public String listeCandidatures(@PathVariable Integer idOffre, Model model) {
        model.addAttribute("offre", offreService.getOffreEmploiById(idOffre));
        model.addAttribute("candidatures", candidatureService.listeCandidatureByOffre(idOffre));
        return "bloc2/candidatures/liste";
    }

    // TÂCHE 11 : Fiche Candidat
    @GetMapping("/candidats/{idCandidat}")
    public String ficheCandidat(@PathVariable Integer idCandidat, Model model) {
        Candidat candidat = candidatRepo.findById(idCandidat)
            .orElseThrow(() -> new RuntimeException("Candidat introuvable"));
        
        model.addAttribute("candidat", candidat);
        model.addAttribute("candidatures", candidatureService.listeCandidatureByCandidat(idCandidat));
        return "bloc2/candidats/fiche";
    }
}

