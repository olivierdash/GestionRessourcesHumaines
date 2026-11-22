package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import java.time.LocalDate;

/**
 * @author moi
 * Module 11
 * demande d'explication 
 */

@Entity
@Table(name = "demande_explication")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeExplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Integer idDemande;

    // UNIQUE côté SQL → OneToOne
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incident", nullable = false, unique = true)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_envoyeur", nullable = false)
    private Utilisateur envoyeur;

    @Column(name = "date_envoi", nullable = false)
    private LocalDate dateEnvoi;

    @Column(name = "date_limite_reponse", nullable = false)
    private LocalDate dateLimiteReponse;

    @Column(nullable = false, length = 500)
    private String motif;

    @OneToOne(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReponseEtDecision reponseEtDecision;
}