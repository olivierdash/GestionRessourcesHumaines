package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Incident;


/**
 * @author moi
 * Module 11 
 * incident repository prendre les incidents en fonction de l'employer et gérant (rh ou autre )
 */
@Repository 
public interface IncidentRepository extends JpaRepository <Incident, Integer> {
    // liste de tout les incident par employer
    List<Incident> findByEmployeIdEmployer(Integer idEmployer);
    // lister les incident selon leur statut
    List<Incident> findByStatutInstruction(String statutInstruction);
    // voir les incidents déclarer par un manager spécifique 
    List<Incident> findByDeclarantIdDeclarant(Integer id_utilisateur);
    long countByEmployeIdEmployerAndStatutInstructionIn(Integer idEmploye, List<String> status);
}
