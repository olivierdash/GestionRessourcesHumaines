package com.example.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.StatutTacheOnboarding;

public interface StatutTacheOnboardingRepository extends JpaRepository<StatutTacheOnboarding, Integer> {

    Optional<StatutTacheOnboarding> findByLibelle(String libelle);
}