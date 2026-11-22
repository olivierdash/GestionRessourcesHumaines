package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.MaterielEmploye;

/**
 * @author moi
 * pour le matos donné pour l'employer
 * MaterielEmployeRepository
 */
@Repository 
public interface MaterielEmployeRepository extends JpaRepository <MaterielEmploye, Integer> {
    // ==========================================
    // ESPACE EMPLOYÉ : Consulter son matériel à rendre
    // ==========================================
    List<MaterielEmploye> findByEmployeIdEmployer(Integer idEmployer);
    
    // Voir uniquement ce qu'il lui reste à restituer (restitution non datée)
    List<MaterielEmploye> findByEmployeIdEmployerAndDateRestitutionIsNull(Integer idEmployer);
    long countByEmployeIdEmployerAndDateRestitutionIsNull(Integer idEmployer);
}
