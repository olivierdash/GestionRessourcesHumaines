package com.example.sirh.service;

import com.example.sirh.model.Affectation;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Poste;
import com.example.sirh.repository.AffectationRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;

    @Transactional
    public Affectation save(Affectation affectation) {
        return affectationRepository.save(affectation);
    }

    @Transactional(readOnly = true)
    public List<Affectation> findByEmploye(Integer idEmploye) {
        return affectationRepository.findByEmployeIdEmployer(idEmploye);
    }

    @Transactional
    public Affectation changerPoste(Employe employe, Poste nouveauPoste, LocalDate dateDebut) {
        // 1. Clôturer l'affectation en cours (celle qui n'a pas de date de fin)
        for (Affectation a : affectationRepository
                .findByEmployeIdEmployerOrderByDateDebutDesc(employe.getIdEmployer())) {
            if (a.getDateFin() == null) {
                LocalDate fin = dateDebut.minusDays(1);
                if (fin.isBefore(a.getDateDebut())) {
                    fin = a.getDateDebut();
                }
                a.setDateFin(fin);
                affectationRepository.save(a);
            }
        }
        // 2. Ouvrir la nouvelle affectation
        return affectationRepository.save(Affectation.builder()
                .employe(employe)
                .poste(nouveauPoste)
                .dateDebut(dateDebut)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Affectation> historique(Integer idEmploye) {
        return affectationRepository.findByEmployeIdEmployerOrderByDateDebutDesc(idEmploye);
    }
}