package com.example.sirh.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.enums.StatutDemandeConge;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Integer> {

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    List<DemandeConge> findByEmployeIdEmployerOrderByDateCreationDesc(Integer employeId);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    List<DemandeConge> findByStatutOrderByDateCreationAsc(StatutDemandeConge statut);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    Page<DemandeConge> findByStatutOrderByDateCreationAsc(
            StatutDemandeConge statut, Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    List<DemandeConge> findByStatutInOrderByDateCreationAsc(Collection<StatutDemandeConge> statuts);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    Page<DemandeConge> findByStatutInOrderByDateCreationAsc(
            Collection<StatutDemandeConge> statuts, Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    List<DemandeConge> findByStatutAndManagerAttenduIdUtilisateurOrderByDateCreationAsc(
            StatutDemandeConge statut, Integer managerId);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    Page<DemandeConge> findByStatutAndManagerAttenduIdUtilisateurOrderByDateCreationAsc(
            StatutDemandeConge statut, Integer managerId, Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    List<DemandeConge> findByManagerAttenduIdUtilisateurAndStatutInOrderByDateCreationAsc(
            Integer managerId, Collection<StatutDemandeConge> statuts);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    Page<DemandeConge> findByManagerAttenduIdUtilisateurAndStatutInOrderByDateCreationAsc(
            Integer managerId, Collection<StatutDemandeConge> statuts, Pageable pageable);

    long countByStatut(StatutDemandeConge statut);

    long countByStatutIn(Collection<StatutDemandeConge> statuts);

    long countByStatutAndManagerAttenduIdUtilisateur(StatutDemandeConge statut, Integer managerId);

    long countByManagerAttenduIdUtilisateurAndStatutIn(
            Integer managerId, Collection<StatutDemandeConge> statuts);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    @Query("""
            SELECT d FROM DemandeConge d
            WHERE d.statut IN :statuts
              AND d.dateDebut <= :fin
              AND d.dateFin >= :debut
            ORDER BY d.dateDebut ASC
            """)
    List<DemandeConge> findActivesDansPeriode(@Param("debut") LocalDate debut,
                                              @Param("fin") LocalDate fin,
                                              @Param("statuts") Collection<StatutDemandeConge> statuts);

    @Query("""
            SELECT d FROM DemandeConge d
            WHERE d.employe.idEmployer = :employeId
              AND d.statut IN :statuts
              AND d.dateDebut <= :fin
              AND d.dateFin >= :debut
              AND (:excludeId IS NULL OR d.id <> :excludeId)
            """)
    List<DemandeConge> findChevauchements(@Param("employeId") Integer employeId,
                                           @Param("debut") LocalDate debut,
                                           @Param("fin") LocalDate fin,
                                           @Param("statuts") Collection<StatutDemandeConge> statuts,
                                           @Param("excludeId") Integer excludeId);

    @Query("""
            SELECT COUNT(d) FROM DemandeConge d
            WHERE d.statut IN :statuts
              AND d.dateDebut <= :fin
              AND d.dateFin >= :debut
            """)
    long countChevauchementsPeriode(@Param("debut") LocalDate debut,
                                    @Param("fin") LocalDate fin,
                                    @Param("statuts") Collection<StatutDemandeConge> statuts);

    @EntityGraph(attributePaths = {"employe", "typeConge", "createur", "managerAttendu"})
    @Query("""
            SELECT d FROM DemandeConge d
            WHERE (:employeId IS NULL OR d.employe.idEmployer = :employeId)
              AND (:typeId IS NULL OR d.typeConge.id = :typeId)
              AND (:statut IS NULL OR d.statut = :statut)
              AND (:debut IS NULL OR d.dateFin >= :debut)
              AND (:fin IS NULL OR d.dateDebut <= :fin)
            ORDER BY d.dateCreation DESC
            """)
    List<DemandeConge> rechercher(@Param("employeId") Integer employeId,
                                  @Param("typeId") Integer typeId,
                                  @Param("statut") StatutDemandeConge statut,
                                  @Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin);
}
