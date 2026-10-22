package com.example.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Poste;
import com.example.sirh.model.PosteMission;

@Repository 
public interface PosteMissionRepository extends JpaRepository<PosteMission,Integer> {
    List<PosteMission> findByPosteIdPoste(Integer id_poste);
}
