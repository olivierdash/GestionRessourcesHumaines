package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import java.time.LocalDate;

/**
 * @author moi
 * Module 11
 * ReponseEtDecision
 * en fonction de la réponse on décidera de la punition ou sanction donné 
 */

@Entity
@Table(name = "reponse_et_decision")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReponseEtDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decision")
    private Integer idDecision;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false, unique = true)
    private DemandeExplication demande;

    @Column(name = "date_reponse")
    private LocalDate dateReponse;

    @Column(name = "contenu_reponse", length = 500)
    private String contenuReponse;

    @Column(name = "type_sanction", nullable = false, length = 30)
    private String typeSanction = "AUCUNE";

    @Column(name = "date_notification")
    private LocalDate dateNotification;

    @Column(name = "commentaire_decision", length = 500)
    private String commentaireDecision;
}