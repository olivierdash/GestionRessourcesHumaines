package com.example.sirh.repository;

import com.example.sirh.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    long countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(Integer idDepartement);

    Optional<Employe> findByMatricule(String matricule);

    boolean existsByCandidatureIdCandidature(Integer idCandidature);

    Optional<Employe> findByCandidatureIdCandidature(Integer idCandidature);

    @Query(value = "select nextval('matricule_seq')", nativeQuery = true)
    Long prochainNumeroMatricule();
}