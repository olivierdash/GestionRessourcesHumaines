package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;
    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;
}