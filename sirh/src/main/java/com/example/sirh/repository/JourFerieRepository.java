package com.example.sirh.repository;

import com.example.sirh.model.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JourFerieRepository extends JpaRepository<JourFerie, Integer> {

    @Query("""
            SELECT j FROM JourFerie j
            WHERE (j.annee IS NULL AND j.jour = :jour AND j.mois = :mois)
               OR (j.annee = :annee AND j.jour = :jour AND j.mois = :mois)
            """)
    List<JourFerie> findPourDate(@Param("jour") Integer jour,
                                 @Param("mois") Integer mois,
                                 @Param("annee") Integer annee);

    @Query("""
            SELECT j FROM JourFerie j
            WHERE j.annee IS NULL
               OR j.annee = :annee
            """)
    List<JourFerie> findPourAnnee(@Param("annee") Integer annee);
}