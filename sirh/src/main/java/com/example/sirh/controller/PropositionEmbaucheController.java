package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.constants.ConstantSirh.Action;
import com.example.sirh.constants.ConstantSirh.Role;
import com.example.sirh.constants.ConstantSirh.Statut;
import com.example.sirh.dto.PropositionEmbaucheFilter;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.DepartementService;
import com.example.sirh.service.EmbaucheService;
import com.example.sirh.service.GrilleSalarialService;
import com.example.sirh.service.PropositionMessageService;
import com.example.sirh.service.PropositionService;
import com.example.sirh.service.StatutPropositionService;
import com.example.sirh.service.TypeContratService;
import com.example.sirh.service.ValidationPropositionService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static com.example.sirh.constants.ConstantSirh.Action;
import static com.example.sirh.constants.ConstantSirh.Role;
import static com.example.sirh.constants.ConstantSirh.Statut;

@Controller
@RequiredArgsConstructor
public class PropositionEmbaucheController {

    private final PropositionService propositionService;
    private final EmbaucheService embaucheService;
    private final CandidatureService candidatureService;
    private final ValidationPropositionService validationPropositionService;
    private final TypeContratService typeContratService;
    private final GrilleSalarialService grilleSalarialService;
    private final StatutPropositionService statutPropositionService;
    private final DepartementService departementService;
    private final PropositionMessageService propositionMessageService;

    @GetMapping("/propositions")
    public String liste(@ModelAttribute("filter") PropositionEmbaucheFilter filter,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("propositions", propositionService.liste(filter));
        model.addAttribute("statuts", statutPropositionService.getStatuts());
        model.addAttribute("departements", departementService.getAllDepartements());

        String role = userDetails.getUtilisateur().getRole().getLibelle();
        model.addAttribute("estValidateur",
                Role.DG.equals(role) || Role.FINANCE.equals(role));

        return "propositions/liste";
    }

    @GetMapping("/candidatures/{idCandidature}/proposer")
    public String formProposer(@PathVariable("idCandidature") Integer idCandidature, Model model) {
        Candidature c = candidatureService.getCandidatureById(idCandidature);
        Poste poste = c.getDemandeRecrutement().getPoste();

        grilleSalarialService.getGrillePourPosteEtDepartement(
                poste.getIdPoste(), poste.getDepartement().getIdDepartement())
                .ifPresent(g -> {
                    model.addAttribute("salaireMin", g.getSalaireMin());
                    model.addAttribute("salaireMax", g.getSalaireMax());
                    model.addAttribute("salaireDefaut", g.getSalaireMin()
                            .add(g.getSalaireMax())
                            .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
                });

        model.addAttribute("candidature", c);
        return "propositions/form";
    }

    @PostMapping("/candidatures/{idCandidature}/proposer")
    public String proposer(@PathVariable("idCandidature") Integer idCandidature,
            @RequestParam("salaire") BigDecimal salaire,
            @RequestParam(value = "joursValidite", defaultValue = "7") int joursValidite,
            RedirectAttributes ra) {
        try {
            PropositionEmbauche p = propositionService.proposer(idCandidature, salaire, joursValidite);
            ra.addFlashAttribute("succes", "Proposition créée.");
            return "redirect:/propositions/" + p.getIdProposition();
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
            return "redirect:/candidatures/" + idCandidature + "/proposer";
        }
    }

    @GetMapping("/propositions/{id}")
    public String detail(@PathVariable("id") Integer id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PropositionEmbauche p = propositionService.getProposition(id);
        String statut = p.getStatut().getLibelle();

        model.addAttribute("proposition", p);
        model.addAttribute("estEnAttente", Statut.EN_ATTENTE.equals(statut));
        model.addAttribute("estSoumise", Statut.SOUMISE.equals(statut));
        model.addAttribute("estApprouvee", Statut.APPROUVEE.equals(statut));
        model.addAttribute("estAcceptee", Statut.ACCEPTE.equals(statut));
        model.addAttribute("estCloturee", Statut.REFUSE.equals(statut)
                || Statut.EXPIREE.equals(statut));

        String role = userDetails.getUtilisateur().getRole().getLibelle();
        boolean peutValider = Statut.SOUMISE.equals(statut)
                && (Role.DG.equals(role) || Role.FINANCE.equals(role));
        model.addAttribute("peutValider", peutValider);

        model.addAttribute("alerteGrille", propositionService.verifierGrille(p));
        model.addAttribute("validations", validationPropositionService.findByProposition(id));
        model.addAttribute("employe",
                embaucheService.trouverEmploye(p.getCandidature().getIdCandidature()).orElse(null));
        model.addAttribute("typesContrat", typeContratService.getAllTypes());

        // ⚠️ LIGNES MANQUANTES
        model.addAttribute("messages", propositionMessageService.lister(id));
        model.addAttribute("attendReponseRh", propositionMessageService.attendReponseRh(id));

        return "propositions/show";
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
        return "redirect:/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/soumettre")
    public String soumettre(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            propositionService.soumettre(id);
            ra.addFlashAttribute("succes", "Proposition soumise à validation.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/valider")
    public String valider(@PathVariable("id") Integer id,
            @RequestParam("decision") String decision,
            @RequestParam(value = "commentaire", required = false) String commentaire,
            Principal principal,
            RedirectAttributes ra) {
        try {
            boolean approuve = Action.APPROUVER.equals(decision);
            propositionService.valider(id, principal.getName(), approuve, commentaire);
            ra.addFlashAttribute("succes", approuve
                    ? "Proposition approuvée. En attente de la réponse du candidat."
                    : "Proposition refusée : elle repasse en correction.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/propositions/" + id;
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
        return "redirect:/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/dossier")
    public String creerDossier(@PathVariable("id") Integer id,
            @RequestParam("dateDebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(value = "dateFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            RedirectAttributes ra) {
        try {
            var employe = embaucheService.creerDossier(id, dateDebut, dateFin);
            ra.addFlashAttribute("succes", "Dossier créé. Matricule : " + employe.getMatricule());
            return "redirect:/employes/" + employe.getIdEmployer();
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erreur", ex.getMessage());
            return "redirect:/propositions/" + id;
        }
    }
}