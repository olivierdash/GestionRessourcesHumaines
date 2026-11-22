package com.example.sirh.controller;

import com.example.sirh.constants.ConstantSirh.Statut;
import com.example.sirh.model.Candidat;
import com.example.sirh.service.CandidatService;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.EvaluationService;
import com.example.sirh.service.FileStorageService;
import com.example.sirh.service.OffreEmploieService;
import com.example.sirh.service.StatutCandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final OffreEmploieService offreService;
    private final CandidatService candidatService; // Injection du Service
    private final EvaluationService evaluationService;
    private final StatutCandidatureService statutCandidatureService;
    private final FileStorageService fileStorageService;

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

    /**
     * Affiche le CV dans le navigateur (PDF ou image) au lieu de le télécharger.
     */
    @GetMapping("/candidats/{idCandidat}/cv")
    public ResponseEntity<Resource> consulterCv(@PathVariable Integer idCandidat) {
        return reponseCv(idCandidat, true);
    }

    @GetMapping("/candidats/{idCandidat}/cv/telecharger")
    public ResponseEntity<Resource> telechargerCv(@PathVariable Integer idCandidat) {
        return reponseCv(idCandidat, false);
    }

    private ResponseEntity<Resource> reponseCv(Integer idCandidat, boolean apercu) {
        Candidat candidat = candidatService.getCandidatById(idCandidat);
        String cvUrl = candidat.getCvUrl();

        if (cvUrl == null || cvUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Aucun CV n'est associé à ce candidat.");
        }

        return fileStorageService.repondre(cvUrl, nomCv(candidat, cvUrl), apercu);
    }

    /**
     * Les CV sont stockés sous la forme {@code cv_<uuid>_<nom-original>.pdf}.
     * On restitue le nom d'origine pour le navigateur.
     */
    private String nomCv(Candidat candidat, String cvUrl) {
        String nomStocke = cvUrl.substring(cvUrl.lastIndexOf('/') + 1);
        String nomOriginal = nomStocke.replaceFirst(
                "^cv_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_",
                "");

        if (nomOriginal.isBlank() || nomOriginal.equals(nomStocke)) {
            nomOriginal = "CV - " + candidat.getPrenom() + " " + candidat.getNom() + ".pdf";
        }
        return nomOriginal;
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