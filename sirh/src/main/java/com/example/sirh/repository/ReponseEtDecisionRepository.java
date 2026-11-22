package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.ReponseEtDecision;

/**
 * @author moi 
 * module 11
 * ReponseEtDecisionRepository
 * 
 */
@Repository 
public interface ReponseEtDecisionRepository extends JpaRepository<ReponseEtDecision, Integer> {
    List<ReponseEtDecision> findByDemandeIncidentEmployerIdEmployer(Integer idEmployer);
    
    Optional<ReponseEtDecision> findByDemandeIdDemande(Integer idDemande);
}
