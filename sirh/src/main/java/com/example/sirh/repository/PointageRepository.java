package com.example.sirh.repository;

import com.example.sirh.model.Pointage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PointageRepository extends JpaRepository<Pointage, Integer> {

    @EntityGraph(attributePaths = {"employe", "employe.candidature", "employe.candidature.candidat"})
    Optional<Pointage> findByEmployeIdEmployerAndDatePointage(Integer employeId, LocalDate datePointage);

    @EntityGraph(attributePaths = {"employe", "employe.candidature", "employe.candidature.candidat"})
    @Query(value = """
            SELECT p FROM Pointage p
            WHERE (CAST(:debut AS date) IS NULL OR p.datePointage >= :debut)
              AND (CAST(:fin AS date) IS NULL OR p.datePointage <= :fin)
              AND (:employeId IS NULL OR p.employe.idEmployer = :employeId)
            """,
           countQuery = """
            SELECT COUNT(p) FROM Pointage p
            WHERE (CAST(:debut AS date) IS NULL OR p.datePointage >= :debut)
              AND (CAST(:fin AS date) IS NULL OR p.datePointage <= :fin)
              AND (:employeId IS NULL OR p.employe.idEmployer = :employeId)
            """)
    Page<Pointage> findWithFilters(@Param("debut") LocalDate debut,
                                   @Param("fin") LocalDate fin,
                                   @Param("employeId") Integer employeId,
                                   Pageable pageable);
}