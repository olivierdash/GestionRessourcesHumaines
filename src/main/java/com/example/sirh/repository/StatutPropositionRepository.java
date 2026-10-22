package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.StatutProposition;

@Repository 
public interface StatutPropositionRepository extends JpaRepository<StatutProposition, Integer> {
}
