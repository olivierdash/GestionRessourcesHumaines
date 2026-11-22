package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.RuptureContrat;
/**
 * @author moi
 * module 12
 * RuptureContratRepository
 * archivage en gros genre pour voir la rupture ou autre 
 */

@Repository 
public interface RuptureContratRepository extends JpaRepository<RuptureContrat,Integer> {
    Optional<RuptureContrat> findByEmployeIdEmployer(Integer idEmployer);
    
    Optional<RuptureContrat> findByContratIdContract(Integer idContract);
    List<RuptureContrat> findByArchiveFalse();
    List<RuptureContrat> findByStatutRupture(String statutRupture);
    // Connaître les dossiers initiés par un utilisateur spécifique
    List<RuptureContrat> findByInitieParIdUtilisateur(Integer idUtilisateur);
}
