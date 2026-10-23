package com.example.sirh.repository;

import com.example.sirh.model.OnboardingTache;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Integer> {

    List<OnboardingTache> findByOnboardingIdOrderByDateEcheanceAsc(Integer idOnboarding);

    long countByOnboardingId(Integer idOnboarding);

    long countByOnboardingIdAndStatutLibelle(Integer idOnboarding, String libelle);

    long countByOnboardingIdAndObligatoireTrueAndStatutLibelleNot(Integer idOnboarding, String libelle);
}