package com.example.sirh.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "objectif_critere_evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectifCritereEvaluation {

    @EmbeddedId
    private ObjectifCritereEvaluationId id;

    @MapsId("idObjectifEvaluation")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_objectif_evaluation", nullable = false)
    private Objectif objectif;

    @MapsId("idCritereEvaluation")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_critere_evaluation", nullable = false)
    private CritereEvaluation critere;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal poids = BigDecimal.valueOf(100);
}
