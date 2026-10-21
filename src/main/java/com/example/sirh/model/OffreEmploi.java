package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "offre_emploi")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OffreEmploi {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Integer idOffre;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande_recrutement", nullable = false)
    private DemandeRecrutement demandeRecrutement;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "date_publication")
    private LocalDateTime datePublication;
    
    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;
    
    // 🔥 Relation normalisée
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_offre_emploie", nullable = false)
    private StatutOffreEmploi statut;
}