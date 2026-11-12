package com.example.sirh.service;

import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.dto.PointageFilter;
import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.HoraireTravail;
import com.example.sirh.model.Planning;
import com.example.sirh.model.Pointage;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.model.TypeDecompte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempsTravailService {

    private final RegimeTravailService regimeTravailService;
    private final PlanningService planningService;
    private final AffectationPlanningService affectationPlanningService;
    private final PointageService pointageService;
    private final JourFerieService jourFerieService;
    private final TypeDecompteService typeDecompteService;

    // =========================================================
    // RÉGIME DE TRAVAIL
    // =========================================================

    public List<RegimeTravail> regimes() {
        return regimeTravailService.getAll();
    }

    public RegimeTravail regime(Integer id) {
        return regimeTravailService.getById(id);
    }

    public RegimeTravail enregistrerRegime(RegimeTravail form) {
        return regimeTravailService.enregistrer(form);
    }

    public HoraireTravail enregistrerHoraire(HoraireTravail form) {
        return regimeTravailService.enregistrerHoraire(form);
    }

    // =========================================================
    // PLANNING
    // =========================================================

    public List<Planning> plannings() {
        return planningService.getAll();
    }

    public Planning planning(Integer id) {
        return planningService.getById(id);
    }

    public Planning enregistrerPlanning(Planning form) {
        return planningService.enregistrer(form);
    }

    // =========================================================
    // AFFECTATION PLANNING
    // =========================================================

    public List<AffectationPlanning> affectations(Integer planningId) {
        return affectationPlanningService.listerParPlanning(planningId);
    }

    public AffectationPlanning affecterPlanning(AffectationPlanning form) {
        return affectationPlanningService.enregistrer(form);
    }

    // =========================================================
    // CALCUL (orchestration multi-services)
    // =========================================================

    @Transactional(readOnly = true)
    public PointageCalculDto calculer(Pointage pointage) {
        PointageCalculDto result = new PointageCalculDto();

        if (pointage == null
                || pointage.getHeureEntree() == null
                || pointage.getHeureSortie() == null) {
            return result;
        }

        int minutesPresence = (int) Duration.between(
                pointage.getHeureEntree(), pointage.getHeureSortie()).toMinutes();

        List<AffectationPlanning> affectations = affectationPlanningService.findActivesForDate(
                pointage.getEmploye().getIdEmployer(), pointage.getDatePointage());

        if (affectations.isEmpty()) {
            result.setMinutesPresence(minutesPresence);
            result.setMinutesTravaillees(minutesPresence);
            result.setAnomalie("Aucune affectation de planning sur cette date.");
            return result;
        }

        RegimeTravail regime = affectations.get(0).getPlanning().getRegimeTravail();
        HoraireTravail horaire = regimeTravailService.trouverHorairePourJour(
                regime, pointage.getDatePointage().getDayOfWeek());

        if (horaire == null) {
            result.setMinutesPresence(minutesPresence);
            result.setMinutesTravaillees(minutesPresence);
            result.setAnomalie("Aucun horaire prévu pour ce jour dans le régime.");
            return result;
        }

        int minutesPause = Math.max(0,
                horaire.getPauseMinutes() != null ? horaire.getPauseMinutes() : 0);
        int minutesTravaillees = Math.max(0, minutesPresence - Math.min(minutesPause, minutesPresence));

        int minutesRetard = 0;
        if (pointage.getHeureEntree().isAfter(horaire.getHeureDebut())) {
            minutesRetard = (int) Duration.between(
                    horaire.getHeureDebut(), pointage.getHeureEntree()).toMinutes();
        }

        int minutesPrevues = (int) Duration.between(
                horaire.getHeureDebut(), horaire.getHeureFin()).toMinutes() - minutesPause;
        int minutesSup = Math.max(0, minutesTravaillees - Math.max(0, minutesPrevues));

        String anomalie = null;
        if (minutesRetard > 15) {
            anomalie = "Retard de " + minutesRetard + " min";
        }

        result.setMinutesPresence(minutesPresence);
        result.setMinutesPause(minutesPause);
        result.setMinutesTravaillees(minutesTravaillees);
        result.setMinutesRetard(minutesRetard);
        result.setMinutesSupplementaires(minutesSup);
        result.setAnomalie(anomalie);

        return result;
    }
}