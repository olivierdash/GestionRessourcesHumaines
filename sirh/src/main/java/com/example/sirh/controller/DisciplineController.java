package com.example.sirh.controller;

import com.example.sirh.model.DemandeExplication;
import com.example.sirh.model.Incident;
import com.example.sirh.model.ReponseEtDecision;
import com.example.sirh.repository.DemandeExplicationReposiroty;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.IncidentDocumentService;
import com.example.sirh.service.IncidentService;
import com.example.sirh.service.DemandeExplicationService;
import com.example.sirh.service.ReponseEtDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller("disciplineMvcController")
@RequestMapping("/discipline")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH','DG','MANAGER')")
public class DisciplineController {
    private final IncidentService incidentService;
    private final DemandeExplicationService demandeService;
    private final ReponseEtDecisionService decisionService;
    private final IncidentDocumentService documentService;
    private final EmployeRepository employeRepository;
    private final DemandeExplicationReposiroty demandeRepository;
    private final CongeAccessService accessService;

    @GetMapping
    public String liste(@RequestParam(required=false) String statut, Model model) {
        List<Incident> incidents = statut == null || statut.isBlank()
                ? incidentService.lister() : incidentService.findByStatut(statut);
        model.addAttribute("incidents", incidents);
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("statutFiltre", statut);
        return "discipline/index";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("employes", employeRepository.findAll());
        return "discipline/form";
    }

    @PostMapping("/incidents")
    public String creer(@RequestParam Integer idEmployer,
                        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateFait,
                        @RequestParam String descriptions,
                        @RequestParam String gravite,
                        RedirectAttributes redirect) {
        try {
            Incident incident = Incident.builder()
                    .employe(employeRepository.findById(idEmployer).orElseThrow())
                    .declarant(accessService.utilisateurCourant())
                    .dateFait(dateFait)
                    .descriptions(descriptions)
                    .gravite(gravite)
                    .statutInstruction("OUVERT")
                    .build();
            incidentService.declarer(incident);
            redirect.addFlashAttribute("succes", "Incident déclaré.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/discipline";
    }

    @GetMapping("/incidents/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Incident incident = incidentService.findById(id);
        DemandeExplication demande = demandeService.findByIncident(id);
        model.addAttribute("incident", incident);
        model.addAttribute("demande", demande);
        model.addAttribute("documents", documentService.findByIncident(id));
        model.addAttribute("decision", demande == null ? null
                : decisionService.findByDemande(demande.getIdDemande()).orElse(null));
        return "discipline/detail";
    }

    @PostMapping("/incidents/{id}/demande")
    @PreAuthorize("hasAnyRole('RH','DG')")
    public String envoyerDemande(@PathVariable Integer id,
                                 @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateLimiteReponse,
                                 @RequestParam String motif,
                                 RedirectAttributes redirect) {
        try {
            DemandeExplication demande = DemandeExplication.builder()
                    .incident(incidentService.findById(id))
                    .envoyeur(accessService.utilisateurCourant())
                    .dateEnvoi(LocalDate.now())
                    .dateLimiteReponse(dateLimiteReponse)
                    .motif(motif)
                    .build();
            demandeService.envoyer(demande);
            redirect.addFlashAttribute("succes", "Demande d'explication envoyée.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/discipline/incidents/" + id;
    }

    @PostMapping("/demandes/{id}/decision")
    @PreAuthorize("hasAnyRole('RH','DG')")
    public String decision(@PathVariable Integer id,
                           @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateNotification,
                           @RequestParam String typeSanction,
                           @RequestParam(required=false) String commentaireDecision,
                           RedirectAttributes redirect) {
        try {
            DemandeExplication demande = demandeRepository.findById(id).orElseThrow();
            ReponseEtDecision decision = ReponseEtDecision.builder()
                    .demande(demande)
                    .contenuReponse(demande.getReponseEtDecision() == null ? null : demande.getReponseEtDecision().getContenuReponse())
                    .typeSanction(typeSanction)
                    .dateNotification(dateNotification)
                    .commentaireDecision(commentaireDecision)
                    .build();
            decisionService.enregistrer(decision);
            redirect.addFlashAttribute("succes", "Décision enregistrée.");
            return "redirect:/discipline/incidents/" + demande.getIncident().getIdIncident();
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/discipline";
        }
    }
}
