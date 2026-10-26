package com.example.sirh.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;

@Service  
public class UtilisateurService {
    private UtilisateurRepository utilisateurRepository;
    public UtilisateurService(UtilisateurRepository utilisateurRepository){
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur getById(Integer id){
        return  utilisateurRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Manager introuvable"));
    }
}
