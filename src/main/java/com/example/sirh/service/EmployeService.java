package com.example.sirh.service;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.Employe;
import com.example.sirh.repository.EmployeRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final CandidatureService candidatureService;

    public Page<Employe> getAllEmployes(Pageable pageable) {
        return employeRepository.findAll(pageable);
    }

    @Transactional
    public Employe createEmploye(Integer idCandidature, String matricule, LocalDate hiringDate, String statusEmployer) {
        // 1. Récupération de la candidature via son service
        Candidature candidature = candidatureService.getCandidatureById(idCandidature);

        // 2. Création et sauvegarde de l'employé
        Employe employe = Employe.builder()
                .candidature(candidature)
                .matricule(matricule)
                .hiringDate(hiringDate)
                .statusEmployer(statusEmployer)
                .build();

        return employeRepository.save(employe);
    }
}