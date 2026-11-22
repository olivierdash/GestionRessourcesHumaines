package com.example.sirh.repository;

import com.example.sirh.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    
    // Pour la création
    boolean existsByTitreIgnoreCase(String titre);
    
    // Pour la modification (on vérifie que le titre n'est pas utilisé par un AUTRE département)
    boolean existsByTitreIgnoreCaseAndIdDepartementNot(String titre, Integer idDepartement);
}