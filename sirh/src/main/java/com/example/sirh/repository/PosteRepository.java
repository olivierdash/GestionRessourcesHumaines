package com.example.sirh.repository;

import com.example.sirh.model.Poste;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Integer> {

    boolean existsByIntituleIgnoreCaseAndDepartement_IdDepartement(
            String intitule,
            Integer idDepartement);

    boolean existsByIntituleIgnoreCaseAndDepartement_IdDepartementAndIdPosteNot(
            String intitule,
            Integer idDepartement,
            Integer idPoste);
}