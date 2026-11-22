package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.sirh.model.enums.DemiJournee;
import com.example.sirh.model.enums.StatutDemandeConge;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demande_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande_conge")
    private Integer id;

    @Column(unique = true, length = 30)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeConge typeConge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur_createur", nullable = false)
    private Utilisateur createur;

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

    @Column(name = "solde_requis", nullable = false)
    @Builder.Default
    private Boolean soldeRequis = true;

    @Column(length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutDemandeConge statut;

    @Column(name = "manager_requis", nullable = false)
    @Builder.Default
    private Boolean managerRequis = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_manager_attendu")
    private Utilisateur managerAttendu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur_manager")
    private Utilisateur validateurManager;

    @Column(name = "date_validation_manager")
    private LocalDateTime dateValidationManager;

    @Column(name = "commentaire_manager", length = 500)
    private String commentaireManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur_rh")
    private Utilisateur validateurRh;

    @Column(name = "date_validation_rh")
    private LocalDateTime dateValidationRh;

    @Column(name = "commentaire_rh", length = 500)
    private String commentaireRh;

    @Column(name = "date_limite_justificatif")
    private LocalDate dateLimiteJustificatif;

    @Column(name = "date_soumission")
    private LocalDateTime dateSoumission;

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
