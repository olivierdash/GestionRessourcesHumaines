package com.example.sirh.repository;

import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.wrapper.DemandeRecrutementFilter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Integer> {

    /*@Query("""
        SELECT new com.example.sirh.dto.DemandeRecrutementLib(
            d.idDemandeRecrutement, d.dateCreation, d.effectif, d.motif,
            m.nom, p.intitule, dep.titre
        )
        FROM DemandeRecrutement d
        JOIN d.manager m
        JOIN d.poste p
        JOIN p.departement dep
        WHERE (CAST(:#{#filter.dateMin} AS timestamp) IS NULL OR d.dateCreation >= :#{#filter.dateMin.atStartOfDay()})
          AND (CAST(:#{#filter.dateMax} AS timestamp) IS NULL OR d.dateCreation <= :#{#filter.dateMax.atTime(T(java.time.LocalTime).MAX)})
          AND (CAST(:#{#filter.idPoste} AS integer) IS NULL OR p.idPoste = :#{#filter.idPoste})
          AND (CAST(:#{#filter.effectifMin} AS integer) IS NULL OR d.effectif >= :#{#filter.effectifMin})
          AND (CAST(:#{#filter.effectifMax} AS integer) IS NULL OR d.effectif <= :#{#filter.effectifMax})
          AND (CAST(:#{#filter.idManager} AS integer) IS NULL OR m.idUtilisateur = :#{#filter.idManager})
          AND (CAST(:#{#filter.idDepartement} AS integer) IS NULL OR dep.idDepartement = :#{#filter.idDepartement})
    """)
    Page<DemandeRecrutementLib> findAllLib(
            @Param("filter") DemandeRecrutementFilter filter, 
            Pageable pageable
    );*/
    @Query("""
        SELECT new com.example.sirh.dto.DemandeRecrutementLib(
            d.idDemandeRecrutement, d.dateCreation, d.effectif, d.motif,
            m.nom, p.intitule, dep.titre 
        )
        FROM DemandeRecrutement d
        JOIN d.manager m
        JOIN d.poste p
        JOIN p.departement dep
        WHERE (CAST(:#{#filter.dateMin} AS timestamp) IS NULL OR d.dateCreation >= :#{#filter.dateMin})
          AND (CAST(:#{#filter.dateMax} AS timestamp) IS NULL OR d.dateCreation <= :#{#filter.dateMax})
          AND (CAST(:#{#filter.idPoste} AS integer) IS NULL OR p.idPoste = :#{#filter.idPoste})
          AND (CAST(:#{#filter.effectifMin} AS integer) IS NULL OR d.effectif >= :#{#filter.effectifMin})
          AND (CAST(:#{#filter.effectifMax} AS integer) IS NULL OR d.effectif <= :#{#filter.effectifMax})
          AND (CAST(:#{#filter.idManager} AS integer) IS NULL OR m.idUtilisateur = :#{#filter.idManager})
          AND (CAST(:#{#filter.idDepartement} AS integer) IS NULL OR dep.idDepartement = :#{#filter.idDepartement})
    """)
    Page<DemandeRecrutementLib> findAllLib(
            @Param("filter") DemandeRecrutementFilter filter,
            Pageable pageable
    );

    @Query("SELECT d FROM DemandeRecrutement d WHERE d.idDemandeRecrutement NOT IN (SELECT o.demandeRecrutement.idDemandeRecrutement FROM OffreEmploi o)")
    List<DemandeRecrutement> findDemandesSansOffre();
}