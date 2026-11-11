package com.example.sirh.repository;

import com.example.sirh.model.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PointageRepository extends JpaRepository<Pointage, Integer> {
    Optional<Pointage> findByEmployeIdEmployerAndDatePointage(Integer employeId, LocalDate datePointage);

    List<Pointage> findByDatePointageBetweenOrderByDatePointageDesc(LocalDate dateDebut, LocalDate dateFin);

    List<Pointage> findByEmployeIdEmployerAndDatePointageBetweenOrderByDatePointageDesc(
            Integer employeId, LocalDate dateDebut, LocalDate dateFin);
}
