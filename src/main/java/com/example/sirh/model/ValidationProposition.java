package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "validation_proposition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationProposition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validation_proposition")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition", nullable = false)
    private PropositionEmbauche proposition;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_validateur", nullable = false)
    private Utilisateur validateur;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;
    @Column(nullable = false, length = 50)
    private String decision;
    @Column(name = "date_validation_proposition", insertable = false, updatable = false)
    private LocalDateTime dateValidationProposition;
    @Column(columnDefinition = "TEXT")
    private String commentaire;
}