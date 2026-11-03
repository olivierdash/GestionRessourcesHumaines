package com.example.sirh.service;

import com.example.sirh.model.StatutProposition;
import com.example.sirh.repository.StatutPropositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatutPropositionService {

    private final StatutPropositionRepository statutPropositionRepository;

    public StatutProposition getByLibelle(String libelle) {
        return statutPropositionRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut de proposition '" + libelle + "' manquant en base "
                        + "(applique le script SQL des statuts)."));
    }
}