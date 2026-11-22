package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.sirh.model.enums.CodeVariablePaie;
import com.example.sirh.model.enums.StatutVariablePaie;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variable_paie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariablePaie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variable_paie")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande_conge")
    private DemandeConge demandeConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_absence")
    private Absence absence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CodeVariablePaie code;

    @Column(length = 255)
    private String libelle;

    @Column(nullable = false)
    private LocalDate periode;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String unite = "JOUR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutVariablePaie statut = StatutVariablePaie.A_CONSOMMER;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_consommation")
    private LocalDateTime dateConsommation;

    @Column(length = 500)
    private String commentaire;
}
