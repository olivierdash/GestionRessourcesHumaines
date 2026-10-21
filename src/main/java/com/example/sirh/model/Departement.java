package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Integer idDepartement;

    @Column(name = "titre", nullable = false)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_manager")
    private Utilisateur manager;
}