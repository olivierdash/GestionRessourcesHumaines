package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "candidature")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Candidature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Integer idCandidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande_recrutement", nullable = false)
    private DemandeRecrutement demandeRecrutement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre", nullable = false)
    private OffreEmploi offre;

    @Column(name = "date_depot_candidature", insertable = false, updatable = false)
    private LocalDateTime dateDepotCandidature;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_candidature", nullable = false)
    private StatutCandidature statutCandidature;
}