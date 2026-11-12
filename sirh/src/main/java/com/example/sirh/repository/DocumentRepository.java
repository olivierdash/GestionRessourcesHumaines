package com.example.sirh.repository;

import com.example.sirh.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByEmployeIdEmployerOrderByDateDepotDesc(Integer idEmploye);

    boolean existsByTacheIdAndTypeDocumentLibelle(Integer idTache, String libelle);

    /**
     * Gestion des documents et de leur validité 
     * @param idEmploye
     * @param date
     * @param statutDocument
     * @return
     */
    List<Document> findByEmployeIdEmployerAndDateExpirationBeforeAndStatutDocument(Integer idEmploye, LocalDate date, String statutDocument);
    List<Document> findByEmployeIdEmployerAndDateExpirationBefore(Integer idEmploye, LocalDate date);
}