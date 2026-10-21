package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employer")
    private Integer idEmployer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false, unique = true)
    private Candidature candidature;

    @Column(name = "matricule", nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "hiring_date", nullable = false)
    private LocalDate hiringDate;

    @Column(name = "status_employer", nullable = false, length = 50)
    private String statusEmployer;
}