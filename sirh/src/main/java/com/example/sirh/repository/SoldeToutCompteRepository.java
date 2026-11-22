package com.example.sirh.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.SoldeToutCompte;

@Repository 
public interface SoldeToutCompteRepository extends JpaRepository<SoldeToutCompte,Integer> {
    // ==========================================
    // ESPACE EMPLOYÉ : Voir son reçu financier
    // ==========================================
    Optional<SoldeToutCompte> findByRuptureContratEmployeIdEmployer(Integer idEmployer);
    
    // ==========================================
    // ESPACE RH / POSTE HAUT PLACÉ
    // ==========================================
    Optional<SoldeToutCompte> findByRuptureContratIdRupture(Integer idRupture);
    
    // Voir tous les STC ayant un certain statut (ex: 'BROUILLON' à vérifier, ou 'VALIDE')
    List<SoldeToutCompte> findByStatut(String statut);
}
