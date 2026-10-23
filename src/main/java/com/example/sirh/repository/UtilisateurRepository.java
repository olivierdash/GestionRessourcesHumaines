package com.example.sirh.repository;

import com.example.sirh.model.Utilisateur;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findFirstByRoleLibelle(String libelle);

    Optional<Utilisateur> findByEmail(String email);
}