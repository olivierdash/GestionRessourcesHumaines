package com.example.sirh.service;

import com.example.sirh.model.Planning;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.repository.PlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository planningRepository;
    private final RegimeTravailService regimeTravailService;

    @Transactional(readOnly = true)
    public List<Planning> getAll() {
        return planningRepository.findAllWithRelations();
    }

    @Transactional(readOnly = true)
    public Planning getById(Integer id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning introuvable : " + id));
    }

    @Transactional
    public Planning enregistrer(Planning form) {
        if (form.getLibelle() == null || form.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        }
        if (form.getRegimeTravail() == null || form.getRegimeTravail().getId() == null) {
            throw new IllegalArgumentException("Le régime de travail est obligatoire.");
        }
        if (form.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (form.getDateFin() != null && form.getDateFin().isBefore(form.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas précéder la date de début.");
        }

        RegimeTravail regime = regimeTravailService.getById(form.getRegimeTravail().getId());
        if (Boolean.FALSE.equals(regime.getActif())) {
            throw new IllegalArgumentException("Impossible d'utiliser un régime inactif.");
        }

        Planning planning = form.getId() == null
                ? new Planning()
                : getById(form.getId());

        planning.setLibelle(form.getLibelle().trim());
        planning.setRegimeTravail(regime);
        planning.setDateDebut(form.getDateDebut());
        planning.setDateFin(form.getDateFin());
        planning.setActif(form.getActif() != null ? form.getActif() : Boolean.TRUE);

        return planningRepository.save(planning);
    }
}