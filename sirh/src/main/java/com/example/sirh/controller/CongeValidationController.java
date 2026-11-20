package com.example.sirh.controller;

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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.dto.ValidationDemandeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DemandeCongeService;
import com.example.sirh.service.JustificatifCongeService;
import com.example.sirh.service.PlanningCongeService;
import com.example.sirh.service.SoldeCongeService;
import com.example.sirh.service.VariablePaieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/validations")
@RequiredArgsConstructor
public class CongeValidationController {

    private final CongeAccessService accessService;
    private final DemandeCongeService demandeCongeService;
    private final JustificatifCongeService justificatifCongeService;
    private final SoldeCongeService soldeCongeService;
    private final PlanningCongeService planningCongeService;
    private final VariablePaieService variablePaieService;

    @GetMapping
    public String index(Model model) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        model.addAttribute("demandes", demandeCongeService.fileValidation(utilisateur));
        model.addAttribute("enAttente", demandeCongeService.compterEnAttente(utilisateur));
        return "conges/validations/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        DemandeConge demande = demandeCongeService.getById(id);
        Utilisateur utilisateur = accessService.utilisateurCourant();
        if (!accessService.peutVoirDemande(demande, utilisateur)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas accès à cette demande.");
        }

        model.addAttribute("demande", demande);
        model.addAttribute("actionBase", "/conges/validations");
        model.addAttribute("historique", demandeCongeService.historique(id));
        model.addAttribute("justificatifs", justificatifCongeService.listerPourDemande(id));
        model.addAttribute("variablesPaie", variablePaieService.parDemande(id));
        model.addAttribute("conflitsPlanning", planningCongeService.conflitsPourDemande(demande));
        model.addAttribute("soldes", soldeCongeService.rechercher(null,
                demande.getEmploye().getIdEmployer(), demande.getTypeConge().getId()));
        model.addAttribute("peutSoumettre", false);
        model.addAttribute("peutAnnuler", false);
        model.addAttribute("peutValider", demande.getStatut() == StatutDemandeConge.SOUMISE
                && accessService.peutValiderEnTantQueManager(demande, utilisateur));
        model.addAttribute("peutApprouver", demande.getStatut() == StatutDemandeConge.VALIDEE_MANAGER
                && accessService.peutApprouver(demande, utilisateur));
        model.addAttribute("peutRefuser", (demande.getStatut() == StatutDemandeConge.SOUMISE
                && accessService.peutValiderEnTantQueManager(demande, utilisateur))
                || (demande.getStatut() == StatutDemandeConge.VALIDEE_MANAGER
                && accessService.peutApprouver(demande, utilisateur)));
        return "conges/demandes/detail";
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id,
                          @RequestParam(required = false) String commentaire,
                          RedirectAttributes redirect) {
        try {
            demandeCongeService.validerManager(id, commentaire, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Demande validée par le manager.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/validations/" + id;
    }

    @PostMapping("/{id}/approuver")
    public String approuver(@PathVariable Integer id,
                            @ModelAttribute ValidationDemandeForm form,
                            RedirectAttributes redirect) {
        try {
            demandeCongeService.approuver(id, form, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Demande approuvée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("erreur", premier(e));
            redirect.addFlashAttribute("form", form);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/validations/" + id;
    }

    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Integer id,
                          @ModelAttribute ValidationDemandeForm form,
                          RedirectAttributes redirect) {
        try {
            demandeCongeService.refuser(id, form, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Demande refusée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/validations/" + id;
    }

    @PostMapping("/{id}/justificatifs")
    public String deposerJustificatif(@PathVariable Integer id,
                                      @RequestParam("fichier") MultipartFile fichier,
                                      RedirectAttributes redirect) {
        try {
            justificatifCongeService.deposerPourDemande(
                    id, fichier, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Justificatif déposé.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/validations/" + id;
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
