package com.example.sirh.config;

import com.example.sirh.model.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<Utilisateur> result = em.createQuery(
                "select u from Utilisateur u join fetch u.role where u.email = :email",
                Utilisateur.class)
            .setParameter("email", email)
            .getResultList();

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur introuvable : " + email);
        }
        Utilisateur u = result.get(0);

        return User.withUsername(u.getEmail())
                .password(u.getMotDePasse())
                .roles(u.getRole().getLibelle())
                .build();
    }

    // Mots de passe en clair (projet de cours uniquement)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
