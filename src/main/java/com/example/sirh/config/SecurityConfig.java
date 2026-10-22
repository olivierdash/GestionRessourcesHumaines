package com.example.sirh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login").permitAll() // Autorise l'accès à la page
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/utilisateur/login") // <-- Indique à Spring d'utiliser TA page
                .loginProcessingUrl("/utilisateur/login") // L'URL vers laquelle le formulaire POST
                .defaultSuccessUrl("/utilisateur/dashboard", true) // Où aller après connexion
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/utilisateur/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}