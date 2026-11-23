package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * @author moi
 * Module 12
 * MaterielEmploye
 * le rendu du matos fournie par l'entreprise pour l'employer 
 */

@Entity
@Table(name = "materiel_employe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterielEmploye {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materiel_employe")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @Column(nullable = false, length = 150)
    private String designation;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "date_remise", nullable = false)
    private LocalDate dateRemise;

    @Column(name = "date_restitution")
    private LocalDate dateRestitution;

    @Column(name = "etat_restitution", length = 15)
    private String etatRestitution;

    @Column(name = "montant_retenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantRetenue = BigDecimal.ZERO;
}