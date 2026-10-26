package com.example.sirh.service;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.StatutProposition;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.ValidationProposition;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.GrilleSalarialRepository;
import com.example.sirh.repository.PropositionEmbaucheRepository;
import com.example.sirh.repository.StatutPropositionRepository;
import com.example.sirh.repository.UtilisateurRepository;
import com.example.sirh.repository.ValidationPropositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropositionService {

    public static final String EN_ATTENTE = "EN_ATTENTE";
    public static final String SOUMISE = "SOUMISE";
    public static final String APPROUVEE = "APPROUVEE";
    public static final String ACCEPTE = "ACCEPTE";
    public static final String REFUSE = "REFUSE";
    public static final String EXPIREE = "EXPIREE";

    private static final List<String> STATUTS_ACTIFS = List.of(EN_ATTENTE, SOUMISE, APPROUVEE);
    private static final int JOURS_PATIENCE = 7;

    private final PropositionEmbaucheRepository propositionRepo;
    private final ValidationPropositionRepository validationRepo;
    private final StatutPropositionRepository statutRepo;
    private final CandidatureRepository candidatureRepo;
    private final EmployeRepository employeRepo;
    private final GrilleSalarialRepository grilleRepo;
    private final UtilisateurRepository utilisateurRepo;

    // ---------- Lecture ----------

    @Transactional(readOnly = true)
    public List<PropositionEmbauche> liste() {
        return propositionRepo.findAllByOrderByIdPropositionDesc();
    }

    /** Charge une proposition et la passe en EXPIREE si la date limite est dépassée. */
    @Transactional
    public PropositionEmbauche getProposition(Integer id) {
        PropositionEmbauche p = charger(id);
        appliquerExpiration(p);
        return p;
    }

    @Transactional(readOnly = true)
    public List<ValidationProposition> historiqueValidations(Integer idProposition) {
        return validationRepo.findByPropositionIdPropositionOrderByIdDesc(idProposition);
    }

    /** Retourne un message d'alerte si le salaire est hors grille (ou sans grille), sinon null. */
    @Transactional(readOnly = true)
    public String verifierGrille(PropositionEmbauche p) {
        Poste poste = p.getCandidature().getDemandeRecrutement().getPoste();
        Optional<GrilleSalarial> grille = grilleRepo.findFirstByPosteIdPosteAndDepartementIdDepartement(
                poste.getIdPoste(), poste.getDepartement().getIdDepartement());
        if (grille.isEmpty()) {
            return "Aucune grille salariale définie pour ce poste : impossible de vérifier le salaire.";
        }
        GrilleSalarial g = grille.get();
        BigDecimal salaire = p.getSalairePropose();
        if (salaire.compareTo(g.getSalaireMin()) < 0 || salaire.compareTo(g.getSalaireMax()) > 0) {
            return "Salaire hors grille : la grille va de " + g.getSalaireMin() + " à " + g.getSalaireMax() + ".";
        }
        return null;
    }

    // ---------- Étape « Proposition » ----------

    @Transactional
    public PropositionEmbauche proposer(Integer idCandidature, BigDecimal salaire, int joursValidite) {
        Candidature c = candidatureRepo.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));
        if (!"RETENU".equals(c.getStatutCandidature().getLibelle())) {
            throw new IllegalStateException("Seule une candidature RETENU peut recevoir une proposition.");
        }
        if (employeRepo.existsByCandidatureIdCandidature(idCandidature)) {
            throw new IllegalStateException("Cette candidature a déjà abouti à un dossier salarié.");
        }
        if (propositionRepo.existsByCandidatureIdCandidatureAndStatutLibelleIn(idCandidature, STATUTS_ACTIFS)) {
            throw new IllegalStateException("Une proposition est déjà en cours pour cette candidature.");
        }
        verifierSalaire(salaire);
        if (joursValidite < 1) {
            throw new IllegalArgumentException("La durée de validité doit être d'au moins 1 jour.");
        }
        PropositionEmbauche p = PropositionEmbauche.builder()
                .candidature(c)
                .salairePropose(salaire)
                .dateExpiration(LocalDateTime.now().plusDays(joursValidite))
                .statut(statut(EN_ATTENTE))
                .build();
        return propositionRepo.save(p);
    }

    @Transactional
    public void modifierSalaire(Integer id, BigDecimal salaire) {
        PropositionEmbauche p = charger(id);
        exigerStatut(p, EN_ATTENTE, "modifier le salaire");
        verifierSalaire(salaire);
        p.setSalairePropose(salaire);
    }

    @Transactional
    public void soumettre(Integer id) {
        PropositionEmbauche p = charger(id);
        exigerStatut(p, EN_ATTENTE, "soumettre");
        p.setStatut(statut(SOUMISE));
    }

    // ---------- Étape « Valider » ----------

    @Transactional
    public void valider(Integer id, String emailValidateur, boolean approuve, String commentaire) {
        PropositionEmbauche p = charger(id);
        exigerStatut(p, SOUMISE, "valider");

        Utilisateur validateur = utilisateurRepo.findByEmail(emailValidateur)
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable."));
        String role = validateur.getRole().getLibelle();
        if (!"DG".equals(role) && !"FINANCE".equals(role)) {
            throw new IllegalStateException("Seuls la Direction (DG) et la Finance peuvent valider une proposition.");
        }
        if (!approuve && (commentaire == null || commentaire.isBlank())) {
            throw new IllegalArgumentException("Un commentaire est obligatoire pour refuser.");
        }

        validationRepo.save(ValidationProposition.builder()
                .proposition(p)
                .validateur(validateur)
                .departement(p.getCandidature().getDemandeRecrutement().getPoste().getDepartement())
                .decision(approuve ? "APPROUVEE" : "REFUSEE")
                .commentaire(commentaire)
                .build());

        // Approuvée : on attend le candidat. Refusée : retour à la proposition pour correction.
        p.setStatut(statut(approuve ? APPROUVEE : EN_ATTENTE));
    }

    // ---------- Étape « Réponse du candidat » ----------

    /** reponse = ACCEPTER, REFUSER ou PATIENTER. Retourne le message à afficher. */
    @Transactional
    public String repondre(Integer id, String reponse, String remarque) {
        PropositionEmbauche p = charger(id);
        if (appliquerExpiration(p)) {
            return "La proposition a expiré : la réponse n'a pas été enregistrée. Une nouvelle proposition peut être créée.";
        }
        exigerStatut(p, APPROUVEE, "enregistrer la réponse");
        if (reponse == null) {
            throw new IllegalArgumentException("Réponse manquante.");
        }
        if (remarque != null && !remarque.isBlank()) {
            p.setRemarque(remarque.trim());
        }
        switch (reponse) {
            case "ACCEPTER" -> {
                p.setStatut(statut(ACCEPTE));
                p.setSalaireFinal(p.getSalairePropose());
                p.setDateDecision(LocalDateTime.now());
                return "Proposition acceptée. Le dossier salarié peut être créé.";
            }
            case "REFUSER" -> {
                p.setStatut(statut(REFUSE));
                p.setDateDecision(LocalDateTime.now());
                return "Proposition refusée par le candidat. Une nouvelle proposition peut être créée.";
            }
            case "PATIENTER" -> {
                p.setDateExpiration(p.getDateExpiration().plusDays(JOURS_PATIENCE));
                return "Le candidat patiente : l'expiration est repoussée de " + JOURS_PATIENCE + " jours.";
            }
            default -> throw new IllegalArgumentException("Réponse inconnue : " + reponse);
        }
    }

    // ---------- Outils internes ----------

    private PropositionEmbauche charger(Integer id) {
        return propositionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposition introuvable : " + id));
    }

    private StatutProposition statut(String libelle) {
        return statutRepo.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Statut manquant en base : " + libelle + " (applique le script SQL des statuts)."));
    }

    private void exigerStatut(PropositionEmbauche p, String attendu, String action) {
        String actuel = p.getStatut().getLibelle();
        if (!attendu.equals(actuel)) {
            throw new IllegalStateException("Impossible de " + action + " : la proposition est au statut " + actuel + ".");
        }
    }

    private void verifierSalaire(BigDecimal salaire) {
        if (salaire == null || salaire.signum() <= 0) {
            throw new IllegalArgumentException("Le salaire doit être strictement positif.");
        }
    }

    /** Passe une proposition APPROUVEE dont la date limite est dépassée au statut EXPIREE. */
    private boolean appliquerExpiration(PropositionEmbauche p) {
        boolean echue = APPROUVEE.equals(p.getStatut().getLibelle())
                && p.getDateExpiration().isBefore(LocalDateTime.now());
        if (echue) {
            p.setStatut(statut(EXPIREE));
            p.setDateDecision(LocalDateTime.now());
        }
        return echue;
    }
}