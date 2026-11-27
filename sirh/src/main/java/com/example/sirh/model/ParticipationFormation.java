package com.example.sirh.model;

import java.math.BigDecimal;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participation_formation",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_participation_formation_employe",
                columnNames = {"id_formation", "id_employer"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participation")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_formation", nullable = false)
    private Formation formation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String statut = "INSCRIT";

    @Column(name = "taux_presence", precision = 5, scale = 2)
    private BigDecimal tauxPresence;

    @Column(name = "note_finale", precision = 5, scale = 2)
    private BigDecimal noteFinale;

    @Column(name = "certificat_obtenu", nullable = false)
    @Builder.Default
    private Boolean certificatObtenu = false;

    @Column(length = 500)
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_participation", nullable = false, updatable = false)
    private LocalDateTime dateParticipation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
