package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "historique_demande_recrutement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoriqueDemandeRecrutement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_demande_recrutement")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande_recrutement", nullable = false)
    private DemandeRecrutement demandeRecrutement;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etat_demande", nullable = false)
    private EtatDemande etatDemande;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_validateur", nullable = false)
    private Utilisateur validateur;
    @Column(name = "date_changement", insertable = false, updatable = false)
    private LocalDateTime dateChangement;
    @Column(columnDefinition = "TEXT")
    private String commentaires;
}