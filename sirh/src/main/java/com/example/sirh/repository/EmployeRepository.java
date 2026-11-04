package com.example.sirh.repository;

import com.example.sirh.dto.EmployeFilter;
import com.example.sirh.model.Employe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
                  AND e.statusEmployer = COALESCE(:#{#filter.statusEmployer}, e.statusEmployer)
            """)
    Page<Employe> findWithFilters(@Param("filter") EmployeFilter filter, Pageable pageable);
}