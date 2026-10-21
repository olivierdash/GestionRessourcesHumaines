package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation")
    private Integer idEvaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluateur", nullable = false)
    private Utilisateur evaluateur;

    @Column(name = "type_evaluation", nullable = false, length = 50)
    private String typeEvaluation;

    @Column(name = "evaluation", columnDefinition = "TEXT")
    private String evaluation;

    @Column(name = "note", precision = 5, scale = 2)
    private BigDecimal note;

    @CreationTimestamp
    @Column(name = "date_evaluation", updatable = false)
    private LocalDateTime dateEvaluation;
}