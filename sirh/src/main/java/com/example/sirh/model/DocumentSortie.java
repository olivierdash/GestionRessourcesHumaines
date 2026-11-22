package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "document_sortie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentSortie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document_sortie")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rupture", nullable = false)
    private RuptureContrat rupture;

    @Column(name = "type_document", nullable = false, length = 30)
    private String typeDocument;

    @Column(length = 100)
    private String reference;

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(name = "date_generation", nullable = false)
    private LocalDate dateGeneration;

    @Column(name = "date_remise")
    private LocalDate dateRemise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genere_par")
    private Utilisateur generePar;
}