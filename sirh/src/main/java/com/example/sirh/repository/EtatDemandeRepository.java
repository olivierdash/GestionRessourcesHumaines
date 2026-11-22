package com.example.sirh.repository;

import com.example.sirh.model.EtatDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtatDemandeRepository extends JpaRepository<EtatDemande, Integer> {
    Optional<EtatDemande> findByIntituleIgnoreCase(String intitule);

    List<EtatDemande> findByIntituleInIgnoreCase(String[] intitules);
}