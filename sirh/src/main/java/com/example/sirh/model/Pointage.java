package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "pointage",
        uniqueConstraints = @UniqueConstraint(name = "uq_pointage_employe_date",
                columnNames = {"id_employer", "date_pointage"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pointage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pointage")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @Column(name = "date_pointage", nullable = false)
    private LocalDate datePointage;

    @Column(name = "heure_entree")
    private LocalTime heureEntree;

    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

    @Column(name = "minutes_travaillees", nullable = false)
    private Integer minutesTravaillees = 0;

    @Column(name = "minutes_pause", nullable = false)
    private Integer minutesPause = 0;

    @Column(name = "minutes_retard", nullable = false)
    private Integer minutesRetard = 0;

    @Column(name = "minutes_supplementaires", nullable = false)
    private Integer minutesSupplementaires = 0;

    @Column(length = 500)
    private String commentaire;
}
