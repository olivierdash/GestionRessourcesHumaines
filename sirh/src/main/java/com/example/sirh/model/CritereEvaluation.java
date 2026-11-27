package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "critere_evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CritereEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_critere_evaluation")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "echelle_min", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal echelleMin = BigDecimal.ONE;

    @Column(name = "echelle_max", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal echelleMax = BigDecimal.valueOf(5);

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal poids = BigDecimal.valueOf(100);

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
