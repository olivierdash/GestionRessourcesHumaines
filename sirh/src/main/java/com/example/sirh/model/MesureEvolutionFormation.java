package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mesure_evolution_formation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesureEvolutionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesure_evolution")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_participation", nullable = false)
    private ParticipationFormation participation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competence", nullable = false)
    private Competence competence;

    @Column(name = "niveau_avant", nullable = false, precision = 5, scale = 2)
    private BigDecimal niveauAvant;

    @Column(name = "niveau_apres", precision = 5, scale = 2)
    private BigDecimal niveauApres;

    @Column(name = "niveau_attendu", precision = 5, scale = 2)
    private BigDecimal niveauAttendu;

    @Column(precision = 5, scale = 2)
    private BigDecimal progression;

    @Column(name = "taux_atteinte", precision = 5, scale = 2)
    private BigDecimal tauxAtteinte;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String mesure = "NIVEAU_COMPETENCE";

    @Column(name = "date_mesure_avant")
    private LocalDate dateMesureAvant;

    @Column(name = "date_mesure_apres")
    private LocalDate dateMesureApres;

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluateur")
    private Utilisateur evaluateur;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
