package com.example.sirh.repository;

import com.example.sirh.model.PropositionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropositionMessageRepository extends JpaRepository<PropositionMessage, Integer> {

    List<PropositionMessage> findByPropositionIdPropositionOrderByDateCreationAsc(Integer idProposition);

    Optional<PropositionMessage> findFirstByPropositionIdPropositionOrderByDateCreationDesc(Integer idProposition);

    @Query("""
            SELECT m FROM PropositionMessage m
            WHERE m.proposition.idProposition = :idProposition
              AND m.candidat IS NOT NULL
            ORDER BY m.dateCreation DESC
            """)
    List<PropositionMessage> findCandidatMessagesByProposition(@Param("idProposition") Integer idProposition);

    long countByPropositionIdPropositionAndCandidatIsNotNull(Integer idProposition);
}