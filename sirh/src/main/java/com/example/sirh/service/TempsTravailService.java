package com.example.sirh.service;

import com.example.sirh.model.*;
import com.example.sirh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempsTravailService {
    private final RegimeTravailRepository regimeRepository;
    private final PlanningRepository planningRepository;
    private final AffectationPlanningRepository affectationRepository;
    private final EmployeRepository employeRepository;

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
}
