package com.example.sirh.repository;

import com.example.sirh.model.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Integer> {
    List<Affectation> findByEmployeIdEmployer(Integer idEmployer);

    List<Affectation> findByEmployeIdEmployerOrderByDateDebutDesc(Integer idEmployer);

    @Query("""
            SELECT a FROM Affectation a
            JOIN FETCH a.poste p
            JOIN FETCH p.departement d
            WHERE a.employe.idEmployer = :employeId
              AND a.dateDebut <= :date
              AND (a.dateFin IS NULL OR a.dateFin >= :date)
            ORDER BY a.dateDebut DESC
            """)
    List<Affectation> findActivesForDate(@Param("employeId") Integer employeId,
                                         @Param("date") LocalDate date);
}