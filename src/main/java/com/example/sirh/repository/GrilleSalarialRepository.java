package com.example.sirh.repository;

import com.example.sirh.model.GrilleSalarial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrilleSalarialRepository extends JpaRepository<GrilleSalarial, Integer> {
    Optional<GrilleSalarial> findFirstByPosteIdPosteAndDepartementIdDepartement(Integer idPoste, Integer idDepartement);
}