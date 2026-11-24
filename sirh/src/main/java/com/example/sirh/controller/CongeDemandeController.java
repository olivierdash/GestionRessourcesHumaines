package com.example.sirh.controller;

import java.time.LocalDate;
import java.util.List;

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

import com.example.sirh.dto.DemandeCongeFilter;
import com.example.sirh.dto.ValidationDemandeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DemandeCongeService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.JustificatifCongeService;
import com.example.sirh.service.PlanningCongeService;
import com.example.sirh.service.SoldeCongeService;
import com.example.sirh.service.TypeCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/demandes")
@RequiredArgsConstructor
public class CongeDemandeController {

    private final CongeAccessService accessService;
    private final DemandeCongeService demandeCongeService;
    private final EmployeService employeService;
    private final TypeCongeService typeCongeService;
    private final SoldeCongeService soldeCongeService;
    private final JustificatifCongeService justificatifCongeService;
    private final PlanningCongeService planningCongeService;

    @GetMapping
    public String index(@ModelAttribute("filter") DemandeCongeFilter filter, Model model) {
        List<DemandeConge> demandes = filter.isActif()
                ? demandeCongeService.rechercher(filter)
                : demandeCongeService.rechercher(new DemandeCongeFilter());
        model.addAttribute("demandes", demandes);
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("types", typeCongeService.lister());
        model.addAttribute("statuts", StatutDemandeConge.values());
        return "conges/demandes/index";
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
        model.addAttribute("actionBase", "/conges/demandes");
        model.addAttribute("filtreStatutBase", "/conges/demandes");
        model.addAttribute("historique", demandeCongeService.historique(id));
        model.addAttribute("justificatifs", justificatifCongeService.listerPourDemande(id));
        model.addAttribute("conflitsPlanning", planningCongeService.conflitsPourDemande(demande));
        model.addAttribute("soldes", soldeCongeService.rechercher(null,
                demande.getEmploye().getIdEmployer(), demande.getTypeConge().getId()));
        model.addAttribute("peutSoumettre", false);
        model.addAttribute("peutValider", demande.getStatut() == StatutDemandeConge.SOUMISE
                && accessService.peutValiderEnTantQueManager(demande, utilisateur));
        model.addAttribute("peutApprouver", demande.getStatut() == StatutDemandeConge.VALIDEE_MANAGER
                && accessService.peutApprouver(demande, utilisateur));
        model.addAttribute("peutRefuser", (demande.getStatut() == StatutDemandeConge.SOUMISE
                && accessService.peutValiderEnTantQueManager(demande, utilisateur))
                || (demande.getStatut() == StatutDemandeConge.VALIDEE_MANAGER
                && accessService.peutApprouver(demande, utilisateur)));
        model.addAttribute("peutAnnuler", accessService.estProprietaire(demande, utilisateur)
                || accessService.estRhOuDg(utilisateur));
        model.addAttribute("peutSoumettreJustificatif", 
                accessService.estProprietaire(demande, utilisateur)
                        && demande.getCreateur() != null
                        && demande.getCreateur().getIdUtilisateur() != null
                        && demande.getCreateur().getIdUtilisateur().equals(utilisateur.getIdUtilisateur()));
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
        return "redirect:/conges/demandes/" + id;
    }

    @PostMapping("/{id}/approuver")
    public String approuver(@PathVariable Integer id,
                            @ModelAttribute ValidationDemandeForm form,
                            RedirectAttributes redirect) {
        try {
            demandeCongeService.approuver(id, form, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Demande approuvée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/demandes/" + id;
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
        return "redirect:/conges/demandes/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id,
                          @RequestParam(required = false) String motif,
                          RedirectAttributes redirect) {
        try {
            demandeCongeService.annuler(id, motif, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Demande annulée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/demandes/" + id;
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
        return "redirect:/conges/demandes/" + id;
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
