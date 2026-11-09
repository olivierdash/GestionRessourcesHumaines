package com.example.sirh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sirh.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    
}
