package com.example.sirh.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.sirh.model.CoordonneesEmploye;
import com.example.sirh.model.Employe;

import java.util.Optional;

@Repository 
public interface CoordonneesEmployeRepository extends JpaRepository<CoordonneesEmploye, Integer> {
    Optional<CoordonneesEmploye> findByEmploye(Employe employe);
}
