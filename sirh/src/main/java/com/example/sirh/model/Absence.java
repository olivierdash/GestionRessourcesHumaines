package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.sirh.model.enums.DemiJournee;
import com.example.sirh.model.enums.SourceAbsence;
import com.example.sirh.model.enums.StatutAbsence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "absence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_absence")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeConge typeConge;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande_conge", unique = true)
    private DemandeConge demandeConge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SourceAbsence source;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "demi_jour_debut", length = 10)
    private DemiJournee demiJourDebut;

    @Enumerated(EnumType.STRING)
    @Column(name = "demi_jour_fin", length = 10)
    private DemiJournee demiJourFin;

    @Column(name = "nombre_jours", nullable = false, precision = 7, scale = 2)
    private BigDecimal nombreJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAbsence statut;

    @Column(length = 500)
    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_createur")
    private Utilisateur createur;

    @Column(name = "date_limite_justificatif")
    private LocalDate dateLimiteJustificatif;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
