package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "evaluation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation")
    private Integer idEvaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluateur", nullable = false)
    private Utilisateur evaluateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_evaluation", nullable = false)
    private TypeEvaluation typeEvaluation;

    @Column(columnDefinition = "TEXT")
    private String evaluation;
    
    private BigDecimal note;
    
    @Column(name = "date_evaluation", insertable = false, updatable = false)
    private LocalDateTime dateEvaluation;
}   