package com.example.sirh.service;

import com.example.sirh.model.OnboardingTache;
import com.example.sirh.repository.OnboardingTacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingTacheService {

    private final OnboardingTacheRepository onboardingTacheRepository;

    @Transactional(readOnly = true)
    public OnboardingTache getById(Integer id) {
        return onboardingTacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable : " + id));
    }

    @Transactional
    public OnboardingTache save(OnboardingTache tache) {
        return onboardingTacheRepository.save(tache);
    }

    @Transactional
    public OnboardingTache saveAndFlush(OnboardingTache tache) {
        onboardingTacheRepository.saveAndFlush(tache);
        return tache;
    }

    @Transactional(readOnly = true)
    public List<OnboardingTache> findByOnboarding(Integer idOnboarding) {
        return onboardingTacheRepository.findByOnboardingIdOrderByDateEcheanceAsc(idOnboarding);
    }

    @Transactional(readOnly = true)
    public long countByOnboarding(Integer idOnboarding) {
        return onboardingTacheRepository.countByOnboardingId(idOnboarding);
    }

    @Transactional(readOnly = true)
    public long countTerminees(Integer idOnboarding) {
        return onboardingTacheRepository
                .countByOnboardingIdAndStatutLibelle(idOnboarding, "terminée");
    }

    @Transactional(readOnly = true)
    public long countObligatoiresNonTerminees(Integer idOnboarding) {
        return onboardingTacheRepository
                .countByOnboardingIdAndObligatoireTrueAndStatutLibelleNot(idOnboarding, "terminée");
    }
}