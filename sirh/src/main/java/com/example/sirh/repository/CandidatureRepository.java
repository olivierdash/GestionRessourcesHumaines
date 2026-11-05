package com.example.sirh.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Candidature;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    List<Candidature> findByOffreIdOffre(Integer id_offre);

    List<Candidature> findByCandidatIdCandidatOrderByDateDepotCandidatureDesc(Integer id_candidat);

    boolean existsByCandidatIdCandidatAndOffreIdOffre(Integer idCandidat, Integer idOffre);

    @Query("""
                SELECT c FROM Candidature c
                LEFT JOIN Employe e ON e.candidature.idCandidature = c.idCandidature
                WHERE e.idEmployer IS NULL
            """)
    List<Candidature> findCandidaturesSansEmploye();

    @Query(value = """
                SELECT COUNT(*)
                FROM candidature c
                JOIN statut_candidature sc ON sc.id_statut_candidature = c.id_statut_candidature
                JOIN demande_recrutement dr ON dr.id_demande_recrutement = c.id_demande_recrutement
                WHERE c.date_depot_candidature >= COALESCE(CAST(:start AS timestamp), c.date_depot_candidature)
                  AND c.date_depot_candidature <= COALESCE(CAST(:end AS timestamp), c.date_depot_candidature)
                  AND (:idManager IS NULL OR dr.id_manager = :idManager)
                  AND (:statutExclude IS NULL OR LOWER(sc.libelle) <> LOWER(:statutExclude))
            """, nativeQuery = true)
    long countByPeriodWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("idManager") Integer idManager,
            @Param("statutExclude") String statutExclude);

    @Query(value = """
                SELECT c.*
                FROM candidature c
                JOIN demande_recrutement dr ON dr.id_demande_recrutement = c.id_demande_recrutement
                WHERE c.date_depot_candidature >= COALESCE(CAST(:start AS timestamp), c.date_depot_candidature)
                  AND c.date_depot_candidature <= COALESCE(CAST(:end AS timestamp), c.date_depot_candidature)
                  AND (:idManager IS NULL OR dr.id_manager = :idManager)
            """, nativeQuery = true)
    List<Candidature> findByPeriodWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("idManager") Integer idManager);
}