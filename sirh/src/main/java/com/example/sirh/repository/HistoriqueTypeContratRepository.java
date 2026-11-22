package com.example.sirh.repository;

import com.example.sirh.model.HistoriqueTypeContrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueTypeContratRepository extends JpaRepository<HistoriqueTypeContrat, Integer> {
    List<HistoriqueTypeContrat> findByTypeContratIdOrderByDateModificationDesc(Integer idTypeContrat);
}