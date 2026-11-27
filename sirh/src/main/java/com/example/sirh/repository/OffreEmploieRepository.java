
package com.example.sirh.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sirh.dto.OffreFilter;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.StatutOffreEmploi;

@Repository
public interface OffreEmploieRepository extends JpaRepository<OffreEmploi, Integer> {

    List<OffreEmploi> findAllByOrderByDatePublicationDesc();

    List<OffreEmploi> findByStatutId(Integer idStatut);

    long countByStatutLibelle(String libelle);

    long countByStatutLibelleAndDemandeRecrutementManagerIdUtilisateur(
            String libelle, Integer idManager);

    long countByDatePublicationBetween(LocalDateTime start, LocalDateTime end);

    // Offres visibles par les candidats à l'instant demandé
    @Query("""
            SELECT o FROM OffreEmploi o
            WHERE o.statut.libelle = 'PUBLIEE'
              AND o.datePublication <= :maintenant
              AND o.dateCloture > :maintenant
            ORDER BY o.datePublication DESC
            """)
    List<OffreEmploi> findOffresPubliquesActives(
            @Param("maintenant") LocalDateTime maintenant);

    // Clôture en base des offres dont la date et l'heure sont atteintes
    @Modifying
    @Query("""
            UPDATE OffreEmploi o
            SET o.statut = :statutCloturee
            WHERE o.statut.libelle = 'PUBLIEE'
              AND o.dateCloture IS NOT NULL
              AND o.dateCloture <= :maintenant
            """)
    int cloturerOffresExpirees(
            @Param("statutCloturee") StatutOffreEmploi statutCloturee,
            @Param("maintenant") LocalDateTime maintenant);

    @Query("""
            SELECT o FROM OffreEmploi o
            JOIN o.demandeRecrutement dr
            JOIN dr.manager m
            WHERE m.idUtilisateur = COALESCE(:#{#filter.idManager}, m.idUtilisateur)
              AND o.datePublication >= COALESCE(:#{#filter.dateMin}, o.datePublication)
              AND o.datePublication <= COALESCE(:#{#filter.dateMax}, o.datePublication)
              AND o.statut.id = COALESCE(:#{#filter.idStatut}, o.statut.id)
            """)
    Page<OffreEmploi> findWithFilters(
            @Param("filter") OffreFilter filter,
            Pageable pageable);
}
