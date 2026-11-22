package com.example.sirh.repository;

import com.example.sirh.model.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatutContratRepository extends JpaRepository<StatutContrat, Integer> {

    Optional<StatutContrat> findByLibelle(String libelle);
}