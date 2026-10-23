package com.example.sirh.service;


import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // On cherche l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // On retourne l'utilisateur avec son mot de passe EN CLAIR
        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse()) 
                .roles(utilisateur.getRole().getLibelle()) // ex: "MANAGER", "RH"
                .build();
    }
}
