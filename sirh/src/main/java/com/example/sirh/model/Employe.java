package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * @author moi
 * Module 11
 * Employe
 * ajoute de colonne archive et date d'archivage dans le cas où il y a un employer qui nous quitte 
 */

@Entity @Table(name = "employer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employer")
    private Integer idEmployer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false, unique = true)
    private Candidature candidature;

    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(name = "hiring_date", nullable = false)
    private LocalDate hiringDate;

    @Column(name = "status_employer")
    private String statusEmployer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "statut_employe_id", nullable = false)
    private StatutEmploye statusEmploye;

    @Column(nullable = false)
    private Boolean archive = false;

    @Column(name = "date_archivage")
    private LocalDate dateArchivage;

}