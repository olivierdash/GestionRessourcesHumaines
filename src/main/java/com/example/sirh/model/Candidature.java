package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidature", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_candidat", "id_demande_recrutement"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Integer idCandidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande_recrutement", nullable = false)
    private DemandeRecrutement demandeRecrutement;

    @CreationTimestamp
    @Column(name = "date_depot_candidature", updatable = false)
    private LocalDateTime dateDepotCandidature;

    @Column(name = "statut_candidature", nullable = false, length = 50)
    private String statutCandidature;
}