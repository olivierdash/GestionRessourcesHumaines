package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sirh.model.SoldeConge;

import jakarta.persistence.LockModeType;

public interface SoldeCongeRepository extends JpaRepository<SoldeConge, Integer> {

    Optional<SoldeConge> findByEmployeIdEmployerAndTypeCongeIdAndAnnee(
            Integer employeId, Integer typeCongeId, Integer annee);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM SoldeConge s
            WHERE s.employe.idEmployer = :employeId
              AND s.typeConge.id = :typeId
              AND s.annee = :annee
            """)
    Optional<SoldeConge> findForUpdate(@Param("employeId") Integer employeId,
                                       @Param("typeId") Integer typeId,
                                       @Param("annee") Integer annee);

    @EntityGraph(attributePaths = {"employe", "typeConge"})
    List<SoldeConge> findByAnneeOrderByEmployeMatriculeAscTypeCongeOrdreAffichageAsc(Integer annee);

    @EntityGraph(attributePaths = {"employe", "typeConge"})
    List<SoldeConge> findByEmployeIdEmployerAndAnneeOrderByTypeCongeOrdreAffichageAsc(
            Integer employeId, Integer annee);

    @EntityGraph(attributePaths = {"employe", "typeConge"})
    @Query("""
            SELECT s FROM SoldeConge s
            WHERE (:annee IS NULL OR s.annee = :annee)
              AND (:employeId IS NULL OR s.employe.idEmployer = :employeId)
              AND (:typeId IS NULL OR s.typeConge.id = :typeId)
            ORDER BY s.annee DESC, s.employe.matricule ASC, s.typeConge.ordreAffichage ASC
            """)
    List<SoldeConge> rechercher(@Param("annee") Integer annee,
                                @Param("employeId") Integer employeId,
                                @Param("typeId") Integer typeId);

    List<SoldeConge> findByAnnee(Integer annee);
}
