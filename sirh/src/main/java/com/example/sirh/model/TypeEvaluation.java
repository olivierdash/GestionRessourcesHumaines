package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "type_evaluation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypeEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_evaluation")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}