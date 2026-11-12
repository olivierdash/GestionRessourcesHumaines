package com.example.sirh.service;

import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Planning;
import com.example.sirh.repository.AffectationPlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AffectationPlanningService {

    private final AffectationPlanningRepository affectationRepository;
    private final PlanningService planningService;
    private final EmployeService employeService;

    @Transactional(readOnly = true)
    public List<AffectationPlanning> listerParPlanning(Integer planningId) {
        return affectationRepository.findByPlanningIdOrderByDateDebutDesc(planningId);
    }

    @Transactional(readOnly = true)
    public List<AffectationPlanning> findActivesForDate(Integer employeId, LocalDate date) {
        return affectationRepository.findActivesForDate(employeId, date);
    }

    @Transactional
    public AffectationPlanning enregistrer(AffectationPlanning form) {
        if (form.getPlanning() == null || form.getPlanning().getId() == null) {
            throw new IllegalArgumentException("Le planning est obligatoire.");
        }
        if (form.getEmploye() == null || form.getEmploye().getIdEmployer() == null) {
            throw new IllegalArgumentException("Le salarié est obligatoire.");
        }
        if (form.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (form.getDateFin() != null && form.getDateFin().isBefore(form.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas précéder la date de début.");
        }

        Planning planning = planningService.getById(form.getPlanning().getId());
        Employe employe = employeService.getById(form.getEmploye().getIdEmployer());

        if (form.getDateDebut().isBefore(planning.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de début de l'affectation ne peut pas précéder celle du planning ("
                            + planning.getDateDebut() + ").");
        }
        if (planning.getDateFin() != null
                && form.getDateFin() != null
                && form.getDateFin().isAfter(planning.getDateFin())) {
            throw new IllegalArgumentException(
                    "La date de fin de l'affectation ne peut pas dépasser celle du planning ("
                            + planning.getDateFin() + ").");
        }

        form.setPlanning(planning);
        form.setEmploye(employe);

        verifierPasDeChevauchement(form);

        return affectationRepository.save(form);
    }

    private void verifierPasDeChevauchement(AffectationPlanning form) {
        Integer employeId = form.getEmploye().getIdEmployer();
        LocalDate debut = form.getDateDebut();
        LocalDate fin = form.getDateFin();

        List<AffectationPlanning> existantes =
                affectationRepository.findByEmployeIdEmployerOrderByDateDebutDesc(employeId);

        for (AffectationPlanning existante : existantes) {
            if (form.getId() != null && existante.getId().equals(form.getId())) {
                continue;
            }
            boolean chevauche =
                    (existante.getDateFin() == null || !existante.getDateFin().isBefore(debut))
                    && (fin == null || !existante.getDateDebut().isAfter(fin));
            if (chevauche) {
                throw new IllegalArgumentException(
                        "Le salarié a déjà une affectation de planning sur cette période ("
                                + existante.getDateDebut() + " → "
                                + (existante.getDateFin() != null ? existante.getDateFin() : "en cours")
                                + ").");
            }
        }
    }
}