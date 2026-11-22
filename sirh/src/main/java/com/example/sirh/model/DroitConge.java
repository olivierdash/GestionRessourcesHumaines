package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.sirh.model.enums.ModeAcquisitionDroit;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "droit_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroitConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_droit_conge")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeConge typeConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    @Column(name = "anciennete_min_mois", nullable = false)
    @Builder.Default
    private Integer ancienneteMinMois = 0;

    @Column(name = "anciennete_max_mois")
    private Integer ancienneteMaxMois;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_acquisition", nullable = false, length = 20)
    @Builder.Default
    private ModeAcquisitionDroit modeAcquisition = ModeAcquisitionDroit.ANNUEL;

    @Column(name = "jours_par_an", nullable = false, precision = 5, scale = 2)
    private BigDecimal joursParAn;

    @Column(name = "prorata_annee_embauche", nullable = false)
    @Builder.Default
    private Boolean prorataAnneeEmbauche = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
