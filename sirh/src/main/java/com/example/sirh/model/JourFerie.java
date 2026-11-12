package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jour_ferie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourFerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jour_ferie")
    private Integer id;

    @Column(nullable = false)
    private Integer jour;

    @Column(nullable = false)
    private Integer mois;

    @Column(name = "annee")
    private Integer annee;

    @Column(nullable = false, length = 25)
    private String libelle;
}