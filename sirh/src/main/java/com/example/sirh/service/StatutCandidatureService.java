package com.example.sirh.service;

import com.example.sirh.model.StatutCandidature;
import com.example.sirh.repository.StatutCandidatureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatutCandidatureService {

    private final StatutCandidatureRepository statutCandidatureRepository;

    public StatutCandidature getByLibelle(String libelle) {
        return statutCandidatureRepository.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Statut " + libelle + " introuvable"));
    }

    public StatutCandidature getById(Integer id) {
        return statutCandidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statut introuvable avec l'id " + id));
    }
}