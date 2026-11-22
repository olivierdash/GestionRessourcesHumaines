package com.example.sirh.service;

import com.example.sirh.dto.CandidatLoginChangeDTO;
import com.example.sirh.dto.LoginChangeDTO;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Candidat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.CandidatRepository;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final CandidatRepository candidatRepository;
    private final HistoriqueCandidatIdentifiantsService historiqueCandidatIdentifiantsService;

    public Candidat getCandidatById(Integer id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable"));
    }

    public Optional<Candidat> findByEmail(String email) {
        return candidatRepository.findByEmail(email);
    }

    public Candidat save(Candidat candidat) {
        return candidatRepository.save(candidat);
    }

    @Transactional
    public Candidat register(Candidat candidat) {
        Map<String, String> errors = new LinkedHashMap<>();

        String prenom = candidat.getPrenom(), nom = candidat.getNom(), email = candidat.getEmail(),
                password = candidat.getMotDePasse();

        if (prenom == null || prenom.isBlank()) {
            errors.put("prenom", "Le prénom est requis.");
        }
        if (nom == null || nom.isBlank()) {
            errors.put("nom", "Le nom est requis.");
        }
        if (email == null || email.isBlank()) {
            errors.put("email", "L'email est requis.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            errors.put("motDePasse",
                    "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
        }

        String emailNorm = null;
        if (email != null && !email.isBlank() && !errors.containsKey("email")) {
            emailNorm = email.trim().toLowerCase();
            if (findByEmail(emailNorm).isPresent()) {
                errors.put("email", "Un compte existe déjà avec cet email.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }
        
        candidat.setEmail(emailNorm);
        candidat.setMotDePasse(new BCryptPasswordEncoder().encode(password));
        return candidatRepository.save(candidat);
    }

    @Transactional(readOnly = true)
    public Candidat authenticate(String email, String motDePasseClair) {
        if (email == null || motDePasseClair == null) {
            throw new IllegalArgumentException("Email et mot de passe requis.");
        }

        Candidat candidat = candidatRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects."));

        if (candidat.getMotDePasse() == null
                || !new BCryptPasswordEncoder().matches(motDePasseClair, candidat.getMotDePasse())) {
            throw new IllegalArgumentException("Identifiants incorrects.");
        }

        return candidat;
    }

    @Transactional
    public void resetMotDePasse(CandidatLoginChangeDTO request) {
        Map<String, String> errors = new LinkedHashMap<>();

        String email = request.getEmail();
        String nouveau = request.getNouveauMotDePasse();
        String confirmation = request.getConfirmationMotDePasse();
        
        Candidat candidat = null;

        if (email == null || email.isBlank()) {
            errors.put("email", "L'email est obligatoire.");
        } else {
            candidat = candidatRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
            if (candidat == null) {
                errors.put("email", "Aucun compte n'existe avec cet email.");
            }
        }

        if (nouveau == null || nouveau.isBlank()) {
            errors.put("nouveauMotDePasse", "Le nouveau mot de passe est obligatoire.");
        } else if (nouveau.length() < MIN_PASSWORD_LENGTH) {
            errors.put("nouveauMotDePasse",
                    "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
        }

        if (confirmation == null || confirmation.isBlank()) {
            errors.put("confirmationMotDePasse", "La confirmation est obligatoire.");
        } else if (!confirmation.equals(nouveau)) {
            errors.put("confirmationMotDePasse", "Les mots de passe ne correspondent pas.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        candidat.setMotDePasse(new BCryptPasswordEncoder().encode(nouveau));
        candidatRepository.save(candidat);

        historiqueCandidatIdentifiantsService.enregistrer(candidat);
    }
}