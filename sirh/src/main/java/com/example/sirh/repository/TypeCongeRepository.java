package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.TypeConge;
import com.example.sirh.model.enums.CategorieTypeConge;

public interface TypeCongeRepository extends JpaRepository<TypeConge, Integer> {

    List<TypeConge> findAllByOrderByOrdreAffichageAscLibelleAsc();

    List<TypeConge> findByActifTrueOrderByOrdreAffichageAscLibelleAsc();

    List<TypeConge> findByCategorieAndActifTrueOrderByOrdreAffichageAsc(CategorieTypeConge categorie);

    List<TypeConge> findByCategorieAndConsommeSoldeTrueAndActifTrueOrderByOrdreAffichageAsc(
            CategorieTypeConge categorie);

    Optional<TypeConge> findByCodeIgnoreCase(String code);
}
