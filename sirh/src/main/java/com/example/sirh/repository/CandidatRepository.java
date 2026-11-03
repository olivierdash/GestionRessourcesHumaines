package com.example.sirh.repository;

import com.example.sirh.model.Candidat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Integer> {
    Optional<Candidat> findByEmail(String email);
}