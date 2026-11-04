package com.example.sirh.repository;

import com.example.sirh.dto.GrilleSalarialFilter;
import com.example.sirh.model.GrilleSalarial;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrilleSalarialRepository
                extends JpaRepository<GrilleSalarial, Integer> {

        Optional<GrilleSalarial> findFirstByPosteIdPosteAndDepartementIdDepartement(
                        Integer idPoste,
                        Integer idDepartement);

        Optional<GrilleSalarial> findFirstByPosteIdPoste(Integer idPoste);

        boolean existsByPosteIdPosteAndDepartementIdDepartement(
                        Integer idPoste,
                        Integer idDepartement);

        boolean existsByPosteIdPosteAndDepartementIdDepartementAndIdGrilleNot(
                        Integer idPoste,
                        Integer idDepartement,
                        Integer idGrille);

        @Query("""
                            SELECT g FROM GrilleSalarial g
                            JOIN g.departement dep
                            WHERE dep.idDepartement = COALESCE(:#{#filter.idDepartement}, dep.idDepartement)
                              AND g.salaireMin >= COALESCE(:#{#filter.salaireMin}, g.salaireMin)
                              AND g.salaireMax <= COALESCE(:#{#filter.salaireMax}, g.salaireMax)
                        """)
        Page<GrilleSalarial> findWithFilters(@Param("filter") GrilleSalarialFilter filter, Pageable pageable);
}