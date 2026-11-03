package com.example.sirh.service;

import com.example.sirh.model.Candidat;
import com.example.sirh.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;

    public Candidat getCandidatById(Integer id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable"));
    }

    public Candidat findByEmail(String email) {
        return candidatRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Candidat introuvable avec l'email : " + email));
    }

    public Candidat save(Candidat candidat) {
        return candidatRepository.save(candidat);
    }

    @Transactional
    public Candidat register(String prenom, String nom, String email, String motDePasseClair) {
        if (prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le prénom est requis.");
        }
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est requis.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est requis.");
        }
        if (motDePasseClair == null || motDePasseClair.length() < 8) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères.");
        }

        String emailNorm = email.trim().toLowerCase();
        if (candidatRepository.findByEmail(emailNorm).isPresent()) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }

        Candidat candidat = Candidat.builder()
                .prenom(prenom.trim())
                .nom(nom.trim())
                .email(emailNorm)
                .motDePasse(new BCryptPasswordEncoder().encode(motDePasseClair))
                .build();

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
}