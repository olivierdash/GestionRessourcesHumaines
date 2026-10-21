package com.example.sirh.repository;

import com.example.sirh.model.HistoriqueDemandeRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueDemandeRecrutementRepository 
        extends JpaRepository<HistoriqueDemandeRecrutement, Integer> {

    @Query("""
        SELECT h FROM HistoriqueDemandeRecrutement h 
        WHERE h.demandeRecrutement.idDemandeRecrutement = :idDemande 
        ORDER BY h.dateChangement DESC
    """)
    List<HistoriqueDemandeRecrutement> getByIdDemandeRecrutement(@Param("idDemande") Integer idDemande);
}