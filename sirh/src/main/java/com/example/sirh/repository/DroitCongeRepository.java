package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.DroitConge;

public interface DroitCongeRepository extends JpaRepository<DroitConge, Integer> {

    @EntityGraph(attributePaths = {"typeConge", "typeContrat"})
    List<DroitConge> findAllByOrderByTypeCongeLibelleAscAncienneteMinMoisAsc();

    @EntityGraph(attributePaths = {"typeConge", "typeContrat"})
    List<DroitConge> findByActifTrueOrderByTypeCongeLibelleAscAncienneteMinMoisAsc();

    @EntityGraph(attributePaths = {"typeConge", "typeContrat"})
    List<DroitConge> findByTypeCongeIdAndActifTrue(Integer idTypeConge);

    List<DroitConge> findByTypeCongeId(Integer idTypeConge);
}
