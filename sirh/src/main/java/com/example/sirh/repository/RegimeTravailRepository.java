package com.example.sirh.repository;

import com.example.sirh.model.RegimeTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegimeTravailRepository extends JpaRepository<RegimeTravail, Integer> {

    @Query("""
            SELECT DISTINCT r FROM RegimeTravail r
            LEFT JOIN FETCH r.typeDecompte
            LEFT JOIN FETCH r.horaires
            """)
    List<RegimeTravail> findAllWithRelations();

    Optional<RegimeTravail> findByCode(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Integer id);

    boolean existsByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCaseAndIdNot(String libelle, Integer id);
}