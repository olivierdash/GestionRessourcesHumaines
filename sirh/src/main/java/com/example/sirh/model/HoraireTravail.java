package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "horaire_travail",
        uniqueConstraints = @UniqueConstraint(name = "uq_horaire_regime_dayweek",
                columnNames = {"id_regime_travail", "dayweek"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoraireTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horaire_travail")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_regime_travail", nullable = false)
    private RegimeTravail regimeTravail;

    @Column(nullable = false)
    private Integer dayweek;

    @Column(name = "heure_debut")
    private LocalTime heureDebut;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    @Column(name = "pause_minutes", nullable = false)
    @Builder.Default
    private Integer pauseMinutes = 0;
}