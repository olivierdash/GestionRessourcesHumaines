package com.example.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Competence;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Integer> {
    Optional<Competence> findByCode(String code);
}
