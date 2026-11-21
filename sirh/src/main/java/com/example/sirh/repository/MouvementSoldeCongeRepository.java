package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.MouvementSoldeConge;

public interface MouvementSoldeCongeRepository extends JpaRepository<MouvementSoldeConge, Integer> {

    List<MouvementSoldeConge> findBySoldeCongeIdOrderByDateMouvementDesc(Integer soldeCongeId);

    List<MouvementSoldeConge> findByDemandeCongeIdOrderByDateMouvementDesc(Integer demandeCongeId);
}
