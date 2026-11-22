package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.TypeContrat;

@Repository
public interface TypeContractRepository extends JpaRepository<TypeContrat,Integer> {
}
