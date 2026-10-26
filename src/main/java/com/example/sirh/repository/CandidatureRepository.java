package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Candidature;

@Repository 
public interface CandidatureRepository extends JpaRepository<Candidature,Integer> { 
    List<Candidature> findByOffreIdOffre(Integer id_offre);
    List<Candidature> findByCandidatIdCandidatOrderByDateDepotCandidatureDesc(Integer id_candidat);
    @Query("""
        SELECT c FROM Candidature c
        LEFT JOIN Employe e ON e.candidature.idCandidature = c.idCandidature
        WHERE e.idEmployer IS NULL
    """)
    List<Candidature> findCandidaturesSansEmploye();
}
