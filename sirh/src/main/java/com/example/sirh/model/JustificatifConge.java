package com.example.sirh.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.sirh.model.enums.StatutJustificatif;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "justificatif_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JustificatifConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justificatif")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande_conge")
    private DemandeConge demandeConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_absence")
    private Absence absence;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "chemin_stockage", nullable = false, length = 500)
    private String cheminStockage;

    @Column(name = "type_mime", length = 100)
    private String typeMime;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur_depot", nullable = false)
    private Utilisateur deposePar;

    @CreationTimestamp
    @Column(name = "date_depot", nullable = false, updatable = false)
    private LocalDateTime dateDepot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutJustificatif statut = StatutJustificatif.DEPOSE;

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private Utilisateur validateur;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
}
