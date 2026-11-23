package com.example.sirh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "ecart_competence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcartCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ecart_competence")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competence", nullable = false)
    private Competence competence;

    @Column(name = "niveau_attendu", nullable = false)
    private Integer niveauAttendu;

    @Column(name = "niveau_acquis", nullable = false)
    private Integer niveauAcquis;

    @Column(nullable = false)
    private Integer ecart;

    @Column(name = "niveau_ecart", nullable = false, length = 20)
    private String niveauEcart;

    @Column(name = "date_analyse", nullable = false)
    @Builder.Default
    private LocalDate dateAnalyse = LocalDate.now();

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_analyste")
    private Utilisateur analyste;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
