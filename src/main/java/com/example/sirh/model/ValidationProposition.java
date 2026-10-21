package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_proposition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationProposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validation_proposition")
    private Integer idValidationProposition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition", nullable = false)
    private PropositionEmbauche proposition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_validateur", nullable = false)
    private Utilisateur validateur;

    @Column(name = "departement_role", length = 100)
    private String departementRole;

    @Column(name = "decision", nullable = false, length = 50)
    private String decision;

    @CreationTimestamp
    @Column(name = "date_validation_proposition", updatable = false)
    private LocalDateTime dateValidationProposition;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
}