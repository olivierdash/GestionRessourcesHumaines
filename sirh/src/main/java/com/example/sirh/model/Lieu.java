package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lieu")
    private Integer idLieu;

    @Column(nullable = false, unique = true, length = 150)
    private String libelle;
}