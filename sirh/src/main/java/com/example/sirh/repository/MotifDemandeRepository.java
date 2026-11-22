package com.example.sirh.repository;

import com.example.sirh.model.MotifDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotifDemandeRepository extends JpaRepository<MotifDemande, Integer> {
    
    Optional<MotifDemande> findByLibelleIgnoreCase(String libelle);
}