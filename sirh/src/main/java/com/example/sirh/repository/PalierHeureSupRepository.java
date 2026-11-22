package com.example.sirh.repository;

import com.example.sirh.model.PalierHeureSup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PalierHeureSupRepository
        extends JpaRepository<PalierHeureSup, Integer> {

    List<PalierHeureSup> findByActifTrueOrderByOrdreAsc();
}