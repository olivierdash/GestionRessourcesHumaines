package com.example.sirh.repository;

import com.example.sirh.model.ValidationProposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationPropositionRepository extends JpaRepository<ValidationProposition, Integer> {
    List<ValidationProposition> findByPropositionIdPropositionOrderByIdDesc(Integer idProposition);
}