package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockSirhUserSecurityContextFactory implements WithSecurityContextFactory<WithMockSirhUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockSirhUser annotation) {
        Role role = Role.builder().libelle(annotation.role()).build();
        Utilisateur utilisateur = Utilisateur.builder()
                .nom("Utilisateur test")
                .email("test@example.invalid")
                .motDePasse("test")
                .role(role)
                .build();
        CustomUserDetails principal = new CustomUserDetails(utilisateur);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
