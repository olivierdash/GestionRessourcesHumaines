package com.example.sirh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ContextePaie(
        Integer idEmployer,
        String matricule,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        ContratInfo contrat,
        BigDecimal salaireBaseMensuel,
        BigDecimal tauxHoraire,
        BigDecimal tauxJournalier,
        List<PointageInfo> pointages,
        List<AbsenceInfo> absences,
        List<CongeInfo> conges,
        List<HeureSupInfo> heuresSupplementaires,
        RegimeInfo regime,
        Integer joursFeriesDansPeriode) {
    public record ContratInfo(Integer idContract, LocalDate dateDebut, LocalDate dateFin,
            Integer idTypeContrat, Integer idPoste, Integer idStatutContrat) {
    }

    public record PointageInfo(LocalDate date, LocalTime entree, LocalTime sortie,
            Integer minutesTravaillees, Integer minutesRetard, Integer minutesSup) {
    }

    public record AbsenceInfo(LocalDate dateDebut, LocalDate dateFin, String motif, boolean justifiee) {
    }

    public record CongeInfo(LocalDate dateDebut, LocalDate dateFin, Integer idTypeConge, boolean paye) {
    }

    public record HeureSupInfo(LocalDate date, Integer minutes, BigDecimal tauxMajoration) {
    }

    public record RegimeInfo(Integer idRegime, BigDecimal heuresHebdo, Integer dureeMaxHebdo,
            Integer dureeMaxJournaliere, BigDecimal seuilHeuresSup) {
    }
}