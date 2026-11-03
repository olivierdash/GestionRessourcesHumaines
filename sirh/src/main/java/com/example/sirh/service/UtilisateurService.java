package com.example.sirh.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleService roleService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, RoleService roleService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleService = roleService;
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

    @Transactional
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        // 1. Vérification de l'unicité de l'email
        try {
            getByEmail(utilisateur.getEmail());
            throw new IllegalArgumentException("Erreur : cet email est déjà utilisé.");
        } catch (NoSuchElementException e) {
            // L'email n'existe pas, on peut continuer
        }

        // 2. Vérification de la longueur du mot de passe
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }

        // 3. Récupération du rôle managé depuis la base
        Role role = roleService.getRoleById(utilisateur.getRole().getIdRole());

        // 4. Hashage du mot de passe
        utilisateur.setMotDePasse(new BCryptPasswordEncoder().encode(utilisateur.getMotDePasse()));

        // 5. Assignation du rôle et sauvegarde
        utilisateur.setRole(role);
        return utilisateurRepository.save(utilisateur);
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