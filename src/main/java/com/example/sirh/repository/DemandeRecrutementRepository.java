package com.example.sirh.repository;

import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.DemandeRecrutement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Integer> {

    @Query("""
        SELECT new com.example.sirh.dto.DemandeRecrutementLib(
            d.idDemandeRecrutement, d.dateCreation, d.effectif, d.motif,
            m.nom, p.intitule, dep.titre
        )
        FROM DemandeRecrutement d
        JOIN d.manager m
        JOIN d.poste p
        JOIN p.departement dep
        WHERE (:dateMin IS NULL OR d.dateCreation >= :dateMin)
          AND (:dateMax IS NULL OR d.dateCreation <= :dateMax)
          AND (:idPoste IS NULL OR p.idPoste = :idPoste)
          AND (:effectifMin IS NULL OR d.effectif >= :effectifMin)
          AND (:effectifMax IS NULL OR d.effectif <= :effectifMax)
          AND (:idManager IS NULL OR m.idUtilisateur = :idManager)
          AND (:idDepartement IS NULL OR dep.idDepartement = :idDepartement)
    """)
    Page<DemandeRecrutementLib> findAllLib(
            @Param("dateMin") LocalDateTime dateMin,
            @Param("dateMax") LocalDateTime dateMax,
            @Param("idPoste") Integer idPoste,
            @Param("effectifMin") Integer effectifMin,
            @Param("effectifMax") Integer effectifMax,
            @Param("idManager") Integer idManager,
            @Param("idDepartement") Integer idDepartement,
            Pageable pageable
    );
}