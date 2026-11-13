package com.example.sirh.repository;

import com.example.sirh.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    // ---------------------------------------------------------------------
    // 1. Recherche par employé
    // ---------------------------------------------------------------------

    @EntityGraph(attributePaths = {"typeDocument", "tache", "deposePar"})
    List<Document> findByEmployeIdEmployerOrderByDateDepotDesc(Integer idEmploye);

    @EntityGraph(attributePaths = {"typeDocument", "tache", "deposePar"})
    Page<Document> findByEmployeIdEmployer(Integer idEmploye, Pageable pageable);

    Optional<Document> findByIdDocumentAndEmployeIdEmployer(Integer idDocument, Integer idEmploye);

    long countByEmployeIdEmployer(Integer idEmploye);

    // ---------------------------------------------------------------------
    // 2. Recherche par tâche d'onboarding
    // ---------------------------------------------------------------------

    boolean existsByTacheIdAndTypeDocumentLibelle(Integer idTache, String libelle);

    boolean existsByTacheId(Integer idTache);

    @EntityGraph(attributePaths = {"typeDocument", "employe"})
    List<Document> findByTacheId(Integer idTache);

    /** Documents non rattachés à une tâche (id_tache IS NULL en base). */
    List<Document> findByTacheIsNull();

    // ---------------------------------------------------------------------
    // 3. Recherche par type de document
    // ---------------------------------------------------------------------

    @EntityGraph(attributePaths = {"employe", "tache", "deposePar"})
    List<Document> findByEmployeIdEmployerAndTypeDocumentIdTypeDocument(
            Integer idEmploye, Integer idTypeDocument);

    // ---------------------------------------------------------------------
    // 4. Recherche par période de dépôt (date_depot existe en base)
    // ---------------------------------------------------------------------

    @Query("""
        select d from Document d
        join fetch d.typeDocument td
        where d.employe.idEmployer = :idEmploye
          and d.dateDepot between :debut and :fin
        order by d.dateDepot desc
        """)
    List<Document> findByEmployeAndDateDepotBetween(
            @Param("idEmploye") Integer idEmploye,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    // ---------------------------------------------------------------------
    // 5. Opérations groupées
    // ---------------------------------------------------------------------

    @Modifying
    @Query("delete from Document d where d.tache.id = :idTache")
    int deleteByTacheId(@Param("idTache") Integer idTache);
}