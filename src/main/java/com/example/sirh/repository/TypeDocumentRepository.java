package com.example.sirh.repository;

import com.example.sirh.model.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Integer> {

    Optional<TypeDocument> findByLibelle(String libelle);
}