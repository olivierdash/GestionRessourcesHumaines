package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.IncidentDocument;

@Repository 
public interface IncidentDocumentRepository extends JpaRepository<IncidentDocument,Integer> {
    List<IncidentDocument> findByIncidentIdIncident(Integer idIncident);
    List<IncidentDocument> findByTypePiece(String typePiece);
    long countByIncidentIdIncident(Integer idIncident);
}
