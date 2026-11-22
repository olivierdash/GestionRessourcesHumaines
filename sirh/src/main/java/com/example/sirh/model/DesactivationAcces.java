package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "desactivation_acces")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesactivationAcces {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desactivation")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rupture", nullable = false)
    private RuptureContrat rupture;

    @Column(name = "type_acces", nullable = false, length = 30)
    private String typeAcces;

    @Column(length = 150)
    private String identifiant;

    @Column(name = "date_desactivation")
    private LocalDate dateDesactivation;

    @Column(nullable = false, length = 15)
    private String statut = "A_FAIRE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectue_par")
    private Utilisateur effectuePar;

    @Column(length = 500)
    private String commentaire;
}