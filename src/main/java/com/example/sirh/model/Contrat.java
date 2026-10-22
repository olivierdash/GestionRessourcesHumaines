package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "contract")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contrat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contract")
    private Integer idContract;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_contrat", nullable = false)
    private TypeContrat typeContrat;
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    @Column(name = "date_fin")
    private LocalDate dateFin;
    @Column(name = "salaire_mensuel", nullable = false)
    private BigDecimal salaireMensuel;
    @Column(nullable = false)
    private String status;
}