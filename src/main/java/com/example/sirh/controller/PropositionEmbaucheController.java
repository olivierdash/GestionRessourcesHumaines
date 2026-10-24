package com.example.sirh.controller;

import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.EmbaucheService;
import com.example.sirh.service.PropositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/bloc3")
@RequiredArgsConstructor
public class PropositionEmbaucheController {

    private final PropositionService propositionService;
    private final EmbaucheService embaucheService;
    private final CandidatureService candidatureService;

    /** Liste et suivi de toutes les propositions. */
    @GetMapping("/propositions")
    public String liste(Model model) {
        model.addAttribute("propositions", propositionService.liste());
        return "bloc3/propositions/liste";
    }

    /** Formulaire « Proposer » (bouton du bloc 2 sur une candidature retenue). */
    @GetMapping("/candidatures/{idCandidature}/proposer")
    public String formProposer(@PathVariable("idCandidature") Integer idCandidature, Model model) {
        model.addAttribute("candidature", candidatureService.getCandidatureById(idCandidature));
        return "bloc3/propositions/form";
    }

    @PostMapping("/candidatures/{idCandidature}/proposer")
    public String proposer(@PathVariable("idCandidature") Integer idCandidature,
                           @RequestParam("salaire") BigDecimal salaire,
                           @RequestParam(value = "joursValidite", defaultValue = "7") int joursValidite,
                           RedirectAttributes ra) {
        try {
            PropositionEmbauche p = propositionService.proposer(idCandidature, salaire, joursValidite);
            ra.addFlashAttribute("succes", "Proposition créée.");
            return "redirect:/bloc3/propositions/" + p.getIdProposition();
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
            return "redirect:/bloc3/candidatures/" + idCandidature + "/proposer";
        }
    }

    /** Écran de suivi d'une proposition : affiche les actions possibles selon son statut. */
    @GetMapping("/propositions/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        PropositionEmbauche p = propositionService.getProposition(id);
        model.addAttribute("proposition", p);
        model.addAttribute("alerteGrille", propositionService.verifierGrille(p));
        model.addAttribute("validations", propositionService.historiqueValidations(id));
        model.addAttribute("employe",
                embaucheService.trouverEmploye(p.getCandidature().getIdCandidature()).orElse(null));
        return "bloc3/propositions/show";
    }

    @PostMapping("/propositions/{id}/modifier-salaire")
    public String modifierSalaire(@PathVariable("id") Integer id,
                                  @RequestParam("salaire") BigDecimal salaire,
                                  RedirectAttributes ra) {
        try {
            propositionService.modifierSalaire(id, salaire);
            ra.addFlashAttribute("succes", "Salaire mis à jour.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/bloc3/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/soumettre")
    public String soumettre(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            propositionService.soumettre(id);
            ra.addFlashAttribute("succes", "Proposition soumise à validation.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/bloc3/propositions/" + id;
    }

    /** principal.getName() = email de l'utilisateur connecté. */
    @PostMapping("/propositions/{id}/valider")
    public String valider(@PathVariable("id") Integer id,
                          @RequestParam("decision") String decision,
                          @RequestParam(value = "commentaire", required = false) String commentaire,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            boolean approuve = "APPROUVER".equals(decision);
            propositionService.valider(id, principal.getName(), approuve, commentaire);
            ra.addFlashAttribute("succes", approuve
                    ? "Proposition approuvée. En attente de la réponse du candidat."
                    : "Proposition refusée : elle repasse en correction.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/bloc3/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/repondre")
    public String repondre(@PathVariable("id") Integer id,
                           @RequestParam("reponse") String reponse,
                           @RequestParam(value = "remarque", required = false) String remarque,
                           RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("succes", propositionService.repondre(id, reponse, remarque));
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/bloc3/propositions/" + id;
    }

    /** Bouton « Créer dossier » : employé + matricule + contrat + affectation. */
    @PostMapping("/propositions/{id}/dossier")
    public String creerDossier(@PathVariable("id") Integer id,
                               @RequestParam("idTypeContrat") Integer idTypeContrat,
                               @RequestParam("dateDebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                               @RequestParam(value = "dateFin", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                               RedirectAttributes ra) {
        try {
            var employe = embaucheService.creerDossier(id, idTypeContrat, dateDebut, dateFin);
            ra.addFlashAttribute("succes", "Dossier créé. Matricule : " + employe.getMatricule());
            return "redirect:/bloc3/employes/" + employe.getIdEmployer();
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
            return "redirect:/bloc3/propositions/" + id;
        }
    }

    /** Fiche salarié avec matricule, contrat et affectation. */
    @GetMapping("/employes/{id}")
    public String fiche(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("employe", embaucheService.getEmploye(id));
        model.addAttribute("contrats", embaucheService.contratsDe(id));
        model.addAttribute("affectations", embaucheService.affectationsDe(id));
        return "bloc3/employes/fiche";
    }
}