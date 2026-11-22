package com.example.sirh.service;

import com.example.sirh.dto.ContextePaie;
import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Pointage;
import com.example.sirh.repository.ContratRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContextePaieService {

    private static final BigDecimal HEURES_MOIS_NON_AGRICOLE = new BigDecimal("173.33");
    private static final BigDecimal JOURS_MOIS = new BigDecimal("30");

    private final ContratRepository contratRepository;
    private final EmployeService employeService;
    private final PointageService pointageService;
    // private final AbsenceRepository absenceRepo; // à brancher plus tard
    // private final DemandeCongeRepository congeRepo; // à brancher plus tard
    // private final JourFerieRepository jourFerieRepo; // à brancher plus tard
    // private final AffectationPlanningService affectationPlanningService;

    public ContextePaieService(ContratRepository contratRepository,
            EmployeService employeService,
            PointageService pointageService) {
        this.contratRepository = contratRepository;
        this.employeService = employeService;
        this.pointageService = pointageService;
    }

    @Transactional(readOnly = true)
    public ContextePaie charger(Integer idEmploye, LocalDate debut, LocalDate fin) {
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("Date de début > date de fin");
        }

        Employe employe = employeService.getById(idEmploye);

        List<Contrat> contrats = contratRepository
                .findContratsCouvrantPeriode(idEmploye, debut, fin);
        if (contrats.isEmpty()) {
            throw new IllegalStateException(
                    "Aucun contrat ne couvre la période " + debut + " → " + fin
                            + " pour l'employé " + idEmploye);
        }
        Contrat contrat = contrats.get(0);

        BigDecimal salaireBase = contrat.getSalaireMensuel();
        if (salaireBase == null || salaireBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Salaire de base invalide pour le contrat " + contrat.getIdContract());
        }

        BigDecimal tauxHoraire = salaireBase.divide(HEURES_MOIS_NON_AGRICOLE, 2, RoundingMode.HALF_UP);
        BigDecimal tauxJournalier = salaireBase.divide(JOURS_MOIS, 2, RoundingMode.HALF_UP);

        // --- pointages calculés ---
        List<Pointage> brut = pointageService.listerEntre(idEmploye, debut, fin);

        List<ContextePaie.PointageInfo> pointages = brut.stream()
                .map(p -> {
                    PointageCalculDto calc = pointageService.calculer(p);
                    return new ContextePaie.PointageInfo(
                            p.getDatePointage(),
                            p.getHeureEntree(),
                            p.getHeureSortie(),
                            calc.getMinutesTravaillees(),
                            calc.getMinutesRetard(),
                            calc.getMinutesSupplementaires());
                })
                .toList();

        List<ContextePaie.HeureSupInfo> heuresSup = pointages.stream()
                .filter(p -> p.minutesSup() != null && p.minutesSup() > 0)
                .map(p -> new ContextePaie.HeureSupInfo(p.date(), p.minutesSup(), null))
                .toList();

        List<ContextePaie.AbsenceInfo> absences = List.of(); // stub
        List<ContextePaie.CongeInfo> conges = List.of(); // stub
        ContextePaie.RegimeInfo regime = null; // stub
        int nbJoursFeries = 0; // stub

        ContextePaie.ContratInfo contratInfo = new ContextePaie.ContratInfo(
                contrat.getIdContract(),
                contrat.getDateDebut(),
                contrat.getDateFin(),
                contrat.getTypeContrat().getId(),
                contrat.getPoste().getIdPoste(),
                contrat.getStatut().getId());
        return new ContextePaie(
                employe.getIdEmployer(),
                employe.getMatricule(),
                debut, fin,
                contratInfo,
                salaireBase, tauxHoraire, tauxJournalier,
                pointages, absences, conges, heuresSup,
                regime, nbJoursFeries);
    }
}