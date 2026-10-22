package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Candidature;

@Repository 
public interface CandidatureRepository extends JpaRepository<Candidature,Integer> { 
    List<Candidature> findByOffreIdOffre(Integer id_offre);
    List<Candidature> findByCandidatIdCandidatOrderByDateDepotCandidatureDesc(Integer id_candidat);
}
