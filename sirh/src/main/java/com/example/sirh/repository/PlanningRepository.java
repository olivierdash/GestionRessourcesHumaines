package com.example.sirh.repository;

import com.example.sirh.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanningRepository extends JpaRepository<Planning, Integer> {

    @Query("""
            SELECT DISTINCT p FROM Planning p
            LEFT JOIN FETCH p.regimeTravail
            LEFT JOIN FETCH p.affectations
            """)
    List<Planning> findAllWithRelations();
}