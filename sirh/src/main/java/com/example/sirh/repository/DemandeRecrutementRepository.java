package com.example.sirh.repository;

import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.MotifDemande;
import com.example.sirh.wrapper.DemandeRecrutementFilter;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRecrutementRepository
        extends JpaRepository<DemandeRecrutement, Integer> {

    @Query(value = """
                SELECT new com.example.sirh.dto.DemandeRecrutementLib(
                    d.idDemandeRecrutement,
                    d.dateCreation,
                    d.effectif,
                    d.motif,
                    m.nom,
                    p.intitule,
                    dep.titre
                )
                FROM DemandeRecrutement d
                JOIN d.manager m
                JOIN d.poste p
                JOIN p.departement dep
                JOIN d.motif mo
                WHERE d.dateCreation >= COALESCE(:#{#filter.dateMin?.atStartOfDay()}, d.dateCreation)
                  AND d.dateCreation <= COALESCE(:#{#filter.dateMax?.atTime(T(java.time.LocalTime).MAX)}, d.dateCreation)
                  AND p.idPoste = COALESCE(:#{#filter.idPoste}, p.idPoste)
                  AND d.effectif >= COALESCE(:#{#filter.effectifMin}, d.effectif)
                  AND d.effectif <= COALESCE(:#{#filter.effectifMax}, d.effectif)
                  AND m.idUtilisateur = COALESCE(:#{#filter.idManager}, m.idUtilisateur)
                  AND dep.idDepartement = COALESCE(:#{#filter.idDepartement}, dep.idDepartement)
                  AND mo.idMotifDemande = COALESCE(:#{#filter.idMotifDemande}, mo.idMotifDemande)
            """, countQuery = """
                SELECT COUNT(d)
                FROM DemandeRecrutement d
                JOIN d.manager m
                JOIN d.poste p
                JOIN p.departement dep
                JOIN d.motif mo
                WHERE d.dateCreation >= COALESCE(:#{#filter.dateMin?.atStartOfDay()}, d.dateCreation)
                  AND d.dateCreation <= COALESCE(:#{#filter.dateMax?.atTime(T(java.time.LocalTime).MAX)}, d.dateCreation)
                  AND p.idPoste = COALESCE(:#{#filter.idPoste}, p.idPoste)
                  AND d.effectif >= COALESCE(:#{#filter.effectifMin}, d.effectif)
                  AND d.effectif <= COALESCE(:#{#filter.effectifMax}, d.effectif)
                  AND m.idUtilisateur = COALESCE(:#{#filter.idManager}, m.idUtilisateur)
                  AND dep.idDepartement = COALESCE(:#{#filter.idDepartement}, dep.idDepartement)
                  AND mo.idMotifDemande = COALESCE(:#{#filter.idMotifDemande}, mo.idMotifDemande)
            """)
    Page<DemandeRecrutementLib> findAllLib(
            @Param("filter") DemandeRecrutementFilter filter,
            Pageable pageable);

    @Query("""
                SELECT d FROM DemandeRecrutement d
                WHERE d.idDemandeRecrutement NOT IN (
                    SELECT o.demandeRecrutement.idDemandeRecrutement FROM OffreEmploi o
                )
            """)
    List<DemandeRecrutement> findDemandesSansOffre();

    List<DemandeRecrutement> findByEtatDemandeIntituleIgnoreCaseOrderByDateCreationDesc(String intitule);
}