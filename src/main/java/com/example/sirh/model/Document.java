package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;
    @Column(name = "fichier_url", nullable = false, length = 500)
    private String fichierUrl;
    @Column(name = "date_depot")
    private LocalDateTime dateDepot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_depose_par", nullable = false)
    private Utilisateur deposePar;
}