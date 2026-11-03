package com.example.sirh.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public Utilisateur getById(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> getManagers() {
        return utilisateurRepository.findByRole_LibelleIgnoreCase("manager");
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException(
                        "Utilisateur introuvable avec l'email : " + email));
    }

    /**
     * Utilisé par OnboardingService.creerTache pour trouver le responsable
     * d'une tâche à partir du libellé de son rôle.
     */
    @Transactional(readOnly = true)
    public Utilisateur findFirstByRoleLibelle(String libelle) {
        return utilisateurRepository.findFirstByRoleLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun utilisateur avec le rôle " + libelle));
    }
}