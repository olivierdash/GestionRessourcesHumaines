package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.ModeDecompteConge;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_conge")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategorieTypeConge categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_decompte", nullable = false, length = 20)
    private ModeDecompteConge modeDecompte;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paye = true;

    @Column(name = "consomme_solde", nullable = false)
    @Builder.Default
    private Boolean consommeSolde = true;

    @Column(name = "justificatif_requis", nullable = false)
    @Builder.Default
    private Boolean justificatifRequis = false;

    @Column(name = "delai_justificatif_jours")
    private Integer delaiJustificatifJours;

    @Column(name = "duree_max_jours", precision = 5, scale = 2)
    private BigDecimal dureeMaxJours;

    @Column(name = "report_autorise", nullable = false)
    @Builder.Default
    private Boolean reportAutorise = false;

    @Column(name = "plafond_report_jours", precision = 5, scale = 2)
    private BigDecimal plafondReportJours;

    @Column(name = "demande_employe_autorisee", nullable = false)
    @Builder.Default
    private Boolean demandeEmployeAutorisee = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "ordre_affichage", nullable = false)
    @Builder.Default
    private Integer ordreAffichage = 0;

    @Column(length = 7)
    private String couleur;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
