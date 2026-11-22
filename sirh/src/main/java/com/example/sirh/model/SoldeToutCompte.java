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
import java.math.BigDecimal;

/**
 * @author moi 
 * Module 12
 * SoldeToutCompte
 */

@Entity
@Table(name = "solde_tout_compte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SoldeToutCompte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stc")
    private Integer idStc;

    // UNIQUE → OneToOne
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rupture", nullable = false, unique = true)
    private RuptureContrat ruptureContrat;

    @Column(name = "date_calcul", nullable = false)
    private LocalDate dateCalcul;

    @Column(name = "salaire_mensuel_reference", nullable = false, precision = 14, scale = 2)
    private BigDecimal salaireMensuelReference;

    @Column(name = "jours_conges_restants", nullable = false, precision = 7, scale = 2)
    private BigDecimal joursCongesRestants = BigDecimal.ZERO;

    @Column(name = "total_gains", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalGains = BigDecimal.ZERO;

    @Column(name = "total_retenues", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRetenues = BigDecimal.ZERO;

    @Column(name = "net_a_payer", nullable = false, precision = 14, scale = 2)
    private BigDecimal netAPayer = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String statut = "BROUILLON";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_valide_par")
    private Utilisateur validePar;

    @Column(name = "date_validation")
    private LocalDate dateValidation;
}