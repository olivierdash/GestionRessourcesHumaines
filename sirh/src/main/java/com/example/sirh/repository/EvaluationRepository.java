package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Evaluation;
@Repository 
public interface EvaluationRepository extends JpaRepository<Evaluation,Integer> {
    List<Evaluation> findByCandidatureIdCandidature(Integer id_candidature);
}
