package com.example.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.sirh.model.enums.ModeAcquisitionDroit;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solde_conge",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_solde_conge_employe_type_annee",
                columnNames = {"id_employer", "id_type_conge", "annee"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solde_conge")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeConge typeConge;

    @Column(nullable = false)
    private Integer annee;

    @Column(name = "taux_annuel", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxAnnuel = BigDecimal.ZERO;

    @Column(name = "objectif_annuel", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal objectifAnnuel = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_acquisition", nullable = false, length = 20)
    @Builder.Default
    private ModeAcquisitionDroit modeAcquisition = ModeAcquisitionDroit.ANNUEL;

    @Column(name = "mois_debut_acquisition", nullable = false)
    @Builder.Default
    private Integer moisDebutAcquisition = 1;

    @Column(name = "jours_acquis", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal joursAcquis = BigDecimal.ZERO;

    @Column(name = "jours_reportes", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal joursReportes = BigDecimal.ZERO;

    @Column(name = "jours_ajustes", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal joursAjustes = BigDecimal.ZERO;

    @Column(name = "jours_utilises", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal joursUtilises = BigDecimal.ZERO;

    @Column(name = "jours_en_attente", nullable = false, precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal joursEnAttente = BigDecimal.ZERO;

    @Column(name = "dernier_mois_acquis", nullable = false)
    @Builder.Default
    private Integer dernierMoisAcquis = 0;

    @Column(name = "annee_cloturee", nullable = false)
    @Builder.Default
    private Boolean anneeCloturee = false;

    @Column(name = "report_initialise", nullable = false)
    @Builder.Default
    private Boolean reportInitialise = false;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;
}
