package com.example.sirh.repository;

import com.example.sirh.model.ModeleTacheOnboarding;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeleTacheOnboardingRepository extends JpaRepository<ModeleTacheOnboarding, Integer> {

    @EntityGraph(attributePaths = {"roleResponsable", "typeDocumentRequis"})
    List<ModeleTacheOnboarding> findByModele_IdOrderByOrdreAsc(Integer idModele);

    Optional<ModeleTacheOnboarding> findByIdAndModele_Id(Integer id, Integer idModele);

    long countByModele_Id(Integer idModele);
}
