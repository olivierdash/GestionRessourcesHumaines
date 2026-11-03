package com.example.sirh.repository;

import com.example.sirh.dto.DemandeRecrutementFilter;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.MotifDemande;

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

    @Query("""
                SELECT d FROM DemandeRecrutement d
                JOIN FETCH d.poste p
                JOIN FETCH p.departement dep
                JOIN FETCH d.manager m
                JOIN FETCH d.motif mo
                WHERE d.etatDemande.intitule = 'Valide'
                  AND p.idPoste = COALESCE(:#{#filter.idPoste}, p.idPoste)
                  AND dep.idDepartement = COALESCE(:#{#filter.idDepartement}, dep.idDepartement)
                  AND d.idDemandeRecrutement NOT IN (
                      SELECT o.demandeRecrutement.idDemandeRecrutement FROM OffreEmploi o
                  )
                ORDER BY d.dateCreation DESC
            """)
    List<DemandeRecrutement> getAllDemandeValide(@Param("filter") DemandeRecrutementFilter filter);

    long countByEtatDemandeIntituleIn(List<String> intitules);
    long countByEtatDemandeIntituleInAndManagerIdUtilisateur(List<String> intitules, Integer idManager);

    // NOUVEAU : Limite dynamique pour la table
    List<DemandeRecrutement> findAllByOrderByDateCreationDesc(Pageable pageable);
    List<DemandeRecrutement> findByManagerIdUtilisateurOrderByDateCreationDesc(Integer idManager, Pageable pageable);
}