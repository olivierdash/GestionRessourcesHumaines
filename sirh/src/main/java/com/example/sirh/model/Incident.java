package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "incident")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident")
    private Integer idIncident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_declarant", nullable = false)
    private Utilisateur declarant;

    @Column(name = "date_fait", nullable = false)
    private LocalDate dateFait;

    @Column(nullable = false, length = 500)
    private String descriptions;

    @Column(nullable = false, length = 20)
    private String gravite;

    @Column(name = "statut_instruction", nullable = false, length = 25)
    private String statutInstruction = "OUVERT";

    // Relations filles (facultatif, utile pour mapper)
    @OneToOne(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private DemandeExplication demandeExplication;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentDocument> documents;
}