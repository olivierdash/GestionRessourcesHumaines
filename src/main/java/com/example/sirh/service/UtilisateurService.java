package com.example.sirh.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;

@Service
public class UtilisateurService {
    private UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur getById(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Manager introuvable"));
    }

    public List<Utilisateur> getManagers() {
        return utilisateurRepository.findByRole_LibelleIgnoreCase("manager");
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable avec l'email : " + email));
    }

    public Utilisateur getFirstUtilisateur() {
        return utilisateurRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur en base pour évaluer."));
    }
}
