package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "palier_heure_sup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PalierHeureSup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_palier")
    private Integer idPalier;
    @Column(name = "ordre", nullable = false)
    private Integer ordre;
    @Column(name = "heures_tranche")
    private BigDecimal heuresTranche;
    @Column(name = "coefficient", nullable = false)
    private BigDecimal coefficient;
    @Column(name = "actif", nullable = false)
    private Boolean actif;
}