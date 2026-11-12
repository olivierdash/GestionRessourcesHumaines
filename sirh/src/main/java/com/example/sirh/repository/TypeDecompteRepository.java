package com.example.sirh.repository;

import com.example.sirh.model.TypeDecompte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeDecompteRepository extends JpaRepository<TypeDecompte, Integer> {
    Optional<TypeDecompte> findByCode(String code);
}