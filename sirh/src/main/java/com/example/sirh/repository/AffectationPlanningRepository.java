package com.example.sirh.repository;

import com.example.sirh.model.AffectationPlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AffectationPlanningRepository extends JpaRepository<AffectationPlanning, Integer> {

    List<AffectationPlanning> findByPlanningIdOrderByDateDebutDesc(Integer idPlanning);

    List<AffectationPlanning> findByEmployeIdEmployerOrderByDateDebutDesc(Integer employeId);

    @Query("""
            SELECT a FROM AffectationPlanning a
            JOIN FETCH a.planning p
            JOIN FETCH p.regimeTravail r
            LEFT JOIN FETCH r.horaires
            WHERE a.employe.idEmployer = :employeId
              AND a.dateDebut <= :datePointage
              AND (a.dateFin IS NULL OR a.dateFin >= :datePointage)
              AND p.dateDebut <= :datePointage
              AND (p.dateFin IS NULL OR p.dateFin >= :datePointage)
            ORDER BY a.dateDebut DESC
            """)
    List<AffectationPlanning> findActivesForDate(@Param("employeId") Integer employeId,
                                                 @Param("datePointage") LocalDate datePointage);
}