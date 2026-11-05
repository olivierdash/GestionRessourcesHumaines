package com.example.sirh.service;

import com.example.sirh.dto.LoginChangeDTO;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.RoleRepository;
import com.example.sirh.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    @Value("${app.admin.secret-code:}")
    private String adminSecretCode;

    public Utilisateur getById(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findFirstByRoleLibelle(String roleLibelle) {
        return utilisateurRepository.findFirstByRoleLibelle(roleLibelle)
                .orElseThrow(() -> new NoSuchElementException("Aucun utilisateur trouvé avec le rôle : " + roleLibelle));
    }

    public List<Utilisateur> getByRolesList(List<String> roleLibelles) {
        return utilisateurRepository.findByRole_LibelleIn(roleLibelles);
    }

    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable avec l'email : " + email));
    }

    public Utilisateur getFirstUtilisateur() {
        return utilisateurRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur en base pour évaluer."));
    }

    @Transactional
    public Utilisateur createUtilisateur(Utilisateur utilisateur, String codeSecret) {
        Map<String, String> errors = new LinkedHashMap<>();

        // 1. Validation des champs de base
        if (utilisateur.getNom() == null || utilisateur.getNom().isBlank()) {
            errors.put("nom", "Le nom est obligatoire.");
        }
        if (utilisateur.getEmail() == null || utilisateur.getEmail().isBlank()) {
            errors.put("email", "L'email est obligatoire.");
        } else if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            errors.put("email", "Un utilisateur avec cet email existe déjà.");
        }
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().length() < 8) {
            errors.put("motDePasse", "Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (utilisateur.getRole() == null || utilisateur.getRole().getIdRole() == null) {
            errors.put("role.idRole", "Le rôle est obligatoire.");
        }

        // 2. Validation du code secret si rôle = ADMIN
        if (utilisateur.getRole() != null && utilisateur.getRole().getIdRole() != null) {
            if (estRoleAdmin(utilisateur.getRole().getIdRole())) {
                if (adminSecretCode == null || adminSecretCode.isBlank()) {
                    log.error("Configuration manquante : app.admin.secret-code non défini.");
                    errors.put("codeSecret", "Configuration manquante côté serveur.");
                } else if (codeSecret == null || !codeSecret.equals(adminSecretCode)) {
                    log.warn("Code secret invalide - action: création admin, email cible: {}", utilisateur.getEmail());
                    errors.put("codeSecret", "Code secret invalide.");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        // 3. Hash et sauvegarde
        utilisateur.setMotDePasse(new BCryptPasswordEncoder().encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void changerMotDePasse(LoginChangeDTO request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (request.getNouveauMotDePasse() == null || request.getNouveauMotDePasse().isBlank()) {
            errors.put("nouveauMotDePasse", "Le mot de passe est obligatoire.");
        } else if (request.getNouveauMotDePasse().length() < 8) {
            errors.put("nouveauMotDePasse", "Le mot de passe doit contenir au moins 8 caractères.");
        }

        if (request.getConfirmationMotDePasse() == null || request.getConfirmationMotDePasse().isBlank()) {
            errors.put("confirmationMotDePasse", "La confirmation est obligatoire.");
        } else if (!request.getConfirmationMotDePasse().equals(request.getNouveauMotDePasse())) {
            errors.put("confirmationMotDePasse", "Les mots de passe ne correspondent pas.");
        }

        // 2. Validation du code secret si self
        boolean isSelf = request.getIdAdminConnecte() != null
                && request.getIdAdminConnecte().equals(request.getIdUtilisateur());

        if (isSelf) {
            if (adminSecretCode == null || adminSecretCode.isBlank()) {
                log.error("Configuration manquante : app.admin.secret-code non défini.");
                errors.put("codeSecret", "Configuration manquante côté serveur.");
            } else if (request.getCodeSecret() == null || !request.getCodeSecret().equals(adminSecretCode)) {
                log.warn("Code secret invalide - action: modification mot de passe self, utilisateur id: {}", request.getIdUtilisateur());
                errors.put("codeSecret", "Code secret invalide.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        // 3. Hash et sauvegarde
        Utilisateur utilisateur = getById(request.getIdUtilisateur());
        utilisateur.setMotDePasse(new BCryptPasswordEncoder().encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    private boolean estRoleAdmin(Integer idRole) {
        if (idRole == null) return false;
        return roleRepository.findById(idRole)
                .map(r -> "ADMIN".equalsIgnoreCase(r.getLibelle()))
                .orElse(false);
    }
}