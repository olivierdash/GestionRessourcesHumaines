package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "candidat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Candidat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidat")
    private Integer idCandidat;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenom;
    private String telephone;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "cv_url")
    private String cvUrl;
}