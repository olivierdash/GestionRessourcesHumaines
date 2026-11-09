package com.example.sirh.service;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j; // <-- Ajout de l'import Lombok


@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
        log.info("=== DEBUG ACCÈS DASHBOARD ===");
        log.info("Utilisateur connecté (Email) : {}", utilisateur.getEmail());
        log.info("Rôle en base de données : {}", utilisateur.getRole().getLibelle());
        log.info("=============================");
        // On retourne notre propre objet qui contient toutes les infos
        return new CustomUserDetails(utilisateur);
    }
}
