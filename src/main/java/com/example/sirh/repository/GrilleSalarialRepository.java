package com.example.sirh.repository;

import com.example.sirh.model.GrilleSalarial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrilleSalarialRepository extends JpaRepository<GrilleSalarial, Integer> {
}