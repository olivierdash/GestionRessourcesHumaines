package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Integer idRole;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;
}