package com.example.sirh.repository;

import com.example.sirh.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByEmployeIdEmployerOrderByDateDepotDesc(Integer idEmploye);

    boolean existsByTacheIdAndTypeDocumentLibelle(Integer idTache, String libelle);
}