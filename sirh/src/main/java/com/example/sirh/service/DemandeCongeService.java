package com.example.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DemandeCongeFilter;
import com.example.sirh.dto.DemandeCongeForm;
import com.example.sirh.dto.ValidationDemandeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.HistoriqueDemandeConge;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.model.enums.StatutJustificatif;
import com.example.sirh.repository.AbsenceRepository;
import com.example.sirh.repository.DemandeCongeRepository;
import com.example.sirh.repository.HistoriqueDemandeCongeRepository;
import com.example.sirh.repository.JustificatifCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DemandeCongeService {

    private static final List<StatutDemandeConge> STATUTS_ACTIFS = List.of(
            StatutDemandeConge.SOUMISE,
            StatutDemandeConge.VALIDEE_MANAGER,
            StatutDemandeConge.APPROUVEE);

    private final DemandeCongeRepository demandeCongeRepository;
    private final HistoriqueDemandeCongeRepository historiqueRepository;
    private final AbsenceRepository absenceRepository;
    private final JustificatifCongeRepository justificatifRepository;
    private final TypeCongeService typeCongeService;
    private final CalculDureeCongeService calculDureeCongeService;
    private final SoldeCongeService soldeCongeService;
    private final WorkflowCongeService workflowCongeService;
    private final CongeAccessService accessService;
    private final AbsenceService absenceService;
    private final VariablePaieService variablePaieService;
    private final PlanningCongeService planningCongeService;
    private final AuditService auditService;

    public static List<StatutDemandeConge> statutsActifs() {
        return STATUTS_ACTIFS;
    }

    // ------------------------------------------------------------------
    // Création et soumission
    // ------------------------------------------------------------------

    @Transactional
    public DemandeConge creer(DemandeCongeForm form, Utilisateur utilisateur, boolean soumettre) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        Employe employe = resoudreEmploye(form, utilisateur, erreurs);
        TypeConge type = resoudreType(form, utilisateur, erreurs);

        if (form.getDateDebut() == null || form.getDateFin() == null) {
            erreurs.put("dates", "Les dates de début et de fin sont obligatoires.");
        } else if (form.getDateFin().isBefore(form.getDateDebut())) {
            erreurs.put("dates", "La date de fin ne peut pas précéder la date de début.");
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        BigDecimal nombreJours;
        try {
            nombreJours = calculDureeCongeService.calculer(
                    type, form.getDateDebut(), form.getDateFin());
        } catch (IllegalArgumentException e) {
            erreurs.put("dates", e.getMessage());
            throw new ValidationMetierException(erreurs);
        }

        verifierDureeMax(type, nombreJours, erreurs);
        verifierChevauchement(employe.getIdEmployer(),
                form.getDateDebut(), form.getDateFin(), null, erreurs);
        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        DemandeConge demande = DemandeConge.builder()
                .employe(employe)
                .typeConge(type)
                .createur(utilisateur)
                .dateDebut(form.getDateDebut())
                .dateFin(form.getDateFin())
                .nombreJours(nombreJours)
                .soldeRequis(Boolean.TRUE.equals(type.getConsommeSolde()))
                .motif(form.getMotif())
                .statut(StatutDemandeConge.BROUILLON)
                .managerRequis(true)
                .build();

        demande = demandeCongeRepository.save(demande);
        demande.setReference(reference(demande));
        demandeCongeRepository.save(demande);
        historique(demande, null, StatutDemandeConge.BROUILLON, utilisateur,
                "Création de la demande");

        if (soumettre) {
            soumettre(demande.getId(), utilisateur);
        }
        return demande;
    }

    @Transactional
    public DemandeConge soumettre(Integer id, Utilisateur utilisateur) {
        DemandeConge demande = getById(id);
        Map<String, String> erreurs = new LinkedHashMap<>();

        if (!accessService.estProprietaire(demande, utilisateur)) {
            erreurs.put("demande", "Seul le demandeur peut soumettre cette demande.");
        }
        if (demande.getStatut() != StatutDemandeConge.BROUILLON) {
            erreurs.put("demande", "Seul un brouillon peut être soumis.");
        }
        if (!Boolean.TRUE.equals(demande.getTypeConge().getActif())) {
            erreurs.put("type", "Ce type de congé est désactivé.");
        }

        BigDecimal nombreJours = calculDureeCongeService.calculer(
                demande.getTypeConge(), demande.getDateDebut(), demande.getDateFin());
        verifierDureeMax(demande.getTypeConge(), nombreJours, erreurs);
        verifierChevauchement(demande.getEmploye().getIdEmployer(),
                demande.getDateDebut(), demande.getDateFin(), demande.getId(), erreurs);

        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        StatutDemandeConge avant = demande.getStatut();
        demande.setNombreJours(nombreJours);
        demande.setSoldeRequis(Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde()));
        demande.setDateSoumission(LocalDateTime.now());

        Optional<Utilisateur> manager =
                workflowCongeService.determinerManagerAttendu(demande.getEmploye(), utilisateur);
        demande.setManagerAttendu(manager.orElse(null));
        demande.setManagerRequis(manager.isPresent());

        demande.setStatut(StatutDemandeConge.SOUMISE);
        demandeCongeRepository.save(demande);

        soldeCongeService.reserver(demande);

        historique(demande, avant, StatutDemandeConge.SOUMISE, utilisateur,
                "Soumission de la demande");

        if (manager.isEmpty()) {
            demande.setStatut(StatutDemandeConge.VALIDEE_MANAGER);
            demande.setDateValidationManager(LocalDateTime.now());
            demande.setCommentaireManager(
                    "Validation manager automatique : aucun manager distinct disponible.");
            demandeCongeRepository.save(demande);
            historique(demande, StatutDemandeConge.SOUMISE,
                    StatutDemandeConge.VALIDEE_MANAGER, utilisateur,
                    "Passage direct au contrôle RH");
        }

        audit(utilisateur, "SOUMISSION_DEMANDE_CONGE", demande.getId(),
                "Demande " + demande.getReference() + " soumise");
        return demande;
    }

    // ------------------------------------------------------------------
    // Workflow
    // ------------------------------------------------------------------

    @Transactional
    public DemandeConge validerManager(Integer id, String commentaire, Utilisateur utilisateur) {
        DemandeConge demande = getById(id);
        if (demande.getStatut() != StatutDemandeConge.SOUMISE) {
            erreur("demande", "Cette demande n'est pas en attente de validation manager.");
        }
        workflowCongeService.verifierValidationManager(demande, utilisateur);

        StatutDemandeConge avant = demande.getStatut();
        demande.setStatut(StatutDemandeConge.VALIDEE_MANAGER);
        demande.setValidateurManager(utilisateur);
        demande.setDateValidationManager(LocalDateTime.now());
        demande.setCommentaireManager(commentaire);
        demandeCongeRepository.save(demande);

        historique(demande, avant, StatutDemandeConge.VALIDEE_MANAGER, utilisateur,
                commentaire == null || commentaire.isBlank()
                        ? "Validation manager" : commentaire);
        audit(utilisateur, "VALIDATION_MANAGER_CONGE", demande.getId(),
                "Demande " + demande.getReference() + " validée par le manager");
        return demande;
    }

    @Transactional
    public DemandeConge approuver(Integer id, ValidationDemandeForm form, Utilisateur utilisateur) {
        DemandeConge demande = getById(id);
        if (demande.getStatut() != StatutDemandeConge.VALIDEE_MANAGER) {
            erreur("demande", "Cette demande n'est pas en attente de contrôle RH.");
        }
        workflowCongeService.verifierApprobationRh(demande, utilisateur);

        Map<String, String> erreurs = new LinkedHashMap<>();
        verifierChevauchement(demande.getEmploye().getIdEmployer(),
                demande.getDateDebut(), demande.getDateFin(), demande.getId(), erreurs);

        if (Boolean.TRUE.equals(demande.getTypeConge().getJustificatifRequis())
                && !justificatifRepository.existsByDemandeCongeIdAndStatut(
                        demande.getId(), StatutJustificatif.VALIDE)) {
            erreurs.put("justificatif",
                    "Un justificatif validé est obligatoire avant l'approbation finale.");
        }

        List<com.example.sirh.model.AffectationPlanning> conflits =
                planningCongeService.conflitsPourDemande(demande);
        if (!conflits.isEmpty() && (form == null || !form.isForcerConflitPlanning())) {
            erreurs.put("planning", "La période chevauche " + conflits.size()
                    + " affectation(s) de planning. Confirmez explicitement le maintien du congé.");
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        soldeCongeService.consommer(demande, utilisateur);
        Absence absence = absenceService.creerDepuisDemande(demande, utilisateur);

        StatutDemandeConge avant = demande.getStatut();
        demande.setStatut(StatutDemandeConge.APPROUVEE);
        demande.setValidateurRh(utilisateur);
        demande.setDateValidationRh(LocalDateTime.now());
        demande.setCommentaireRh(form == null ? null : form.getCommentaire());
        demandeCongeRepository.save(demande);

        variablePaieService.genererPourDemande(demande);
        historique(demande, avant, StatutDemandeConge.APPROUVEE, utilisateur,
                form == null || form.getCommentaire() == null
                        || form.getCommentaire().isBlank()
                                ? "Approbation RH" : form.getCommentaire());
        audit(utilisateur, "APPROBATION_DEMANDE_CONGE", demande.getId(),
                "Demande " + demande.getReference() + " approuvée, absence "
                        + (absence == null ? "non créée" : absence.getId()));
        return demande;
    }

    @Transactional
    public DemandeConge refuser(Integer id, ValidationDemandeForm form, Utilisateur utilisateur) {
        DemandeConge demande = getById(id);
        StatutDemandeConge statut = demande.getStatut();
        if (statut != StatutDemandeConge.SOUMISE
                && statut != StatutDemandeConge.VALIDEE_MANAGER) {
            erreur("demande", "Cette demande ne peut plus être refusée.");
        }

        if (statut == StatutDemandeConge.SOUMISE) {
            workflowCongeService.verifierValidationManager(demande, utilisateur);
        } else {
            workflowCongeService.verifierApprobationRh(demande, utilisateur);
        }

        String commentaire = form == null ? null : form.getCommentaire();
        if (commentaire == null || commentaire.isBlank()) {
            erreur("commentaire", "Le motif du refus est obligatoire.");
        }

        soldeCongeService.liberer(demande, utilisateur);

        StatutDemandeConge avant = demande.getStatut();
        demande.setStatut(StatutDemandeConge.REFUSEE);
        if (statut == StatutDemandeConge.SOUMISE) {
            demande.setValidateurManager(utilisateur);
            demande.setDateValidationManager(LocalDateTime.now());
            demande.setCommentaireManager(commentaire);
        } else {
            demande.setValidateurRh(utilisateur);
            demande.setDateValidationRh(LocalDateTime.now());
            demande.setCommentaireRh(commentaire);
        }
        demandeCongeRepository.save(demande);

        historique(demande, avant, StatutDemandeConge.REFUSEE, utilisateur, commentaire);
        audit(utilisateur, "REFUS_DEMANDE_CONGE", demande.getId(),
                "Demande " + demande.getReference() + " refusée");
        return demande;
    }

    @Transactional
    public DemandeConge annuler(Integer id, String motif, Utilisateur utilisateur) {
        DemandeConge demande = getById(id);
        StatutDemandeConge statut = demande.getStatut();

        if (statut == StatutDemandeConge.ANNULEE || statut == StatutDemandeConge.REFUSEE) {
            erreur("demande", "Cette demande est déjà clôturée.");
        }
        workflowCongeService.verifierAnnulation(demande, utilisateur);

        if (statut == StatutDemandeConge.APPROUVEE && !accessService.estRhOuDg(utilisateur)) {
            erreur("demande", "Seul un contrôleur RH ou le DG peut annuler un congé approuvé.");
        }

        if (statut == StatutDemandeConge.SOUMISE
                || statut == StatutDemandeConge.VALIDEE_MANAGER) {
            soldeCongeService.liberer(demande, utilisateur);
        } else if (statut == StatutDemandeConge.APPROUVEE) {
            soldeCongeService.annulerConsommation(demande, utilisateur);
            absenceService.annulerDepuisDemande(demande, utilisateur);
            variablePaieService.annulerPourDemande(demande);
        }

        StatutDemandeConge avant = demande.getStatut();
        demande.setStatut(StatutDemandeConge.ANNULEE);
        demandeCongeRepository.save(demande);

        historique(demande, avant, StatutDemandeConge.ANNULEE, utilisateur,
                motif == null || motif.isBlank() ? "Annulation" : motif);
        audit(utilisateur, "ANNULATION_DEMANDE_CONGE", demande.getId(),
                "Demande " + demande.getReference() + " annulée");
        return demande;
    }

    // ------------------------------------------------------------------
    // Lecture
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DemandeConge getById(Integer id) {
        return demandeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande de congé introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<DemandeConge> listerMesDemandes(Integer employeId) {
        return demandeCongeRepository.findByEmployeIdEmployerOrderByDateCreationDesc(employeId);
    }

    @Transactional(readOnly = true)
    public List<DemandeConge> rechercher(DemandeCongeFilter filter) {
        if (filter == null) {
            return demandeCongeRepository.findAll();
        }
        return demandeCongeRepository.rechercher(filter.getEmployeId(), filter.getTypeId(),
                filter.getStatut(), filter.getDebut(), filter.getFin());
    }

    @Transactional(readOnly = true)
    public List<DemandeConge> fileValidation(Utilisateur utilisateur) {
        if (accessService.estRhOuDg(utilisateur)) {
            return demandeCongeRepository.findByStatutInOrderByDateCreationAsc(
                    List.of(StatutDemandeConge.SOUMISE, StatutDemandeConge.VALIDEE_MANAGER));
        }
        return demandeCongeRepository
                .findByManagerAttenduIdUtilisateurAndStatutInOrderByDateCreationAsc(
                        utilisateur.getIdUtilisateur(), List.of(StatutDemandeConge.SOUMISE));
    }

    @Transactional(readOnly = true)
    public List<HistoriqueDemandeConge> historique(Integer demandeId) {
        return historiqueRepository.findByDemandeCongeIdOrderByDateChangementAsc(demandeId);
    }

    @Transactional(readOnly = true)
    public long compterEnAttente(Utilisateur utilisateur) {
        if (accessService.estRhOuDg(utilisateur)) {
            return demandeCongeRepository.countByStatutIn(
                    List.of(StatutDemandeConge.SOUMISE, StatutDemandeConge.VALIDEE_MANAGER));
        }
        return demandeCongeRepository.countByManagerAttenduIdUtilisateurAndStatutIn(
                utilisateur.getIdUtilisateur(), List.of(StatutDemandeConge.SOUMISE));
    }

    // ------------------------------------------------------------------
    // Règles internes
    // ------------------------------------------------------------------

    private Employe resoudreEmploye(DemandeCongeForm form, Utilisateur utilisateur,
                                    Map<String, String> erreurs) {
        // Le parcours salarié est strictement self-service : on ignore un idEmploye
        // fourni par le formulaire pour empêcher toute demande au nom d'un autre salarié.
        try {
            return accessService.employeCourant();
        } catch (ValidationMetierException e) {
            erreurs.putAll(e.getErrors());
            return null;
        }
    }

    private TypeConge resoudreType(DemandeCongeForm form, Utilisateur utilisateur,
                                   Map<String, String> erreurs) {
        if (form.getIdTypeConge() == null) {
            erreurs.put("typeConge", "Le type de congé est obligatoire.");
            return null;
        }
        TypeConge type = typeCongeService.getById(form.getIdTypeConge());
        if (!Boolean.TRUE.equals(type.getActif())) {
            erreurs.put("typeConge", "Ce type de congé est désactivé.");
        }
        if (type.getCategorie() != CategorieTypeConge.CONGE) {
            erreurs.put("typeConge", "Ce type n'est pas un congé.");
        }
        if (!accessService.estRhOuDg(utilisateur)
                && !Boolean.TRUE.equals(type.getDemandeEmployeAutorisee())) {
            erreurs.put("typeConge", "Vous n'êtes pas autorisé à demander ce type de congé.");
        }
        return type;
    }

    private void verifierDureeMax(TypeConge type, BigDecimal nombreJours,
                                  Map<String, String> erreurs) {
        if (type.getDureeMaxJours() != null
                && nombreJours.compareTo(type.getDureeMaxJours()) > 0) {
            erreurs.put("dates", "La durée demandée (" + nombreJours
                    + " jours) dépasse le maximum autorisé pour ce type ("
                    + type.getDureeMaxJours() + " jours).");
        }
    }

    public void verifierChevauchement(Integer employeId, LocalDate debut, LocalDate fin,
                                      Integer demandeId, Map<String, String> erreurs) {
        List<DemandeConge> demandes = demandeCongeRepository.findChevauchements(
                employeId, debut, fin, STATUTS_ACTIFS, demandeId);
        if (!demandes.isEmpty()) {
            DemandeConge conflit = demandes.get(0);
            erreurs.put("chevauchement", "Chevauchement avec la demande "
                    + conflit.getReference() + " (" + conflit.getTypeConge().getLibelle()
                    + ", du " + conflit.getDateDebut() + " au " + conflit.getDateFin()
                    + ", statut " + conflit.getStatut() + ").");
            return;
        }

        List<Absence> absences = absenceRepository.findChevauchements(
                employeId, debut, fin, null);
        for (Absence absence : absences) {
            if (absence.getStatut() == StatutAbsence.ANNULEE) {
                continue;
            }
            if (demandeId != null && absence.getDemandeConge() != null
                    && demandeId.equals(absence.getDemandeConge().getId())) {
                continue;
            }
            erreurs.put("chevauchement", "Chevauchement avec l'absence "
                    + absence.getTypeConge().getLibelle() + " du "
                    + absence.getDateDebut() + " au " + absence.getDateFin() + ".");
            return;
        }
    }

    private void historique(DemandeConge demande, StatutDemandeConge avant,
                            StatutDemandeConge apres, Utilisateur utilisateur,
                            String commentaire) {
        historiqueRepository.save(HistoriqueDemandeConge.builder()
                .demandeConge(demande)
                .statutAvant(avant)
                .statutApres(apres)
                .utilisateur(utilisateur)
                .commentaire(commentaire)
                .build());
    }

    private String reference(DemandeConge demande) {
        return "CG-" + demande.getDateDebut().getYear() + "-"
                + String.format("%06d", demande.getId());
    }

    private void erreur(String champ, String message) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        erreurs.put(champ, message);
        throw new ValidationMetierException(erreurs);
    }

    private void audit(Utilisateur utilisateur, String action, Integer id, String details) {
        if (utilisateur == null || utilisateur.getEmail() == null) {
            return;
        }
        try {
            auditService.enregistrerAction(utilisateur.getEmail(), action,
                    "demande_conge", id, details);
        } catch (RuntimeException ignored) {
            // L'audit ne doit pas bloquer le workflow.
        }
    }
}
