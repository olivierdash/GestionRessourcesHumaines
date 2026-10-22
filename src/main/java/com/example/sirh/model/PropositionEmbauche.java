package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "proposition_embauche")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropositionEmbauche {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposition")
    private Integer idProposition;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;
    @Column(name = "salaire_propose", nullable = false)
    private BigDecimal salairePropose;
    @Column(name = "salaire_final")
    private BigDecimal salaireFinal;
    @Column(name = "date_proposition", insertable = false, updatable = false)
    private LocalDateTime dateProposition;
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_proposition", nullable = false)
    private StatutProposition statut;
    @Column(name = "date_decision")
    private LocalDateTime dateDecision;
}