package com.example.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.AbsenceFilter;
import com.example.sirh.dto.AbsenceForm;
import com.example.sirh.dto.CouvertureAbsenceDto;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.SourceAbsence;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.model.enums.StatutJustificatif;
import com.example.sirh.repository.AbsenceRepository;
import com.example.sirh.repository.DemandeCongeRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.JustificatifCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private static final List<StatutDemandeConge> STATUTS_DEMANDE_ACTIFS = List.of(
            StatutDemandeConge.SOUMISE,
            StatutDemandeConge.VALIDEE_MANAGER,
            StatutDemandeConge.APPROUVEE);

    private final AbsenceRepository absenceRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final JustificatifCongeRepository justificatifRepository;
    private final TypeCongeService typeCongeService;
    private final CalculDureeCongeService calculDureeCongeService;
    private final CongeAccessService accessService;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Création depuis une demande approuvée
    // ------------------------------------------------------------------

    @Transactional
    public Absence creerDepuisDemande(DemandeConge demande, Utilisateur validateur) {
        Optional<Absence> existante = absenceRepository.findByDemandeCongeId(demande.getId());
        if (existante.isPresent()) {
            return existante.get();
        }
        Absence absence = Absence.builder()
                .employe(demande.getEmploye())
                .typeConge(demande.getTypeConge())
                .demandeConge(demande)
                .source(SourceAbsence.DEMANDE_CONGE)
                .dateDebut(demande.getDateDebut())
                .dateFin(demande.getDateFin())
                .demiJourDebut(demande.getDemiJourDebut())
                .demiJourFin(demande.getDemiJourFin())
                .statut(StatutAbsence.JUSTIFIEE)
                .motif(demande.getMotif())
                .createur(validateur)
                .build();
        return absenceRepository.save(absence);
    }

    @Transactional
    public void annulerDepuisDemande(DemandeConge demande, Utilisateur utilisateur) {
        absenceRepository.findByDemandeCongeId(demande.getId()).ifPresent(absence -> {
            if (absence.getStatut() != StatutAbsence.ANNULEE) {
                absence.setStatut(StatutAbsence.ANNULEE);
                absenceRepository.save(absence);
            }
        });
    }

    // ------------------------------------------------------------------
    // Saisie manuelle
    // ------------------------------------------------------------------

    @Transactional
    public Absence saisir(AbsenceForm form, Utilisateur utilisateur) {
        Map<String, String> erreurs = new LinkedHashMap<>();

        if (form.getIdEmploye() == null) {
            erreurs.put("employe", "Le salarié est obligatoire.");
        }
        if (form.getIdTypeConge() == null) {
            erreurs.put("typeConge", "Le type d'absence est obligatoire.");
        }
        if (form.getDateDebut() == null || form.getDateFin() == null) {
            erreurs.put("dates", "Les dates de début et de fin sont obligatoires.");
        } else if (form.getDateFin().isBefore(form.getDateDebut())) {
            erreurs.put("dates", "La date de fin ne peut pas précéder la date de début.");
        }
        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        Employe employe = employeRepository.findById(form.getIdEmploye())
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable."));
        TypeConge type = typeCongeService.getById(form.getIdTypeConge());

        if (type.getCategorie() != CategorieTypeConge.ABSENCE) {
            erreurs.put("typeConge", "Choisissez un type de la catégorie « absence ».");
        }
        if (!Boolean.TRUE.equals(type.getActif())) {
            erreurs.put("typeConge", "Ce type d'absence est désactivé.");
        }
        if (!accessService.estRhOuDg(utilisateur)
                && !accessService.estAdmin(utilisateur)
                && !accessService.managerDuDepartement(utilisateur, employe)
                && !accessService.estEmployeCourant(employe)) {
            erreurs.put("employe", "Vous ne pouvez saisir une absence que pour vous-même ou votre équipe.");
        }

        BigDecimal nombreJours;
        try {
            nombreJours = calculDureeCongeService.calculer(
                    type, form.getDateDebut(), form.getDateFin());
        } catch (IllegalArgumentException e) {
            erreurs.put("dates", e.getMessage());
            nombreJours = BigDecimal.ZERO;
        }

        verifierChevauchement(employe.getIdEmployer(), form.getDateDebut(),
                form.getDateFin(), null, erreurs);

        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        LocalDate dateLimite = null;
        if (Boolean.TRUE.equals(type.getJustificatifRequis())) {
            int delai = type.getDelaiJustificatifJours() == null
                    ? 0 : type.getDelaiJustificatifJours();
            dateLimite = form.getDateFin().plusDays(delai);
        }

        Absence absence = Absence.builder()
                .employe(employe)
                .typeConge(type)
                .source(SourceAbsence.SAISIE_MANUELLE)
                .dateDebut(form.getDateDebut())
                .dateFin(form.getDateFin())
                .statut(StatutAbsence.DECLAREE)
                .motif(form.getMotif())
                .createur(utilisateur)
                .build();

        absence = absenceRepository.save(absence);
        audit(utilisateur, "SAISIE_ABSENCE", absence.getId(),
                "Absence du " + absence.getDateDebut() + " au " + absence.getDateFin());
        return absence;
    }

    // ------------------------------------------------------------------
    // Justification et annulation
    // ------------------------------------------------------------------

    @Transactional
    public Absence justifier(Integer id, Utilisateur utilisateur) {
        Absence absence = getById(id);
        if (!accessService.peutGererAbsence(absence, utilisateur)) {
            erreur("absence", "Vous n'êtes pas autorisé à justifier cette absence.");
        }
        if (absence.getStatut() == StatutAbsence.JUSTIFIEE) {
            return absence;
        }
        if (absence.getStatut() == StatutAbsence.ANNULEE) {
            erreur("absence", "Une absence annulée ne peut pas être justifiée.");
        }
        if (Boolean.TRUE.equals(absence.getTypeConge().getJustificatifRequis())
                && !justificatifRepository.existsByAbsenceIdAndStatut(
                        absence.getId(), StatutJustificatif.VALIDE)) {
            erreur("absence",
                    "Un justificatif validé est obligatoire pour justifier cette absence.");
        }

        absence.setStatut(StatutAbsence.JUSTIFIEE);
        absenceRepository.save(absence);
        audit(utilisateur, "JUSTIFICATION_ABSENCE", absence.getId(),
                "Absence justifiée");
        return absence;
    }

    @Transactional
    public Absence marquerNonJustifiee(Integer id, Utilisateur utilisateur) {
        Absence absence = getById(id);
        if (!accessService.peutGererAbsence(absence, utilisateur)) {
            erreur("absence", "Vous n'êtes pas autorisé à modifier cette absence.");
        }
        if (absence.getStatut() == StatutAbsence.ANNULEE) {
            erreur("absence", "Une absence annulée ne peut pas être modifiée.");
        }
        absence.setStatut(StatutAbsence.NON_JUSTIFIEE);
        absenceRepository.save(absence);
        audit(utilisateur, "ABSENCE_NON_JUSTIFIEE", absence.getId(),
                "Absence marquée non justifiée");
        return absence;
    }

    @Transactional
    public Absence annuler(Integer id, String motif, Utilisateur utilisateur) {
        Absence absence = getById(id);
        if (!accessService.peutGererAbsence(absence, utilisateur)) {
            erreur("absence", "Vous n'êtes pas autorisé à annuler cette absence.");
        }
        if (absence.getStatut() == StatutAbsence.ANNULEE) {
            return absence;
        }
        absence.setStatut(StatutAbsence.ANNULEE);
        absenceRepository.save(absence);
        audit(utilisateur, "ANNULATION_ABSENCE", absence.getId(),
                motif == null || motif.isBlank() ? "Annulation" : motif);
        return absence;
    }

    // ------------------------------------------------------------------
    // Lecture
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Absence getById(Integer id) {
        return absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Absence introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Absence> lister(AbsenceFilter filter) {
        if (filter == null) {
            return absenceRepository.findAllByOrderByDateDebutDesc();
        }
        return absenceRepository.rechercher(filter.getEmployeId(), filter.getTypeId(),
                filter.getStatut(), filter.getDebut(), filter.getFin());
    }

    @Transactional(readOnly = true)
    public List<Absence> absencesDe(Integer employeId) {
        return absenceRepository.findByEmployeIdEmployerOrderByDateDebutDesc(employeId);
    }

    @Transactional(readOnly = true)
    public Optional<CouvertureAbsenceDto> couverture(Integer employeId, LocalDate date) {
        List<Absence> absences = absenceRepository.findCouvertures(employeId, date);
        if (absences.isEmpty()) {
            return Optional.empty();
        }
        Absence absence = absences.get(0);
        CouvertureAbsenceDto dto = new CouvertureAbsenceDto();
        dto.setAbsenceId(absence.getId());
        dto.setDemandeId(absence.getDemandeConge() == null
                ? null : absence.getDemandeConge().getId());
        dto.setLibelle(absence.getTypeConge().getLibelle());
        dto.setCategorie(absence.getTypeConge().getCategorie());
        dto.setStatut(absence.getStatut());
        dto.setDateDebut(absence.getDateDebut());
        dto.setDateFin(absence.getDateFin());
        dto.setJustifiee(absence.getStatut() == StatutAbsence.JUSTIFIEE);
        return Optional.of(dto);
    }

    @Transactional(readOnly = true)
    public List<Absence> absencesEnAttenteJustificatif() {
        return absenceRepository.findByStatutOrderByDateDebutAsc(StatutAbsence.DECLAREE);
    }

    @Transactional(readOnly = true)
    public List<Absence> absencesPeriode(List<Integer> employeIds, LocalDate debut, LocalDate fin) {
        if (employeIds == null || employeIds.isEmpty()) {
            return List.of();
        }
        return absenceRepository
                .findByEmployeIdEmployerInAndDateDebutLessThanEqualAndDateFinGreaterThanEqualAndStatutNot(
                        employeIds, fin, debut, StatutAbsence.ANNULEE);
    }

    public void verifierChevauchement(Integer employeId, LocalDate debut, LocalDate fin,
                                      Integer absenceId, Map<String, String> erreurs) {
        List<DemandeConge> demandes = demandeCongeRepository.findChevauchements(
                employeId, debut, fin, STATUTS_DEMANDE_ACTIFS, null);
        if (!demandes.isEmpty()) {
            DemandeConge conflit = demandes.get(0);
            erreurs.put("chevauchement", "Chevauchement avec la demande "
                    + conflit.getReference() + " du " + conflit.getDateDebut()
                    + " au " + conflit.getDateFin() + ".");
            return;
        }

        List<Absence> absences = absenceRepository.findChevauchements(
                employeId, debut, fin, absenceId);
        for (Absence existante : absences) {
            if (existante.getStatut() == StatutAbsence.ANNULEE) {
                continue;
            }
            erreurs.put("chevauchement", "Chevauchement avec l'absence "
                    + existante.getTypeConge().getLibelle() + " du "
                    + existante.getDateDebut() + " au " + existante.getDateFin() + ".");
            return;
        }
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
            auditService.enregistrerAction(utilisateur.getEmail(), action, "absence", id, details);
        } catch (RuntimeException ignored) {
            // L'audit ne doit pas bloquer l'opération métier.
        }
    }
}
