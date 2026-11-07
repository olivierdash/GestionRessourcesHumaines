package com.example.sirh.repository;

import com.example.sirh.model.StatutEmploye;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatutEmployeRepository extends JpaRepository<StatutEmploye, Integer> {
    Optional<StatutEmploye> findByLibelle(String libelle);
}