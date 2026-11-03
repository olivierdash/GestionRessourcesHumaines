package com.example.sirh.service;

import com.example.sirh.model.Affectation;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.Employe;
import com.example.sirh.repository.AffectationRepository;
import com.example.sirh.repository.EmployeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final CandidatureService candidatureService;

    // ---------- Existant ----------

    @Transactional(readOnly = true)
    public Page<Employe> getAllEmployes(Pageable pageable) {
        return employeRepository.findAll(pageable);
    }

    @Transactional
    public Employe createEmploye(Integer idCandidature, String matricule,
            LocalDate hiringDate, String statusEmployer) {
        Candidature candidature = candidatureService.getCandidatureById(idCandidature);
        Employe employe = Employe.builder()
                .candidature(candidature)
                .matricule(matricule)
                .hiringDate(hiringDate)
                .statusEmployer(statusEmployer)
                .build();
        return employeRepository.save(employe);
    }

    // ---------- Ajouts pour la séparation des couches ----------

    @Transactional(readOnly = true)
    public Employe getById(Integer id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable : " + id));
    }

    @Transactional
    public Employe save(Employe employe) {
        return employeRepository.save(employe);
    }

    @Transactional(readOnly = true)
    public boolean existsByCandidature(Integer idCandidature) {
        return employeRepository.existsByCandidatureIdCandidature(idCandidature);
    }

    @Transactional(readOnly = true)
    public Optional<Employe> findByCandidature(Integer idCandidature) {
        return employeRepository.findByCandidatureIdCandidature(idCandidature);
    }

    @Transactional(readOnly = true)
    public long countByDepartement(Integer idDepartement) {
        return employeRepository
                .countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(idDepartement);
    }

    /**
     * Génère le prochain matricule (EMP-AAAA-NNNN) à partir de la séquence PostgreSQL.
     * Écriture (nextval) -> @Transactional sans readOnly.
     */
    @Transactional
    public String prochainMatricule() {
        Long numero = employeRepository.prochainNumeroMatricule();
        return String.format("EMP-%d-%04d", LocalDate.now().getYear(), numero);
    }
}