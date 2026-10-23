package com.example.sirh.repository;

import com.example.sirh.model.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    List<Contrat> findByEmployeIdEmployer(Integer idEmployer);
}