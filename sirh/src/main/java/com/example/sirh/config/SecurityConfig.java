package com.example.sirh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/public/**")
            )

            .authorizeHttpRequests(auth -> auth

                // Ressources publiques
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(
                    "/css/**", "/js/**", "/fonts/**", "/images/**"
                ).permitAll()
                .requestMatchers("/login").permitAll()

                // Administration
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Gestion des départements, postes et grilles
                .requestMatchers(
                    "/departements/**", "/postes/**", "/grilles/**"
                ).hasAnyRole("RH", "FINANCE", "DG")

                // Propositions d'embauche
                .requestMatchers("/propositions/**")
                    .hasAnyRole("RH", "FINANCE", "DG")

                // Offres d'emploi
                .requestMatchers("/offres/**").hasAnyRole("RH", "DG")

                // Candidatures
                .requestMatchers("/candidatures/**")
                    .hasAnyRole("RH", "MANAGER")

                // Employés
                .requestMatchers("/employes/**")
                    .hasAnyRole("MANAGER", "RH", "DG")

                // Création : MANAGER uniquement
                .requestMatchers(HttpMethod.GET, "/demandes/create")
                    .hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/demandes/create")
                    .hasRole("MANAGER")

                // Modification des brouillons : MANAGER uniquement
                .requestMatchers(HttpMethod.GET, "/demandes/update/**")
                    .hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/demandes/update")
                    .hasRole("MANAGER")

                // Validation / refus : selon le rôle et l'étape
                .requestMatchers(HttpMethod.POST, "/demandes/update/*")
                    .hasAnyRole("RH", "FINANCE", "DG")

                // Consultation des demandes
                .requestMatchers(HttpMethod.GET, "/demandes")
                    .hasAnyRole("MANAGER", "RH")
                .requestMatchers(HttpMethod.GET, "/demandes/*")
                    .hasAnyRole("MANAGER", "RH", "FINANCE", "DG")

                // Page de validation
                .requestMatchers("/demandes/validations")
                    .hasAnyRole("FINANCE", "DG", "RH")

                // Temps de travail
                .requestMatchers("/temps-travail/**")
                    .hasAnyRole("RH", "DG")

                // Dossiers
                .requestMatchers("/dossier/**")
                    .hasAnyRole("RH", "ADMIN", "DG")

                // Toutes les autres routes nécessitent une connexion
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("motDePasse")
                .defaultSuccessUrl("/utilisateur", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
