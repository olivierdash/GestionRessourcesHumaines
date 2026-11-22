package com.example.sirh.repository;

import com.example.sirh.model.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LieuRepository extends JpaRepository<Lieu, Integer> {

    List<Lieu> findAllByOrderByLibelleAsc();

    Optional<Lieu> findByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCaseAndIdLieuNot(String libelle, Integer idLieu);
}