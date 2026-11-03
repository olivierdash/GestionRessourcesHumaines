package com.example.sirh.service;

import com.example.sirh.model.StatutContrat;
import com.example.sirh.repository.StatutContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatutContratService {

    private final StatutContratRepository statutContratRepository;

    public StatutContrat getByLibelle(String libelle) {
        return statutContratRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut de contrat '" + libelle + "' introuvable"));
    }
}