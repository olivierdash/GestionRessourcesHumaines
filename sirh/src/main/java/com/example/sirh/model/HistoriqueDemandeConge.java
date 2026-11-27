package com.example.sirh.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.sirh.model.enums.StatutDemandeConge;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "historique_demande_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueDemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_demande_conge")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande_conge", nullable = false)
    private DemandeConge demandeConge;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_avant", length = 30)
    private StatutDemandeConge statutAvant;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_apres", nullable = false, length = 30)
    private StatutDemandeConge statutApres;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(length = 500)
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_changement", nullable = false, updatable = false)
    private LocalDateTime dateChangement;
}
