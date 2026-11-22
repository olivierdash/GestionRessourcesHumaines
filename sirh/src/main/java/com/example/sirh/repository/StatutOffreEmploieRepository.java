package com.example.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.StatutOffreEmploi;

@Repository 
public interface StatutOffreEmploieRepository extends JpaRepository<StatutOffreEmploi,Integer> {
    public Optional<StatutOffreEmploi> findByLibelle(String libelle);
}
