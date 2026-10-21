package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "type_contrat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypeContrat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_contrat")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}