package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.sirh.model.enums.TypeMouvementSolde;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mouvement_solde_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementSoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mouvement_solde")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solde_conge", nullable = false)
    private SoldeConge soldeConge;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 30)
    private TypeMouvementSolde typeMouvement;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal jours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande_conge")
    private DemandeConge demandeConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(length = 500)
    private String motif;

    @CreationTimestamp
    @Column(name = "date_mouvement", nullable = false, updatable = false)
    private LocalDateTime dateMouvement;
}
