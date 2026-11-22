package com.example.sirh.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sirh.model.Absence;
import com.example.sirh.model.enums.StatutAbsence;

public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

    @EntityGraph(attributePaths = {"employe", "typeConge", "demandeConge"})
    List<Absence> findAllByOrderByDateDebutDesc();

    @EntityGraph(attributePaths = {"employe", "typeConge", "demandeConge"})
    List<Absence> findByEmployeIdEmployerOrderByDateDebutDesc(Integer employeId);

    Optional<Absence> findByDemandeCongeId(Integer demandeCongeId);

    @Query("""
            SELECT a FROM Absence a
            WHERE a.employe.idEmployer = :employeId
              AND a.dateDebut <= :date
              AND a.dateFin >= :date
              AND a.statut <> com.example.sirh.model.enums.StatutAbsence.ANNULEE
            ORDER BY a.dateDebut DESC
            """)
    List<Absence> findCouvertures(@Param("employeId") Integer employeId,
                                  @Param("date") LocalDate date);

    @Query("""
            SELECT a FROM Absence a
            WHERE a.employe.idEmployer = :employeId
              AND a.dateDebut <= :fin
              AND a.dateFin >= :debut
              AND a.statut <> com.example.sirh.model.enums.StatutAbsence.ANNULEE
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    List<Absence> findChevauchements(@Param("employeId") Integer employeId,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin,
                                     @Param("excludeId") Integer excludeId);

    boolean existsByEmployeIdEmployerAndDateDebutLessThanEqualAndDateFinGreaterThanEqualAndStatutNot(
            Integer employeId, LocalDate fin, LocalDate debut, StatutAbsence statut);

    @EntityGraph(attributePaths = {"employe", "typeConge", "demandeConge"})
    List<Absence> findByEmployeIdEmployerInAndDateDebutLessThanEqualAndDateFinGreaterThanEqualAndStatutNot(
            Collection<Integer> employeIds, LocalDate fin, LocalDate debut, StatutAbsence statut);

    @EntityGraph(attributePaths = {"employe", "typeConge", "demandeConge"})
    List<Absence> findByStatutOrderByDateDebutAsc(StatutAbsence statut);

    @EntityGraph(attributePaths = {"employe", "typeConge", "demandeConge"})
    @Query("""
            SELECT a FROM Absence a
            WHERE (:employeId IS NULL OR a.employe.idEmployer = :employeId)
              AND (:typeId IS NULL OR a.typeConge.id = :typeId)
              AND (:statut IS NULL OR a.statut = :statut)
              AND (:debut IS NULL OR a.dateFin >= :debut)
              AND (:fin IS NULL OR a.dateDebut <= :fin)
            ORDER BY a.dateDebut DESC
            """)
    List<Absence> rechercher(@Param("employeId") Integer employeId,
                             @Param("typeId") Integer typeId,
                             @Param("statut") StatutAbsence statut,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);
}
