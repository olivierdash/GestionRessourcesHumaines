package com.example.sirh.repository;

import com.example.sirh.model.GrilleSalarial;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrilleSalarialRepository extends JpaRepository<GrilleSalarial, Integer> {
    Optional<GrilleSalarial> findFirstByPosteIdPoste(Integer idPoste);
}