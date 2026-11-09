package com.example.sirh.service;

import com.example.sirh.model.StatutTacheOnboarding;
import com.example.sirh.repository.StatutTacheOnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatutTacheOnboardingService {

    private final StatutTacheOnboardingRepository statutTacheOnboardingRepository;

    public StatutTacheOnboarding getByLibelle(String libelle) {
        return statutTacheOnboardingRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut de tâche onboarding '" + libelle + "' introuvable"));
    }
}