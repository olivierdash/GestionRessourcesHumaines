package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author moi
 * Module 12
 * RuptureContrat
 */

@Entity
@Table(name = "rupture_contrat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RuptureContrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rupture")
    private Integer idRupture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    // UNIQUE → OneToOne
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contract", nullable = false, unique = true)
    private Contrat contrat;

    @Column(name = "motif_rupture", nullable = false, length = 30)
    private String motifRupture;

    @Column(name = "statut_rupture", nullable = false, length = 25)
    private String statutRupture = "INITIEE";

    @Column(name = "date_notification", nullable = false)
    private LocalDate dateNotification;

    @Column(name = "duree_preavis_jours", nullable = false)
    private Integer dureePreavisJours = 0;

    @Column(name = "date_sortie", nullable = false)
    private LocalDate dateSortie;

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_initie_par", nullable = false)
    private Utilisateur initiePar;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private Boolean archive = false;
}