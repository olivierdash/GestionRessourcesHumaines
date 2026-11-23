package com.example.sirh.model;

import java.math.BigDecimal;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resultat_critere_evaluation",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_resultat_critere_objectif",
                columnNames = {"id_objectif_evaluation", "id_critere_evaluation"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultatCritereEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultat_critere")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_objectif_evaluation", nullable = false)
    private Objectif objectif;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_critere_evaluation", nullable = false)
    private CritereEvaluation critere;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluateur", nullable = false)
    private Utilisateur evaluateur;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal note;

    @Column(length = 500)
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_evaluation", nullable = false, updatable = false)
    private LocalDateTime dateEvaluation;
}
