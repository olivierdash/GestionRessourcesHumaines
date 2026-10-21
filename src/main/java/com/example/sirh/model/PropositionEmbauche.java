package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposition_embauche")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropositionEmbauche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposition")
    private Integer idProposition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @Column(name = "salaire_propose", nullable = false, precision = 10, scale = 2)
    private BigDecimal salairePropose;

    @Column(name = "salaire_final", precision = 10, scale = 2)
    private BigDecimal salaireFinal;

    @CreationTimestamp
    @Column(name = "date_proposition", updatable = false)
    private LocalDateTime dateProposition;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "statut_proposition", nullable = false, length = 50)
    private String statutProposition;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;
}