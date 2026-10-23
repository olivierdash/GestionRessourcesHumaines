package com.example.sirh.repository;

import com.example.sirh.model.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    List<Contrat> findByEmployeIdEmployerOrderByDateDebutDesc(Integer idEmploye);

    Optional<Contrat> findFirstByEmployeIdEmployerAndStatutLibelle(Integer idEmploye, String libelle);

    @Query("""
                SELECT COUNT(c) > 0 FROM Contrat c
                WHERE c.employe.idEmployer = :idEmploye
                  AND (c.dateFin IS NULL OR c.dateFin >= :debut)
                  AND (:fin IS NULL OR c.dateDebut <= :fin)
            """)
    boolean existeChevauchement(@Param("idEmploye") Integer idEmploye,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin);
}