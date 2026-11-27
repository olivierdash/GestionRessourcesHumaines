package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.EmployeCompetence;

@Repository
public interface EmployeCompetenceRepository extends JpaRepository<EmployeCompetence, Integer> {
    List<EmployeCompetence> findByEmployeIdEmployer(Integer idEmployer);

    List<EmployeCompetence> findByCompetenceId(Integer idCompetence);
}
