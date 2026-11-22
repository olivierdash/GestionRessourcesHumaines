package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.HistoriqueDemandeConge;

public interface HistoriqueDemandeCongeRepository extends JpaRepository<HistoriqueDemandeConge, Integer> {

    List<HistoriqueDemandeConge> findByDemandeCongeIdOrderByDateChangementAsc(Integer demandeCongeId);
}
