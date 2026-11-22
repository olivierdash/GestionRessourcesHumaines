package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate; // <-- IMPORT NÉCESSAIRE
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Integer idDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_document", nullable = false)
    private TypeDocument typeDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tache")
    private OnboardingTache tache;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "fichier_url", nullable = false, length = 500)
    private String fichierUrl;

    @Column(name = "date_depot")
    private LocalDateTime dateDepot;

    // ==========================================
    // PARTIE AJOUTÉE POUR LA VALIDITÉ (Bloc 5)
    // ==========================================
    
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "statut_document", length = 50)
    private String statutDocument = "VALIDE";

    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_depose_par", nullable = false)
    private Utilisateur deposePar;
}