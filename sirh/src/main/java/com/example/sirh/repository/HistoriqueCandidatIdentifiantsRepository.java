package com.example.sirh.repository;

import com.example.sirh.model.HistoriqueCandidatIdentifiants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueCandidatIdentifiantsRepository
        extends JpaRepository<HistoriqueCandidatIdentifiants, Integer> {

    List<HistoriqueCandidatIdentifiants> findByCandidatIdCandidatOrderByDateChangementDesc(
            Integer idCandidat);
}