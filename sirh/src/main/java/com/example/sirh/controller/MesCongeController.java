package com.example.sirh.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.example.sirh.dto.AbsenceForm;
import com.example.sirh.dto.CongeDashboardDto;
import com.example.sirh.dto.DemandeCongeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.SoldeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.service.AbsenceService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.CongeDashboardService;
import com.example.sirh.service.CalculDureeCongeService;
import com.example.sirh.service.DemandeCongeService;
import com.example.sirh.service.JustificatifCongeService;
import com.example.sirh.service.PlanningCongeService;
import com.example.sirh.service.SoldeCongeService;
import com.example.sirh.service.TypeCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mes-conges")
@RequiredArgsConstructor
public class MesCongeController {

    private final CongeAccessService accessService;
    private final CongeDashboardService dashboardService;
    private final DemandeCongeService demandeCongeService;
    private final AbsenceService absenceService;
    private final TypeCongeService typeCongeService;
    private final SoldeCongeService soldeCongeService;
    private final CalculDureeCongeService calculDureeCongeService;
    private final JustificatifCongeService justificatifCongeService;
    private final PlanningCongeService planningCongeService;

    @GetMapping
    public String dashboard(Model model) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            model.addAttribute("dashboard", dashboardService.tableauDeBord(utilisateur));
            model.addAttribute("annee", LocalDate.now().getYear());
        } catch (ValidationMetierException e) {
            model.addAttribute("dashboard", new CongeDashboardDto());
            model.addAttribute("annee", LocalDate.now().getYear());
            model.addAttribute("erreur", e.getErrors().values().stream().findFirst().orElse(
                    "Votre compte n'est lié à aucun salarié."));
        }
        return "conges/mes/index";
    }

    @GetMapping("/demandes")
    public String demandes(@RequestParam(required = false) StatutDemandeConge statut, Model model) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        model.addAttribute("filtreStatut", statut);
        if (utilisateur.getEmploye() == null) {
            model.addAttribute("demandes", List.of());
            model.addAttribute("erreur", "Votre compte n'est lié à aucun salarié.");
            return "conges/mes/demandes";
        }
        List<DemandeConge> demandes = demandeCongeService.listerMesDemandes(
                utilisateur.getEmploye().getIdEmployer());
        if (statut != null) {
            demandes = demandes.stream().filter(demande -> demande.getStatut() == statut).toList();
        }
        model.addAttribute("demandes", demandes);
        return "conges/mes/demandes";
    }

    @GetMapping("/nouveau")
    public String nouveau(@ModelAttribute("demande") DemandeCongeForm demande, Model model) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        model.addAttribute("types", typeCongeService.listerActifsPourEmploye());
        model.addAttribute("soldes", soldesUtilisateur(utilisateur));
        return "conges/mes/form";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("demande") DemandeCongeForm demande,
                              @RequestParam(defaultValue = "false") boolean soumettre,
                              Model model, RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            DemandeConge creee = demandeCongeService.creer(demande, utilisateur, soumettre);
            redirect.addFlashAttribute("succes", soumettre
                    ? "Demande soumise avec succès."
                    : "Brouillon enregistré.");
            return "redirect:/mes-conges/" + creee.getId();
        } catch (ValidationMetierException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("types", typeCongeService.listerActifsPourEmploye());
            model.addAttribute("soldes", soldesUtilisateur(utilisateur));
            return "conges/mes/form";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", typeCongeService.listerActifsPourEmploye());
            model.addAttribute("soldes", soldesUtilisateur(utilisateur));
            return "conges/mes/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        DemandeConge demande = demandeCongeService.getById(id);
        verifierAcces(demande);
        model.addAttribute("demande", demande);
        model.addAttribute("actionBase", "/mes-conges");
        model.addAttribute("filtreStatutBase", "/mes-conges/demandes");
        model.addAttribute("historique", demandeCongeService.historique(id));
        model.addAttribute("justificatifs", justificatifCongeService.listerPourDemande(id));
        model.addAttribute("conflitsPlanning", planningCongeService.conflitsPourDemande(demande));
        model.addAttribute("soldes", soldesPourDemande(demande));
        Utilisateur proprietaire = accessService.utilisateurCourant();
        model.addAttribute("peutSoumettre", accessService.estProprietaire(demande, proprietaire)
                && demande.getStatut() == com.example.sirh.model.enums.StatutDemandeConge.BROUILLON);
        model.addAttribute("peutValider", false);
        model.addAttribute("peutApprouver", false);
        model.addAttribute("peutRefuser", false);
        model.addAttribute("peutAnnuler", accessService.estProprietaire(demande, proprietaire));
        return "conges/demandes/detail";
    }

    @PostMapping("/{id}/soumettre")
    public String soumettre(@PathVariable Integer id, RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            demandeCongeService.soumettre(id, utilisateur);
            redirect.addFlashAttribute("succes", "Demande soumise. Le manager va la recevoir.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mes-conges/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id,
                          @RequestParam(required = false) String motif,
                          RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            demandeCongeService.annuler(id, motif, utilisateur);
            redirect.addFlashAttribute("succes", "Demande annulée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mes-conges/" + id;
    }

    @PostMapping("/{id}/justificatifs")
    public String deposerJustificatif(@PathVariable Integer id,
                                      @RequestParam("fichier") MultipartFile fichier,
                                      RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            justificatifCongeService.deposerPourDemande(id, fichier, utilisateur);
            redirect.addFlashAttribute("succes", "Justificatif déposé.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mes-conges/" + id;
    }

    // ------------------------------------------------------------------
    // Absences déclarées par le salarié
    // ------------------------------------------------------------------

    @GetMapping("/absences")
    public String absences(@ModelAttribute("absence") AbsenceForm absence,
                           @RequestParam(required = false) StatutAbsence statut, Model model) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        model.addAttribute("filtreStatut", statut);
        if (utilisateur.getEmploye() == null) {
            model.addAttribute("erreur", "Votre compte n'est lié à aucun salarié.");
            model.addAttribute("absences", List.of());
            model.addAttribute("types", List.of());
            return "conges/mes/absences";
        }
        Integer employeId = utilisateur.getEmploye().getIdEmployer();
        List<Absence> absences = absenceService.absencesDe(employeId);
        if (statut != null) {
            absences = absences.stream().filter(absenceLignee -> absenceLignee.getStatut() == statut).toList();
        }
        absence.setIdEmploye(employeId);

        Map<Integer, List<com.example.sirh.model.JustificatifConge>> justificatifs =
                new LinkedHashMap<>();
        for (Absence a : absences) {
            justificatifs.put(a.getId(),
                    justificatifCongeService.listerPourAbsence(a.getId()));
        }

        model.addAttribute("absences", absences);
        model.addAttribute("types", typeCongeService.listerAbsencesActives());
        model.addAttribute("justificatifsParAbsence", justificatifs);
        return "conges/mes/absences";
    }

    @PostMapping("/absences")
    public String declarerAbsence(@ModelAttribute("absence") AbsenceForm absence,
                                  Model model, RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        if (utilisateur.getEmploye() == null) {
            redirect.addFlashAttribute("erreur", "Votre compte n'est lié à aucun salarié.");
            return "redirect:/mes-conges/absences";
        }
        absence.setIdEmploye(utilisateur.getEmploye().getIdEmployer());
        try {
            absenceService.saisir(absence, utilisateur);
            redirect.addFlashAttribute("succes", "Absence déclarée.");
            return "redirect:/mes-conges/absences";
        } catch (ValidationMetierException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("erreur", premier(e));
            model.addAttribute("absences", absenceService.absencesDe(
                    utilisateur.getEmploye().getIdEmployer()));
            model.addAttribute("types", typeCongeService.listerAbsencesActives());
            model.addAttribute("justificatifsParAbsence", Map.of());
            return "conges/mes/absences";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-conges/absences";
        }
    }

    @PostMapping("/absences/{id}/justificatifs")
    public String deposerJustificatifAbsence(@PathVariable Integer id,
                                             @RequestParam("fichier") MultipartFile fichier,
                                             RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            justificatifCongeService.deposerPourAbsence(id, fichier, utilisateur);
            redirect.addFlashAttribute("succes", "Justificatif déposé.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mes-conges/absences";
    }

    @PostMapping("/absences/{id}/annuler")
    public String annulerAbsence(@PathVariable Integer id,
                                 @RequestParam(required = false) String motif,
                                 RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            absenceService.annuler(id, motif, utilisateur);
            redirect.addFlashAttribute("succes", "Absence annulée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mes-conges/absences";
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<com.example.sirh.dto.SoldeCongeView> soldesUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            return List.of();
        }
        return soldeCongeService.soldesEmploye(
                utilisateur.getEmploye().getIdEmployer(), LocalDate.now().getYear());
    }

    private List<SoldeConge> soldesPourDemande(DemandeConge demande) {
        return soldeCongeService.rechercher(null, demande.getEmploye().getIdEmployer(),
                demande.getTypeConge().getId()).stream()
                .filter(s -> Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde()))
                .toList();
    }

    private void verifierAcces(DemandeConge demande) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        if (!accessService.peutVoirDemande(demande, utilisateur)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas accès à cette demande.");
        }
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
