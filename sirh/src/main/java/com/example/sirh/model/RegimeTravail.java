package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regime_travail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegimeTravail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regime_travail")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Column(name = "heures_hebdomadaires", nullable = false)
    private Integer heuresHebdomadaires;

    @Column(name = "jours_hebdomadaires", nullable = false)
    private Integer joursHebdomadaires;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "regimeTravail", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("jourSemaine ASC")
    @Builder.Default
    private List<HoraireTravail> horaires = new ArrayList<>();
}
