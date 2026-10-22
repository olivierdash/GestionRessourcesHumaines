package com.example.sirh.repository;

import com.example.sirh.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    long countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(Integer idDepartement);

    java.util.Optional<Employe> findByMatricule(String matricule);
}