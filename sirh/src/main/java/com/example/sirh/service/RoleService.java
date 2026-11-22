package com.example.sirh.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.Role;
import com.example.sirh.repository.RoleRepository;

@Repository 
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getRoles(){
        return roleRepository.findAll();
    }

    public Role getRoleById(Integer id) {
        return roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rôle introuvable : " + id));
    }
}
