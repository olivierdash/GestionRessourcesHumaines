package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.StatutDemandeRecrutement;

@Repository 
public interface StatutDemandeRecrutementRepository extends JpaRepository<StatutDemandeRecrutement,Integer> {
}
