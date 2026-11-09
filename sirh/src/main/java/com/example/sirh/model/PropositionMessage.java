package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposition_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropositionMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Integer idMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition", nullable = false)
    private PropositionEmbauche proposition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidat")
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "salaire_propose")
    private BigDecimal salairePropose;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;
}