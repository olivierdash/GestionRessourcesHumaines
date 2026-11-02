package com.example.sirh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Ressources publiques
                        .requestMatchers("/css/**", "/js/**", "/fonts/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/utilisateur/login").permitAll()

                        // Règles spécifiques (Le PDF)
                        // Seul le RH et le DG peuvent gérer les référentiels (Départements, Postes,
                        // Grilles)
                        .requestMatchers("/departements/**", "/postes/**", "/grilles/**").hasAnyRole("RH", "DG")

                        // Seuls RH et Finance peuvent voir la page de validation des propositions (Bloc
                        // 3)
                        .requestMatchers("/propositions/**").hasAnyRole("RH", "FINANCE", "DG")

                        // Tout le reste (Demandes, Employés, Dashboard) nécessite d'être connecté
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/utilisateur/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // À remplacer par BCrypt en production
    }
}
