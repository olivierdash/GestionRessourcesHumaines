package com.example.sirh.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sirh.model.VariablePaie;
import com.example.sirh.model.enums.CodeVariablePaie;
import com.example.sirh.model.enums.StatutVariablePaie;

public interface VariablePaieRepository extends JpaRepository<VariablePaie, Integer> {

    List<VariablePaie> findByDemandeCongeId(Integer demandeCongeId);

    List<VariablePaie> findByAbsenceId(Integer absenceId);

    boolean existsByDemandeCongeIdAndPeriodeAndCode(
            Integer demandeCongeId, LocalDate periode, CodeVariablePaie code);

    boolean existsByAbsenceIdAndPeriodeAndCode(
            Integer absenceId, LocalDate periode, CodeVariablePaie code);

    @EntityGraph(attributePaths = {"employe", "demandeConge", "absence"})
    List<VariablePaie> findByPeriodeBetweenOrderByPeriodeDescEmployeMatriculeAsc(
            LocalDate debut, LocalDate fin);

    @EntityGraph(attributePaths = {"employe", "demandeConge", "absence"})
    @Query("""
            SELECT v FROM VariablePaie v
            WHERE v.periode >= :debut
              AND v.periode <= :fin
              AND (:employeId IS NULL OR v.employe.idEmployer = :employeId)
              AND (:statut IS NULL OR v.statut = :statut)
            ORDER BY v.periode DESC, v.employe.matricule ASC
            """)
    List<VariablePaie> rechercher(@Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin,
                                  @Param("employeId") Integer employeId,
                                  @Param("statut") StatutVariablePaie statut);
}
