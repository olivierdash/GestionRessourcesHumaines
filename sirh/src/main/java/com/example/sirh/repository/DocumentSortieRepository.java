package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.DocumentSortie;

@Repository 
public interface DocumentSortieRepository extends JpaRepository<DocumentSortie,Integer> {
    List<DocumentSortie> findByRuptureIdRupture(Integer idRupture);
    Optional<DocumentSortie> findByRuptureIdRuptureAndTypeDocument(Integer idRupture, String typeDocument);
}
