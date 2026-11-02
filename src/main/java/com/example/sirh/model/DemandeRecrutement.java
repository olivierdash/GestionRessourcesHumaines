package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity 
@Table(name = "demande_recrutement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeRecrutement {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande_recrutement")
    private Integer idDemandeRecrutement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_manager", nullable = false)
    private Utilisateur manager;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    
    @Column(name = "date_creation", updatable = false, insertable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_souhaitee")
    private LocalDate dateSouhaitee;
    
    @Column(nullable = false)
    private Integer effectif;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_motif_demande", nullable = false)
    private MotifDemande motif;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etat_demande", nullable = false)
    private EtatDemande etatDemande;

    @Column(name = "commentaires", columnDefinition = "TEXT")
    private String commentaires;
}