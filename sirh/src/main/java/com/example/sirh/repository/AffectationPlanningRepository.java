package com.example.sirh.repository;

import com.example.sirh.model.AffectationPlanning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffectationPlanningRepository extends JpaRepository<AffectationPlanning, Integer> {
    List<AffectationPlanning> findByPlanningIdOrderByDateDebutDesc(Integer idPlanning);
}
