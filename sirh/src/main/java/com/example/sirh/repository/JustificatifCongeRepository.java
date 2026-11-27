package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.JustificatifConge;
import com.example.sirh.model.enums.StatutJustificatif;

public interface JustificatifCongeRepository extends JpaRepository<JustificatifConge, Integer> {

    List<JustificatifConge> findByDemandeCongeIdOrderByDateDepotDesc(Integer demandeCongeId);

    List<JustificatifConge> findByAbsenceIdOrderByDateDepotDesc(Integer absenceId);

    boolean existsByDemandeCongeIdAndStatut(Integer demandeCongeId, StatutJustificatif statut);

    boolean existsByAbsenceIdAndStatut(Integer absenceId, StatutJustificatif statut);

    long countByDemandeCongeIdAndStatut(Integer demandeCongeId, StatutJustificatif statut);
}
