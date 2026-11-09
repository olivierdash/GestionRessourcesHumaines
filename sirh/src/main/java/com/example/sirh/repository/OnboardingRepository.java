package com.example.sirh.repository;

import com.example.sirh.model.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingRepository extends JpaRepository<Onboarding, Integer> {

    Optional<Onboarding> findByEmployeIdEmployer(Integer idEmploye);

    List<Onboarding> findByDateFinIsNull();

    long countByDateFinIsNull();
}