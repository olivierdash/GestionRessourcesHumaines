package com.example.sirh.repository;

import com.example.sirh.model.EtatDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtatDemandeRepository extends JpaRepository<EtatDemande, Integer> {
}