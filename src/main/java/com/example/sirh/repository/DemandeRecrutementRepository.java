package com.example.sirh.repository;

import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.wrapper.DemandeRecrutementFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Integer> {

    @Query("""
        SELECT new com.example.sirh.dto.DemandeRecrutementLib(
            d.idDemandeRecrutement, d.dateCreation, d.effectif, d.motif,
            m.nom, p.intitule, dep.titre
        )
        FROM DemandeRecrutement d
        JOIN d.manager m
        JOIN d.poste p
        JOIN p.departement dep
        WHERE d.dateCreation >= :#{#filter.dateMin == null ? T(java.time.LocalDate).of(1900,1,1).atStartOfDay() : #filter.dateMin.atStartOfDay()}
          AND d.dateCreation <= :#{#filter.dateMax == null ? T(java.time.LocalDate).of(2999,12,31).atTime(T(java.time.LocalTime).MAX) : #filter.dateMax.atTime(T(java.time.LocalTime).MAX)}
          AND (:#{#filter.idPoste == null ? -1 : #filter.idPoste} = -1
               OR p.idPoste = :#{#filter.idPoste == null ? -1 : #filter.idPoste})
          AND d.effectif >= :#{#filter.effectifMin == null ? 0 : #filter.effectifMin}
          AND d.effectif <= :#{#filter.effectifMax == null ? T(java.lang.Integer).MAX_VALUE : #filter.effectifMax}
          AND (:#{#filter.idManager == null ? -1 : #filter.idManager} = -1
               OR m.idUtilisateur = :#{#filter.idManager == null ? -1 : #filter.idManager})
          AND (:#{#filter.idDepartement == null ? -1 : #filter.idDepartement} = -1
               OR dep.idDepartement = :#{#filter.idDepartement == null ? -1 : #filter.idDepartement})
    """)
    Page<DemandeRecrutementLib> findAllLib(
            @Param("filter") DemandeRecrutementFilter filter,
            Pageable pageable
    );
}