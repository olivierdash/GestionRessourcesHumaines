package com.example.sirh.service;

import com.example.sirh.model.*;
import com.example.sirh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempsTravailService {
    private final RegimeTravailRepository regimeRepository;
    private final PlanningRepository planningRepository;
    private final AffectationPlanningRepository affectationRepository;
    private final EmployeRepository employeRepository;
    private final PointageRepository pointageRepository;

    @Transactional(readOnly = true)
    public List<RegimeTravail> regimes() {
        return regimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RegimeTravail regime(Integer id) {
        return regimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Régime de travail introuvable : " + id));
    }

    @Transactional
    public RegimeTravail enregistrerRegime(Integer id, String libelle, String description,
                                           Integer heuresHebdomadaires, Integer joursHebdomadaires) {
        if (libelle == null || libelle.isBlank() || heuresHebdomadaires == null
                || heuresHebdomadaires <= 0 || joursHebdomadaires == null
                || joursHebdomadaires < 1 || joursHebdomadaires > 7) {
            throw new IllegalArgumentException("Le libellé, les heures et le nombre de jours sont obligatoires et valides.");
        }
        RegimeTravail regime = id == null ? new RegimeTravail() : regime(id);
        regime.setLibelle(libelle.trim());
        regime.setDescription(description);
        regime.setHeuresHebdomadaires(heuresHebdomadaires);
        regime.setJoursHebdomadaires(joursHebdomadaires);
        return regimeRepository.save(regime);
    }

    @Transactional
    public void enregistrerHoraire(Integer regimeId, DayOfWeek jour, java.time.LocalTime debut,
                                   java.time.LocalTime fin, Integer pauseMinutes) {
        RegimeTravail regime = regime(regimeId);
        if (debut == null || fin == null || !fin.isAfter(debut) || pauseMinutes == null || pauseMinutes < 0) {
            throw new IllegalArgumentException("Les heures et la pause doivent former un horaire valide.");
        }
        HoraireTravail horaire = regime.getHoraires().stream()
                .filter(item -> item.getJourSemaine() == jour).findFirst().orElseGet(() -> {
                    HoraireTravail nouveau = HoraireTravail.builder()
                            .regimeTravail(regime).jourSemaine(jour).build();
                    regime.getHoraires().add(nouveau);
                    return nouveau;
                });
        horaire.setHeureDebut(debut);
        horaire.setHeureFin(fin);
        horaire.setPauseMinutes(pauseMinutes);
        regimeRepository.save(regime);
    }

    @Transactional(readOnly = true)
    public List<Planning> plannings() {
        return planningRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Planning planning(Integer id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning introuvable : " + id));
    }

    @Transactional
    public Planning enregistrerPlanning(Integer id, String libelle, Integer regimeId,
                                         LocalDate dateDebut, LocalDate dateFin) {
        if (libelle == null || libelle.isBlank() || regimeId == null || dateDebut == null
                || (dateFin != null && dateFin.isBefore(dateDebut))) {
            throw new IllegalArgumentException("Les informations du planning sont invalides.");
        }
        Planning planning = id == null ? new Planning() : planning(id);
        planning.setLibelle(libelle.trim());
        planning.setRegimeTravail(regime(regimeId));
        planning.setDateDebut(dateDebut);
        planning.setDateFin(dateFin);
        return planningRepository.save(planning);
    }

    @Transactional
    public void affecterPlanning(Integer planningId, Integer employeId, LocalDate dateDebut, LocalDate dateFin) {
        if (employeId == null || dateDebut == null || (dateFin != null && dateFin.isBefore(dateDebut))) {
            throw new IllegalArgumentException("Les dates d'affectation sont invalides.");
        }
        AffectationPlanning affectation = AffectationPlanning.builder()
                .planning(planning(planningId))
                .employe(employeRepository.findById(employeId)
                        .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable : " + employeId)))
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .build();
        affectationRepository.save(affectation);
    }

    @Transactional(readOnly = true)
    public List<AffectationPlanning> affectations(Integer planningId) {
        return affectationRepository.findByPlanningIdOrderByDateDebutDesc(planningId);
    }

    @Transactional(readOnly = true)
    public List<Pointage> pointages(LocalDate dateDebut, LocalDate dateFin, Integer employeId) {
        if (employeId == null) {
            return pointageRepository.findByDatePointageBetweenOrderByDatePointageDesc(dateDebut, dateFin);
        }
        return pointageRepository.findByEmployeIdEmployerAndDatePointageBetweenOrderByDatePointageDesc(
                employeId, dateDebut, dateFin);
    }

    @Transactional(readOnly = true)
    public Pointage pointage(Integer id) {
        return pointageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pointage introuvable : " + id));
    }

    @Transactional
    public Pointage enregistrerPointage(Integer id, Integer employeId, LocalDate datePointage,
                                        LocalTime heureEntree, LocalTime heureSortie,
                                        String commentaire) {
        if (employeId == null || datePointage == null) {
            throw new IllegalArgumentException("Le salarié et la date sont obligatoires.");
        }
        if (heureEntree != null && heureSortie != null && !heureSortie.isAfter(heureEntree)) {
            throw new IllegalArgumentException("L'heure de sortie doit être postérieure à l'heure d'entrée.");
        }

        Pointage pointage;
        if (id == null) {
            pointage = pointageRepository.findByEmployeIdEmployerAndDatePointage(employeId, datePointage)
                    .orElseGet(Pointage::new);
        } else {
            pointage = pointage(id);
        }

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable : " + employeId));
        pointage.setEmploye(employe);
        pointage.setDatePointage(datePointage);
        pointage.setHeureEntree(heureEntree);
        pointage.setHeureSortie(heureSortie);
        pointage.setCommentaire(commentaire);

        int minutesTravaillees = minutesEntre(heureEntree, heureSortie);
        int minutesRetard = 0;
        int minutesSupplementaires = 0;
        List<AffectationPlanning> affectations = affectationRepository.findActivesForDate(employeId, datePointage);
        if (!affectations.isEmpty()) {
            RegimeTravail regime = affectations.get(0).getPlanning().getRegimeTravail();
            HoraireTravail horaire = regime.getHoraires().stream()
                    .filter(item -> item.getJourSemaine() == datePointage.getDayOfWeek())
                    .findFirst().orElse(null);
            if (horaire != null) {
                if (heureEntree != null && horaire.getHeureDebut() != null
                        && heureEntree.isAfter(horaire.getHeureDebut())) {
                    minutesRetard = (int) Duration.between(horaire.getHeureDebut(), heureEntree).toMinutes();
                }
                int minutesPrevues = minutesEntre(horaire.getHeureDebut(), horaire.getHeureFin())
                        - horaire.getPauseMinutes();
                minutesSupplementaires = Math.max(0, minutesTravaillees - Math.max(0, minutesPrevues));
            }
        }
        pointage.setMinutesTravaillees(minutesTravaillees);
        pointage.setMinutesRetard(minutesRetard);
        pointage.setMinutesSupplementaires(minutesSupplementaires);
        return pointageRepository.save(pointage);
    }

    private int minutesEntre(LocalTime debut, LocalTime fin) {
        if (debut == null || fin == null) {
            return 0;
        }
        return (int) Duration.between(debut, fin).toMinutes();
    }
}
