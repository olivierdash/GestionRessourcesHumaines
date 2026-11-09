package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "horaire_travail")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false, length = 12)
    private DayOfWeek jourSemaine;

    @Column(name = "heure_debut")
    private LocalTime heureDebut;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    @Column(name = "pause_minutes", nullable = false)
    private Integer pauseMinutes = 0;
}
