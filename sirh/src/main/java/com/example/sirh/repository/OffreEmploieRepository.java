package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.OffreEmploi;

@Repository
public interface OffreEmploieRepository extends JpaRepository<OffreEmploi, Integer> {
    List<OffreEmploi> findAllByOrderByDatePublicationDesc();

    List<OffreEmploi> findByStatutId(Integer idStatut);

    long countByStatutLibelle(String libelle);

    long countByStatutLibelleAndDemandeRecrutementManagerIdUtilisateur(String libelle, Integer idManager);
}