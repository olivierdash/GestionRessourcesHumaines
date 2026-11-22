package com.example.sirh.controller.api.v1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.example.sirh.dto.DemandeExplicationDto;
import com.example.sirh.dto.IncidentDto;
import com.example.sirh.model.DemandeExplication;
import com.example.sirh.model.Incident;
import com.example.sirh.model.IncidentDocument;
import com.example.sirh.model.ReponseEtDecision;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DemandeExplicationReposiroty;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.service.AuditService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DemandeExplicationService;
import com.example.sirh.service.IncidentDocumentService;
import com.example.sirh.service.IncidentService;
import com.example.sirh.service.ReponseEtDecisionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discipline")
@RequiredArgsConstructor
@Transactional
public class DisciplineController {

    private static final Set<String> GRAVITES = Set.of("FAIBLE", "MOYENNE", "GRAVE", "TRES_GRAVE");
    private static final Set<String> SANCTIONS = Set.of(
            "AUCUNE", "AVERTISSEMENT", "BLAME", "MISE_A_PIED", "LICENCIEMENT");
    private static final Set<String> TYPES_PIECE = Set.of(
            "RAPPORT", "TEMOIGNAGE", "COURRIER", "PREUVE", "AUTRE");

    private final IncidentService incidentService;
    private final DemandeExplicationService demandeService;
    private final ReponseEtDecisionService decisionService;
    private final IncidentDocumentService documentService;
    private final CongeAccessService accessService;
    private final AuditService auditService;
    private final EmployeRepository employeRepository;
    private final DemandeExplicationReposiroty demandeRepository;

    @GetMapping("/incidents")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    public List<IncidentDto> listerIncidents(
            @RequestParam(required = false) Integer idEmployer,
            @RequestParam(required = false) String statut) {
        Utilisateur acteur = accessService.utilisateurCourant();
        List<Incident> incidents = idEmployer != null
                ? incidentService.findByEmploye(idEmployer)
                : statut != null && !statut.isBlank()
                        ? incidentService.findByStatut(statut)
                        : incidentService.lister();
        if (idEmployer != null && statut != null && !statut.isBlank()) {
            incidents = incidents.stream()
                    .filter(incident -> statut.equals(incident.getStatutInstruction()))
                    .toList();
        }
        auditService.enregistrerConsultationSensible(acteur, "incident", 0,
                "Consultation de la liste des incidents");
        return incidents.stream().map(this::versDto).toList();
    }

    @GetMapping("/incidents/{id}")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    public IncidentDto consulterIncident(@PathVariable Integer id) {
        Incident incident = incidentService.findById(id);
        auditService.enregistrerConsultationSensible(
                accessService.utilisateurCourant(), "incident", id, "Consultation d'un incident");
        return versDto(incident);
    }

    @PostMapping("/incidents")
    @PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentDto declarerIncident(@Valid @RequestBody IncidentRequest request) {
        var employe = employeRepository.findById(request.idEmployer())
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable"));
        Utilisateur acteur = accessService.utilisateurCourant();
        if (accessService.estManager(acteur)
                && !accessService.managerDuDepartement(acteur, employe)) {
            throw new AccessDeniedException("Vous ne pouvez déclarer un incident que pour votre département.");
        }
        auditService.definirUtilisateurCourant(acteur);
        Incident incident = Incident.builder()
                .employe(employe)
                .declarant(acteur)
                .dateFait(request.dateFait())
                .descriptions(request.descriptions())
                .gravite(validerValeur(request.gravite(), GRAVITES, "gravité"))
                .statutInstruction("OUVERT")
                .build();
        return versDto(incidentService.declarer(incident));
    }

