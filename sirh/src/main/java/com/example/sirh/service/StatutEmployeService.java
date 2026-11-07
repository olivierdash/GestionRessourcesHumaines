package com.example.sirh.service;

import com.example.sirh.model.StatutEmploye;
import com.example.sirh.repository.StatutEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatutEmployeService {

    public static final String ACTIF         = "ACTIF";
    public static final String INACTIF       = "INACTIF";
    public static final String EN_ONBOARDING = "EN_ONBOARDING";

    private final StatutEmployeRepository statutEmployeRepository;

    @Transactional(readOnly = true)
    public StatutEmploye getByLibelle(String libelle) {
        return statutEmployeRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut employé '" + libelle + "' introuvable"));
    }

    @Transactional(readOnly = true)
    public StatutEmploye getById(Integer id) {
        return statutEmployeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut employé id=" + id + " introuvable"));
    }

    @Transactional(readOnly = true)
    public List<StatutEmploye> getAll() {
        return statutEmployeRepository.findAll();
    }
}