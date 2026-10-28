package com.example.sirh.repository;

import com.example.sirh.model.GrilleSalarial;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrilleSalarialRepository
        extends JpaRepository<GrilleSalarial, Integer> {

    Optional<GrilleSalarial>
    findFirstByPosteIdPosteAndDepartementIdDepartement(
            Integer idPoste,
            Integer idDepartement);

    Optional<GrilleSalarial>
    findFirstByPosteIdPoste(Integer idPoste);

    boolean existsByPosteIdPosteAndDepartementIdDepartement(
            Integer idPoste,
            Integer idDepartement);

    boolean existsByPosteIdPosteAndDepartementIdDepartementAndIdGrilleNot(
            Integer idPoste,
            Integer idDepartement,
            Integer idGrille);
}