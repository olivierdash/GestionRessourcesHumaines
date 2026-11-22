package com.example.sirh.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.repository.AffectationPlanningRepository;
import com.example.sirh.repository.DemandeCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanningCongeService {

    private final AffectationPlanningRepository affectationPlanningRepository;
    private final DemandeCongeRepository demandeCongeRepository;

    @Transactional(readOnly = true)
    public List<AffectationPlanning> conflitsPourDemande(DemandeConge demande) {
        if (demande == null || demande.getEmploye() == null) {
            return List.of();
        }
        return affectationPlanningRepository.findChevauchementsForEmploye(
                demande.getEmploye().getIdEmployer(),
                demande.getDateDebut(), demande.getDateFin());
    }

    @Transactional(readOnly = true)
    public List<AffectationPlanning> affectationsPeriode(Integer employeId,
                                                         LocalDate debut, LocalDate fin) {
        return affectationPlanningRepository.findChevauchementsForEmploye(employeId, debut, fin);
    }

    @Transactional(readOnly = true)
    public List<DemandeConge> demandesEnAttentePeriode(LocalDate debut, LocalDate fin) {
        return demandeCongeRepository.findActivesDansPeriode(debut, fin,
                List.of(StatutDemandeConge.SOUMISE, StatutDemandeConge.VALIDEE_MANAGER));
    }
}
