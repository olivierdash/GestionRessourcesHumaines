package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.DesactivationAcces;

@Repository 
public interface DesactivationAccesRepository extends JpaRepository<DesactivationAcces,Integer> {
    List<DesactivationAcces> findByRuptureIdRupture(Integer idRupture);
    List<DesactivationAcces> findByRuptureIdRuptureAndStatut(Integer idRupture, String statut);
    long countByRuptureIdRuptureAndStatutNot(Integer idRupture, String statut);
    long countByRuptureIdRupture(Integer idRupture);
}
