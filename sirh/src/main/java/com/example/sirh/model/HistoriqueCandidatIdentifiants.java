package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_candidat_identifiants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoriqueCandidatIdentifiants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @Column(name = "date_changement", insertable = false, updatable = false)
    private LocalDateTime dateChangement;

    @Column(name = "ancien_nom", length = 255)
    private String ancienNom;

    @Column(name = "ancien_prenom", length = 255)
    private String ancienPrenom;

    @Column(name = "ancien_email", length = 255)
    private String ancienEmail;
}