package com.example.sirh.repository;

import com.example.sirh.model.ModeleOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeleOnboardingRepository extends JpaRepository<ModeleOnboarding, Integer> {

    List<ModeleOnboarding> findAllByOrderByNomAsc();

    List<ModeleOnboarding> findAllByActifTrue();

    Optional<ModeleOnboarding> findByActifTrue();

    Optional<ModeleOnboarding> findByNomIgnoreCase(String nom);
}
