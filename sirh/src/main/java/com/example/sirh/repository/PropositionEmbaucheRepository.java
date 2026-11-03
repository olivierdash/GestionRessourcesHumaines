package com.example.sirh.repository;

import com.example.sirh.model.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Integer> {
    List<PropositionEmbauche> findAllByOrderByIdPropositionDesc();

    boolean existsByCandidatureIdCandidatureAndStatutLibelleIn(Integer idCandidature, Collection<String> libelles);
}