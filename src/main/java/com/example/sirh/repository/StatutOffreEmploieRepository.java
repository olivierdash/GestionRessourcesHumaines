package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.StatutOffreEmploi;

@Repository 
public interface StatutOffreEmploieRepository extends JpaRepository<StatutOffreEmploi,Integer> {
}