    @GetMapping("/incidents/{id}/demande-explication")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    public DemandeExplicationDto consulterDemande(@PathVariable Integer id) {
        DemandeExplication demande = demandeService.findByIncident(id);
        if (demande == null) {
            throw new IllegalArgumentException("Aucune demande d'explication pour cet incident");
        }
        auditService.enregistrerConsultationSensible(
                accessService.utilisateurCourant(), "demande_explication",
                demande.getIdDemande(), "Consultation d'une demande d'explication");
        return versDto(demande);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("isAuthenticated()")
    public List<DemandeExplicationDto> mesDemandes() {
        Utilisateur acteur = accessService.utilisateurCourant();
        if (acteur.getEmploye() == null) {
            throw new AccessDeniedException("Aucun dossier salarié n'est associé à ce compte.");
        }
        List<DemandeExplication> demandes = demandeRepository
                .findByIncidentEmployeIdEmployer(acteur.getEmploye().getIdEmployer());
        auditService.enregistrerConsultationSensible(acteur, "demande_explication", 0,
                "Consultation des demandes d'explication personnelles");
        return demandes.stream().map(this::versDto).toList();
    }

    @PostMapping("/incidents/{id}/demande-explication")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeExplicationDto envoyerDemande(
            @PathVariable Integer id, @Valid @RequestBody DemandeRequest request) {
        Incident incident = incidentService.findById(id);
        Utilisateur acteur = accessService.utilisateurCourant();
        auditService.definirUtilisateurCourant(acteur);
        DemandeExplication demande = DemandeExplication.builder()
                .incident(incident)
                .envoyeur(acteur)
                .dateEnvoi(request.dateEnvoi())
                .dateLimiteReponse(request.dateLimiteReponse())
                .motif(request.motif())
                .build();
        return versDto(demandeService.envoyer(demande));
    }

    @PostMapping("/demandes/{idDemande}/reponse")
    @PreAuthorize("isAuthenticated()")
    public DecisionDto repondre(
            @PathVariable Integer idDemande, @Valid @RequestBody ReponseRequest request) {
        Utilisateur acteur = accessService.utilisateurCourant();
        if (acteur.getEmploye() == null) {
            throw new AccessDeniedException("Aucun dossier salarié n'est associé à ce compte.");
        }
        DemandeExplication demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (!acteur.getEmploye().getIdEmployer()
                .equals(demande.getIncident().getEmploye().getIdEmployer())) {
            throw new AccessDeniedException("Cette demande ne concerne pas votre dossier.");
        }
        auditService.definirUtilisateurCourant(acteur);
        ReponseEtDecision reponse = decisionService.enregistrerReponse(demande, request.contenuReponse());
        return versDto(reponse);
    }

    @GetMapping("/demandes/{idDemande}/decision")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    public DecisionDto consulterDecision(@PathVariable Integer idDemande) {
        ReponseEtDecision decision = decisionService.findByDemande(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Réponse ou décision introuvable"));
        auditService.enregistrerConsultationSensible(accessService.utilisateurCourant(),
                "reponse_et_decision", decision.getIdDecision(),
                "Consultation d'une réponse et décision disciplinaire");
        return versDto(decision);
    }

    @PostMapping("/demandes/{idDemande}/decision")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    @ResponseStatus(HttpStatus.CREATED)
    public DecisionDto enregistrerDecision(
            @PathVariable Integer idDemande, @Valid @RequestBody DecisionRequest request) {
        DemandeExplication demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        Utilisateur acteur = accessService.utilisateurCourant();
        auditService.definirUtilisateurCourant(acteur);
        ReponseEtDecision decision = ReponseEtDecision.builder()
                .demande(demande)
                .dateReponse(request.dateReponse())
                .contenuReponse(request.contenuReponse())
                .typeSanction(validerValeur(request.typeSanction(), SANCTIONS, "sanction"))
                .dateNotification(request.dateNotification())
                .commentaireDecision(request.commentaireDecision())
                .build();
        return versDto(decisionService.enregistrer(decision));
    }

    @GetMapping("/incidents/{id}/documents")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    public List<IncidentDocumentDto> listerDocuments(@PathVariable Integer id) {
        incidentService.findById(id);
        auditService.enregistrerConsultationSensible(
                accessService.utilisateurCourant(), "incident_document", id,
                "Consultation des pièces d'un incident");
        return documentService.findByIncident(id).stream().map(this::versDto).toList();
    }

    @PostMapping("/incidents/{id}/documents")
    @PreAuthorize("hasAnyRole('RH', 'DG')")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentDocumentDto ajouterDocument(
            @PathVariable Integer id, @Valid @RequestBody DocumentRequest request) {
        IncidentDocument document = IncidentDocument.builder()
                .incident(incidentService.findById(id))
                .nomFichier(request.nomFichier())
                .fichierUrl(request.fichierUrl())
                .typePiece(validerValeur(request.typePiece(), TYPES_PIECE, "type de pièce"))
                .deposePar(accessService.utilisateurCourant())
                .dateDepot(LocalDateTime.now())
                .build();
        Utilisateur acteur = document.getDeposePar();
        auditService.definirUtilisateurCourant(acteur);
        IncidentDocument ajoute = documentService.ajouter(document);
        auditService.enregistrerActionSensible(acteur, "AJOUT", "incident_document",
                ajoute.getId(), "Ajout d'une pièce à un incident");
        return versDto(ajoute);
    }

    private IncidentDto versDto(Incident incident) {
        var employe = incident.getEmploye();
        var candidat = employe.getCandidature().getCandidat();
        return IncidentDto.builder()
                .idIncident(incident.getIdIncident())
                .idEmployer(employe.getIdEmployer())
                .employeNom(candidat.getPrenom() + " " + candidat.getNom())
                .matricule(employe.getMatricule())
                .dateFait(incident.getDateFait())
                .descriptions(incident.getDescriptions())
                .gravite(incident.getGravite())
                .statutInstruction(incident.getStatutInstruction())
                .declarantNom(incident.getDeclarant().getNom())
                .build();
    }

    private DemandeExplicationDto versDto(DemandeExplication demande) {
        var employe = demande.getIncident().getEmploye();
        var candidat = employe.getCandidature().getCandidat();
        return DemandeExplicationDto.builder()
                .idDemande(demande.getIdDemande())
                .idIncident(demande.getIncident().getIdIncident())
                .employeNom(candidat.getPrenom() + " " + candidat.getNom())
                .matricule(employe.getMatricule())
                .dateEnvoi(demande.getDateEnvoi())
                .dateLimiteReponse(demande.getDateLimiteReponse())
                .motif(demande.getMotif())
                .statutIncident(demande.getIncident().getStatutInstruction())
                .dejaRepondu(demande.getReponseEtDecision() != null)
                .build();
    }

    private IncidentDocumentDto versDto(IncidentDocument document) {
        return new IncidentDocumentDto(
                document.getId(),
                document.getIncident().getIdIncident(),
                document.getNomFichier(),
                document.getFichierUrl(),
                document.getTypePiece(),
                document.getDeposePar().getNom(),
                document.getDateDepot());
    }

    private DecisionDto versDto(ReponseEtDecision decision) {
        return new DecisionDto(
                decision.getIdDecision(),
                decision.getDemande().getIdDemande(),
                decision.getDateReponse(),
                decision.getContenuReponse(),
                decision.getTypeSanction(),
                decision.getDateNotification(),
                decision.getCommentaireDecision());
    }

    private String validerValeur(String valeur, Set<String> valeurs, String nom) {
        if (!valeurs.contains(valeur)) {
            throw new IllegalArgumentException("Valeur de " + nom + " invalide");
        }
        return valeur;
    }

    public record IncidentRequest(
            @NotNull Integer idEmployer,
            @NotNull LocalDate dateFait,
            @NotBlank @Size(max = 500) String descriptions,
            @NotBlank String gravite) {}

    public record DemandeRequest(
            LocalDate dateEnvoi,
            @NotNull LocalDate dateLimiteReponse,
            @NotBlank @Size(max = 500) String motif) {}

    public record DecisionRequest(
            LocalDate dateReponse,
            @Size(max = 500) String contenuReponse,
            @NotBlank String typeSanction,
            LocalDate dateNotification,
            @Size(max = 500) String commentaireDecision) {}

    public record ReponseRequest(
            @NotBlank @Size(max = 500) String contenuReponse) {}

    public record DocumentRequest(
            @NotBlank @Size(max = 255) String nomFichier,
            @NotBlank @Size(max = 500) String fichierUrl,
            @NotBlank @Pattern(regexp = "RAPPORT|TEMOIGNAGE|COURRIER|PREUVE|AUTRE") String typePiece) {}

    public record IncidentDocumentDto(
            Integer id,
            Integer idIncident,
            String nomFichier,
            String fichierUrl,
            String typePiece,
            String deposePar,
            LocalDateTime dateDepot) {}

    public record DecisionDto(
            Integer idDecision,
            Integer idDemande,
            LocalDate dateReponse,
            String contenuReponse,
            String typeSanction,
            LocalDate dateNotification,
            String commentaireDecision) {}
}
