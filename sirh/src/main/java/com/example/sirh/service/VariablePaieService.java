package com.example.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DureeCongeDto;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.VariablePaie;
import com.example.sirh.model.enums.CodeVariablePaie;
import com.example.sirh.model.enums.SourceAbsence;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutVariablePaie;
import com.example.sirh.repository.VariablePaieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariablePaieService {

    private final VariablePaieRepository variablePaieRepository;
    private final CalculDureeCongeService calculDureeCongeService;

    // ------------------------------------------------------------------
    // Génération
    // ------------------------------------------------------------------

    @Transactional
    public List<VariablePaie> genererPourDemande(DemandeConge demande) {
        if (demande == null || demande.getTypeConge() == null) {
            return List.of();
        }
        CodeVariablePaie code = Boolean.TRUE.equals(demande.getTypeConge().getPaye())
                ? CodeVariablePaie.CONGE_PAYE
                : CodeVariablePaie.CONGE_NON_PAYE;

        DureeCongeDto duree = calculDureeCongeService.detail(
                demande.getTypeConge(),
                demande.getDateDebut(),
                demande.getDateFin(),
                demande.getDemiJourDebut(),
                demande.getDemiJourFin());

        List<VariablePaie> generees = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entree : duree.getParMois().entrySet()) {
            YearMonth mois = YearMonth.parse(entree.getKey());
            LocalDate periode = mois.atDay(1);
            if (variablePaieRepository.existsByDemandeCongeIdAndPeriodeAndCode(
                    demande.getId(), periode, code)) {
                continue;
            }

            LocalDate debut = demande.getDateDebut().isAfter(mois.atDay(1))
                    ? demande.getDateDebut() : mois.atDay(1);
            LocalDate fin = demande.getDateFin().isBefore(mois.atEndOfMonth())
                    ? demande.getDateFin() : mois.atEndOfMonth();

            VariablePaie variable = VariablePaie.builder()
                    .employe(demande.getEmploye())
                    .demandeConge(demande)
                    .code(code)
                    .libelle(demande.getTypeConge().getLibelle())
                    .periode(periode)
                    .dateDebut(debut)
                    .dateFin(fin)
                    .quantite(entree.getValue().setScale(2, RoundingMode.HALF_UP))
                    .unite("JOUR")
                    .statut(StatutVariablePaie.A_CONSOMMER)
                    .commentaire("Demande " + demande.getReference())
                    .build();
            generees.add(variablePaieRepository.save(variable));
        }
        return generees;
    }

    @Transactional
    public List<VariablePaie> genererPourAbsence(Absence absence) {
        if (absence == null || absence.getTypeConge() == null
                || absence.getSource() == SourceAbsence.DEMANDE_CONGE
                || absence.getStatut() == StatutAbsence.ANNULEE
                || absence.getStatut() == StatutAbsence.DECLAREE) {
            return List.of();
        }
        TypeConge type = absence.getTypeConge();
        boolean payee = absence.getStatut() == StatutAbsence.JUSTIFIEE
                && Boolean.TRUE.equals(type.getPaye());
        CodeVariablePaie code = payee
                ? CodeVariablePaie.ABSENCE_PAYEE
                : CodeVariablePaie.ABSENCE_NON_PAYEE;

        DureeCongeDto duree = calculDureeCongeService.detail(
                type, absence.getDateDebut(), absence.getDateFin(),
                absence.getDemiJourDebut(), absence.getDemiJourFin());

        List<VariablePaie> generees = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entree : duree.getParMois().entrySet()) {
            YearMonth mois = YearMonth.parse(entree.getKey());
            LocalDate periode = mois.atDay(1);
            if (variablePaieRepository.existsByAbsenceIdAndPeriodeAndCode(
                    absence.getId(), periode, code)) {
                continue;
            }
            LocalDate debut = absence.getDateDebut().isAfter(mois.atDay(1))
                    ? absence.getDateDebut() : mois.atDay(1);
            LocalDate fin = absence.getDateFin().isBefore(mois.atEndOfMonth())
                    ? absence.getDateFin() : mois.atEndOfMonth();

            VariablePaie variable = VariablePaie.builder()
                    .employe(absence.getEmploye())
                    .absence(absence)
                    .code(code)
                    .libelle(type.getLibelle())
                    .periode(periode)
                    .dateDebut(debut)
                    .dateFin(fin)
                    .quantite(entree.getValue().setScale(2, RoundingMode.HALF_UP))
                    .unite("JOUR")
                    .statut(StatutVariablePaie.A_CONSOMMER)
                    .commentaire("Absence du " + absence.getDateDebut()
                            + " au " + absence.getDateFin())
                    .build();
            generees.add(variablePaieRepository.save(variable));
        }
        return generees;
    }

    // ------------------------------------------------------------------
    // Annulation
    // ------------------------------------------------------------------

    @Transactional
    public void annulerPourDemande(DemandeConge demande) {
        List<VariablePaie> variables = variablePaieRepository
                .findByDemandeCongeId(demande.getId());
        annuler(variables, "Annulation de la demande " + demande.getReference());
    }

    @Transactional
    public void annulerPourAbsence(Absence absence) {
        List<VariablePaie> variables = variablePaieRepository.findByAbsenceId(absence.getId());
        annuler(variables, "Annulation de l'absence du " + absence.getDateDebut());
    }

    private void annuler(List<VariablePaie> variables, String motif) {
        for (VariablePaie variable : variables) {
            if (variable.getStatut() == StatutVariablePaie.CONSOMME) {
                Map<String, String> erreurs = new LinkedHashMap<>();
                erreurs.put("paie",
                        "La variable de paie " + variable.getCode() + " de "
                                + variable.getPeriode() + " est déjà consommée : annulation impossible.");
                throw new ValidationMetierException(erreurs);
            }
            if (variable.getStatut() != StatutVariablePaie.ANNULE) {
                variable.setStatut(StatutVariablePaie.ANNULE);
                variable.setCommentaire(variable.getCommentaire() == null
                        ? motif : variable.getCommentaire() + " — " + motif);
                variablePaieRepository.save(variable);
            }
        }
    }

    @Transactional
    public VariablePaie marquerConsomme(Integer id) {
        VariablePaie variable = variablePaieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variable de paie introuvable : " + id));
        if (variable.getStatut() != StatutVariablePaie.A_CONSOMMER) {
            Map<String, String> erreurs = new LinkedHashMap<>();
            erreurs.put("paie", "Seule une variable « à consommer » peut être consommée.");
            throw new ValidationMetierException(erreurs);
        }
        variable.setStatut(StatutVariablePaie.CONSOMME);
        variable.setDateConsommation(java.time.LocalDateTime.now());
        return variablePaieRepository.save(variable);
    }

    // ------------------------------------------------------------------
    // Lecture
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<VariablePaie> rechercher(String periode, Integer employeId,
                                         StatutVariablePaie statut) {
        LocalDate debut;
        LocalDate fin;
        if (periode == null || periode.isBlank()) {
            debut = LocalDate.now().withDayOfYear(1);
            fin = LocalDate.now().withMonth(12).withDayOfMonth(31);
        } else {
            YearMonth mois = YearMonth.parse(periode);
            debut = mois.atDay(1);
            fin = mois.atEndOfMonth();
        }
        return variablePaieRepository.rechercher(debut, fin, employeId, statut);
    }

    @Transactional(readOnly = true)
    public List<VariablePaie> parDemande(Integer demandeId) {
        return variablePaieRepository.findByDemandeCongeId(demandeId);
    }

    @Transactional(readOnly = true)
    public List<VariablePaie> parAbsence(Integer absenceId) {
        return variablePaieRepository.findByAbsenceId(absenceId);
    }
}
