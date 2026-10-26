package com.example.sirh.repository;

import com.example.sirh.model.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Integer> {
    List<Affectation> findByEmployeIdEmployer(Integer idEmployer);
}