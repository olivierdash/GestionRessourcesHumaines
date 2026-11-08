package com.example.sirh.repository;

import com.example.sirh.dto.PosteFilter;
import com.example.sirh.model.Poste;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Integer> {

        boolean existsByIntituleIgnoreCaseAndDepartement_IdDepartement(
                        String intitule,
                        Integer idDepartement);

        boolean existsByIntituleIgnoreCaseAndDepartement_IdDepartementAndIdPosteNot(
                        String intitule,
                        Integer idDepartement,
                        Integer idPoste);

        List<Poste> findByDepartementIdDepartementOrderByIntituleAsc(Integer idDepartement);

        @Query("""
                        SELECT p FROM Poste p
                        WHERE (:#{#f.q} IS NULL OR LOWER(p.intitule) LIKE LOWER(CONCAT('%', :#{#f.q}, '%')))
                          AND p.departement.idDepartement = COALESCE(:#{#f.idDepartement}, p.departement.idDepartement)
                          AND (:#{#f.idLieu} IS NULL OR p.lieu.idLieu = :#{#f.idLieu})
                        """)
        Page<Poste> findWithFilters(@Param("f") PosteFilter filter, Pageable pageable);
}