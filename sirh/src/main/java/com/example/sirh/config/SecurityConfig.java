package com.example.sirh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Mots de passe en clair dans la base (projet de cours uniquement)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API publique (site carrière) : pas de session, pas de token CSRF.
                // Sans cette ligne, POST /api/public/jobs/{id}/apply renvoie 403.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/public/**"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/fonts/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/utilisateur/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/departements/**", "/postes/**", "/grilles/**").hasAnyRole("RH", "FINANCE", "DG")
                        .requestMatchers("/propositions/**").hasAnyRole("RH", "FINANCE", "DG")
                        .requestMatchers("/offres/**").hasAnyRole("RH", "DG")
                        .requestMatchers("/candidatures/**").hasAnyRole("RH", "MANAGER")
                        .requestMatchers("/employes/**").hasAnyRole("MANAGER", "RH", "DG")
                        .requestMatchers("/demandes/validations").hasAnyRole("FINANCE", "DG", "RH")

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
}