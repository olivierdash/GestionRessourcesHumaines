package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regime_travail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegimeTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regime_travail")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, unique = true)
    private String libelle;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_decompte_id", nullable = false)
    private TypeDecompte typeDecompte;

    @Column(name = "heures_hebdomadaires", precision = 3, scale = 1)
    private BigDecimal heuresHebdomadaires;

    @Column(name = "jours_hebdomadaires")
    private Integer joursHebdomadaires;

    @Column(name = "jours_annuels_reference")
    private Integer joursAnnuelsReference;

    @Column(name = "pointage_obligatoire", nullable = false)
    @Builder.Default
    private Boolean pointageObligatoire = true;

    @Column(name = "seuil_heures_supplementaires", precision = 3, scale = 1)
    private BigDecimal seuilHeuresSupplementaires;

    @Column(name = "duree_max_hebdomadaire")
    private Integer dureeMaxHebdomadaire;

    @Column(name = "duree_max_journaliere")
    private Integer dureeMaxJournaliere;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    // L'ordre de tri utilise maintenant "dayweek" au lieu de "jourSemaine"
    @OneToMany(mappedBy = "regimeTravail", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayweek ASC")
    @Builder.Default
    private List<HoraireTravail> horaires = new ArrayList<>();

    @Transient
    public Boolean getForfaitJours() {
        return typeDecompte != null && "JOURS".equals(typeDecompte.getCode());
    }

    @Transient
    public void setForfaitJours(Boolean forfaitJours) {
        // La conversion est faite dans RegimeTravailService
    }
}