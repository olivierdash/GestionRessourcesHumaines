package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidat")
    private Integer idCandidat;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;
}