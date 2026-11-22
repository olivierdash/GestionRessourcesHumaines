package com.example.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByLibelleIgnoreCase(String libelle);
}
