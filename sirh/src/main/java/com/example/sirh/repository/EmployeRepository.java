package com.example.sirh.repository;

import com.example.sirh.dto.EmployeFilter;
import com.example.sirh.dto.HiringDelayDto;
import com.example.sirh.model.Employe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    long countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(Integer idDepartement);

    boolean existsByCandidatureIdCandidature(Integer idCandidature);

    Optional<Employe> findByCandidatureIdCandidature(Integer idCandidature);

    @Query("""
            SELECT e FROM Employe e
            JOIN e.candidature c
            JOIN c.demandeRecrutement dr
            JOIN dr.poste p
            JOIN p.departement dep
            WHERE dep.idDepartement = COALESCE(:#{#filter.idDepartement}, dep.idDepartement)
              AND e.hiringDate >= COALESCE(:#{#filter.dateMin}, e.hiringDate)
              AND e.hiringDate <= COALESCE(:#{#filter.dateMax}, e.hiringDate)
              AND (:#{#filter.statusEmployer} IS NULL OR e.statusEmploye.id = :#{#filter.statusEmployer})
            """)
    Page<Employe> findWithFilters(@Param("filter") EmployeFilter filter, Pageable pageable);

    @Query("""
                SELECT new com.example.sirh.dto.HiringDelayDto(
                    c.demandeRecrutement.dateCreation,
                    e.hiringDate
                )
                FROM Employe e
                JOIN e.candidature c
                WHERE e.hiringDate BETWEEN :start AND :end
            """)
    List<HiringDelayDto> findHiringDatesAndDemandCreation(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}