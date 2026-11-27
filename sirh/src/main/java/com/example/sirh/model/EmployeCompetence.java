package com.example.sirh.model;

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
@Table(name = "competence_acquise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competence_acquise")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competence", nullable = false)
    private Competence competence;

    @Column(name = "niveau_acquis", nullable = false)
    private Integer niveauAcquis;

    @Column(name = "date_acquisition", nullable = false)
    private LocalDate dateAcquisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluateur")
    private Utilisateur evaluateur;

    @Column(name = "source_evaluation", length = 30)
    private String sourceEvaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluation")
    private Evaluation evaluation;

    @Column(length = 500)
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
