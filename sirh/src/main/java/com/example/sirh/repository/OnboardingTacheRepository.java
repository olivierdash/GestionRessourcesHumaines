package com.example.sirh.repository;

import com.example.sirh.dto.OnboardingStatutCountDto;
import com.example.sirh.model.OnboardingTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Integer> {

    List<OnboardingTache> findByOnboardingIdOrderByDateEcheanceAsc(Integer idOnboarding);

    long countByOnboardingId(Integer idOnboarding);

    long countByOnboardingIdAndStatutLibelle(Integer idOnboarding, String libelle);

    long countByOnboardingIdAndObligatoireTrueAndStatutLibelleNot(Integer idOnboarding, String libelle);

    @Query("""
        SELECT new com.example.sirh.dto.OnboardingStatutCountDto(
            t.onboarding.id,
            t.statut.libelle,
            COUNT(t)
        )
        FROM OnboardingTache t
        WHERE t.onboarding.dateFin IS NULL
        GROUP BY t.onboarding.id, t.statut.libelle
    """)
    List<OnboardingStatutCountDto> countTachesByActiveOnboardingAndStatut();
}