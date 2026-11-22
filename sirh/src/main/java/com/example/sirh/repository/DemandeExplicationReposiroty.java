package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.DemandeExplication;

/**
 * @author moi 
 * Module 11
 * DemandeExplicationReposiroty
 */

@Repository 
public interface DemandeExplicationReposiroty extends JpaRepository <DemandeExplication, Integer>{
    List<DemandeExplication> findByIncidentEmployeIdEmployer(Integer idEmployer);
    
    Optional<DemandeExplication> findByIncidentIdIncident(Integer idIncident);
    
    // Trouver les courriers envoyés par un RH spécifique
    List<DemandeExplication> findByEnvoyeurIdUtilisateur(Integer idEnvoyeur);
}
