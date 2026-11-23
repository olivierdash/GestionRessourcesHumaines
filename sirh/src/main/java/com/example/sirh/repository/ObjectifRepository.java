package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Objectif;

@Repository
public interface ObjectifRepository extends JpaRepository<Objectif, Integer> {
    List<Objectif> findByEmployeIdEmployer(Integer idEmployer);
}
