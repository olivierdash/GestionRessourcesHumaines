package com.example.sirh.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ObjectifCritereEvaluationId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_objectif_evaluation")
    private Integer idObjectifEvaluation;

    @Column(name = "id_critere_evaluation")
    private Integer idCritereEvaluation;
}
