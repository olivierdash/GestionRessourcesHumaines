package com.example.sirh.service;

import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.dto.PointageFilter;
import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.Employe;
import com.example.sirh.model.HoraireTravail;
import com.example.sirh.model.Pointage;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.repository.PointageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointageService {

    private final PointageRepository pointageRepository;
    private final EmployeService employeService;
    private final AffectationPlanningService affectationPlanningService;
    private final RegimeTravailService regimeTravailService;

    @Transactional(readOnly = true)
    public Page<Pointage> lister(PointageFilter filter, Pageable pageable) {
        if (filter.getDateDebut() != null
                && filter.getDateFin() != null
                && filter.getDateFin().isBefore(filter.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure ou égale à la date de début.");
        }
        return pointageRepository.findWithFilters(
                filter.getDateDebut(), filter.getDateFin(), filter.getEmployeId(), pageable);
    }

    @Transactional(readOnly = true)
    public Pointage getById(Integer id) {
        return pointageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pointage introuvable : " + id));
    }

    @Transactional
    public Pointage enregistrer(Pointage form) {
        if (form.getEmploye() == null || form.getEmploye().getIdEmployer() == null) {
            throw new IllegalArgumentException("Le salarié est obligatoire.");
        }
        if (form.getDatePointage() == null) {
            throw new IllegalArgumentException("La date de pointage est obligatoire.");
        }
        if (form.getDatePointage().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Impossible de pointer sur une date future.");
        }
        if (form.getHeureEntree() != null && form.getHeureSortie() != null) {
            if (!form.getHeureSortie().isAfter(form.getHeureEntree())) {
                throw new IllegalArgumentException(
                        "L'heure de sortie doit être postérieure à l'heure d'entrée.");
            }
            long heures = Duration.between(form.getHeureEntree(), form.getHeureSortie()).toHours();
            if (heures >= 24) {
                throw new IllegalArgumentException(
                        "La plage horaire ne peut pas dépasser 24h.");
            }
        }

        Employe employe = employeService.getById(form.getEmploye().getIdEmployer());
        form.setEmploye(employe);

        Pointage pointage;
        if (form.getId() == null) {
            pointage = pointageRepository
                    .findByEmployeIdEmployerAndDatePointage(
                            employe.getIdEmployer(), form.getDatePointage())
                    .orElseGet(Pointage::new);
        } else {
            pointage = getById(form.getId());
        }

        pointage.setEmploye(employe);
        pointage.setDatePointage(form.getDatePointage());
        pointage.setHeureEntree(form.getHeureEntree());
        pointage.setHeureSortie(form.getHeureSortie());
        pointage.setCommentaire(form.getCommentaire());

        return pointageRepository.save(pointage);
    }

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

        List<AffectationPlanning> affectations =
                affectationPlanningService.findActivesForDate(
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
        int minutesTravaillees = Math.max(0,
                minutesPresence - Math.min(minutesPause, minutesPresence));

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