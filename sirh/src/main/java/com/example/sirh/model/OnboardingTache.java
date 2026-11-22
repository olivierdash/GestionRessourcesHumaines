package com.example.sirh.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "onboarding_tache")
public class OnboardingTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tache")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_onboarding", nullable = false)
    private Onboarding onboarding;

    @Column(name = "titre", nullable = false)
    private String titre;

    @ManyToOne
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "est_obligatoire", nullable = false)
    private Boolean obligatoire = true;

    @ManyToOne
    @JoinColumn(name = "id_statut_tache_onboarding", nullable = false)
    private StatutTacheOnboarding statut;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Onboarding getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(Onboarding onboarding) {
        this.onboarding = onboarding;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Utilisateur getResponsable() {
        return responsable;
    }

    public void setResponsable(Utilisateur responsable) {
        this.responsable = responsable;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public Boolean getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }

    public StatutTacheOnboarding getStatut() {
        return statut;
    }

    public void setStatut(StatutTacheOnboarding statut) {
        this.statut = statut;
    }
}