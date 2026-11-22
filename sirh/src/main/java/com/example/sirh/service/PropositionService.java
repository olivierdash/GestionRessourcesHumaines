package com.example.sirh.service;

import com.example.sirh.constants.ConstantSirh.Action;
import com.example.sirh.constants.ConstantSirh.Decision;
import com.example.sirh.constants.ConstantSirh.Role;
import com.example.sirh.constants.ConstantSirh.Statut;
import com.example.sirh.dto.PropositionEmbaucheFilter;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.ValidationProposition;
import com.example.sirh.repository.PropositionEmbaucheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.sirh.constants.ConstantSirh.Action;
import static com.example.sirh.constants.ConstantSirh.Role;
import static com.example.sirh.constants.ConstantSirh.Statut;

@Service
@RequiredArgsConstructor
public class PropositionService {

    private static final List<String> STATUTS_ACTIFS = List.of(Statut.EN_ATTENTE, Statut.SOUMISE, Statut.APPROUVEE);
    private static final int JOURS_PATIENCE = 7;

    private final PropositionEmbaucheRepository propositionRepo;
    private final ValidationPropositionService validationPropositionService;
    private final StatutPropositionService statutPropositionService;
    private final CandidatureService candidatureService;
    private final EmployeService employeService;
    private final GrilleSalarialService grilleSalarialService;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<PropositionEmbauche> liste() {
        return propositionRepo.findAllByOrderByIdPropositionDesc();
    }

    @Transactional(readOnly = true)
    public List<PropositionEmbauche> liste(PropositionEmbaucheFilter filter) {
        return propositionRepo.findWithFilters(filter);
    }

    @Transactional
    public PropositionEmbauche getProposition(Integer id) {
        PropositionEmbauche p = charger(id);
        appliquerExpiration(p);
        return p;
    }

    @Transactional(readOnly = true)
    public List<PropositionEmbauche> listerPourCandidat(Integer idCandidat) {
        return propositionRepo
                .findByCandidatureCandidatIdCandidatOrderByIdPropositionDesc(idCandidat);
    }

    @Transactional
    public PropositionEmbauche getPourCandidat(Integer idProposition, Integer idCandidat) {
        PropositionEmbauche p = propositionRepo
                .findByIdPropositionAndCandidatureCandidatIdCandidat(idProposition, idCandidat)
                .orElseThrow(() -> new IllegalArgumentException("Proposition introuvable."));
        appliquerExpiration(p);
        return p;
    }

    @Transactional(readOnly = true)
    public String verifierGrille(PropositionEmbauche p) {
        Poste poste = p.getCandidature().getDemandeRecrutement().getPoste();
        Optional<GrilleSalarial> grille = grilleSalarialService.getGrillePourPosteEtDepartement(
                poste.getIdPoste(), poste.getDepartement().getIdDepartement());
        if (grille.isEmpty()) {
            return "Aucune grille salariale définie pour ce poste : impossible de vérifier le salaire.";
        }
        GrilleSalarial g = grille.get();
        BigDecimal salaire = p.getSalairePropose();
        if (salaire.compareTo(g.getSalaireMin()) < 0 || salaire.compareTo(g.getSalaireMax()) > 0) {
            return "Salaire hors grille : la grille va de "
                    + g.getSalaireMin() + " à " + g.getSalaireMax() + ".";
        }
        return null;
    }

    @Transactional
    public PropositionEmbauche proposer(Integer idCandidature, BigDecimal salaire, int joursValidite) {
        Candidature c = candidatureService.getCandidatureById(idCandidature);

        if (!Statut.RETENU.equals(c.getStatutCandidature().getLibelle())) {
            throw new IllegalStateException(
                    "Seule une candidature RETENU peut recevoir une proposition.");
        }
        if (employeService.existsByCandidature(idCandidature)) {
            throw new IllegalStateException(
                    "Cette candidature a déjà abouti à un dossier salarié.");
        }
        if (propositionRepo.existsByCandidatureIdCandidatureAndStatutLibelleIn(
                idCandidature, STATUTS_ACTIFS)) {
            throw new IllegalStateException(
                    "Une proposition est déjà en cours pour cette candidature.");
        }
        verifierSalaire(salaire);
        if (joursValidite < 1) {
            throw new IllegalArgumentException(
                    "La durée de validité doit être d'au moins 1 jour.");
        }
        PropositionEmbauche p = PropositionEmbauche.builder()
                .candidature(c)
                .salairePropose(salaire)
                .dateExpiration(LocalDateTime.now().plusDays(joursValidite))
                .statut(statutPropositionService.getByLibelle(Statut.EN_ATTENTE))
                .build();
        return propositionRepo.save(p);
    }

    @Transactional
    public void modifierSalaire(Integer id, BigDecimal salaire) {
        PropositionEmbauche p = charger(id);
        exigerStatutParmi(p, List.of(Statut.EN_ATTENTE, Statut.APPROUVEE),
                "modifier le salaire");
        verifierSalaire(salaire);
        verifierSalaireDansGrille(p, salaire);
        p.setSalairePropose(salaire);
    }

    @Transactional
    public void soumettre(Integer id) {
        PropositionEmbauche p = charger(id);
        exigerStatut(p, Statut.EN_ATTENTE, "soumettre");
        p.setStatut(statutPropositionService.getByLibelle(Statut.SOUMISE));
    }

    @Transactional
    public void valider(Integer id, String emailValidateur, boolean approuve, String commentaire) {
        PropositionEmbauche p = charger(id);
        exigerStatut(p, Statut.SOUMISE, "valider");

        Utilisateur validateur = utilisateurService.getByEmail(emailValidateur);
        String role = validateur.getRole().getLibelle();
        if (!Role.DG.equals(role) && !Role.FINANCE.equals(role)) {
            throw new IllegalStateException(
                    "Seuls la Direction (DG) et la Finance peuvent valider une proposition.");
        }
        if (!approuve && (commentaire == null || commentaire.isBlank())) {
            throw new IllegalArgumentException(
                    "Un commentaire est obligatoire pour refuser.");
        }

        validationPropositionService.save(ValidationProposition.builder()
                .proposition(p)
                .validateur(validateur)
                .departement(p.getCandidature().getDemandeRecrutement().getPoste().getDepartement())
                .decision(approuve ? Decision.APPROUVEE : Decision.REFUSEE)
                .commentaire(commentaire)
                .build());

        p.setStatut(statutPropositionService.getByLibelle(
                approuve ? Statut.APPROUVEE : Statut.EN_ATTENTE));
    }

    @Transactional
    public String repondre(Integer id, String reponse, String remarque) {
        PropositionEmbauche p = charger(id);
        if (appliquerExpiration(p)) {
            return "La proposition a expiré : la réponse n'a pas été enregistrée. "
                    + "Une nouvelle proposition peut être créée.";
        }
        exigerStatut(p, Statut.APPROUVEE, "enregistrer la réponse");
        return appliquerReponse(p, reponse, remarque);
    }

    @Transactional
    public String repondreCandidat(Integer idProposition, Integer idCandidat,
            String reponse, String remarque) {
        PropositionEmbauche p = propositionRepo
                .findByIdPropositionAndCandidatureCandidatIdCandidat(idProposition, idCandidat)
                .orElseThrow(() -> new IllegalArgumentException("Proposition introuvable."));
        if (appliquerExpiration(p)) {
            throw new IllegalStateException(
                    "Cette proposition a expiré. Vous ne pouvez plus y répondre.");
        }
        exigerStatut(p, Statut.APPROUVEE, "enregistrer la réponse");
        return appliquerReponse(p, reponse, remarque);
    }

    private String appliquerReponse(PropositionEmbauche p, String reponse, String remarque) {
        if (reponse == null) {
            throw new IllegalArgumentException("Réponse manquante.");
        }
        if (remarque != null && !remarque.isBlank()) {
            p.setRemarque(remarque.trim());
        }
        return switch (reponse) {
            case Action.ACCEPTER -> {
                p.setStatut(statutPropositionService.getByLibelle(Statut.ACCEPTE));
                p.setSalaireFinal(p.getSalairePropose());
                p.setDateDecision(LocalDateTime.now());
                yield "Proposition acceptée.";
            }
            case Action.REFUSER -> {
                p.setStatut(statutPropositionService.getByLibelle(Statut.REFUSE));
                p.setDateDecision(LocalDateTime.now());
                yield "Proposition refusée par le candidat.";
            }
            case Action.PATIENTER -> {
                p.setDateExpiration(p.getDateExpiration().plusDays(JOURS_PATIENCE));
                yield "Le candidat patiente : l'expiration est repoussée de "
                        + JOURS_PATIENCE + " jours.";
            }
            default -> throw new IllegalArgumentException("Réponse inconnue : " + reponse);
        };
    }

    private PropositionEmbauche charger(Integer id) {
        return propositionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proposition introuvable : " + id));
    }

    private void exigerStatut(PropositionEmbauche p, String attendu, String action) {
        exigerStatutParmi(p, List.of(attendu), action);
    }

    private void exigerStatutParmi(PropositionEmbauche p, List<String> attendus, String action) {
        String actuel = p.getStatut().getLibelle();
        if (!attendus.contains(actuel)) {
            throw new IllegalStateException(
                    "Impossible de " + action + " : la proposition est au statut " + actuel + ".");
        }
    }

    private void verifierSalaire(BigDecimal salaire) {
        if (salaire == null || salaire.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le salaire doit être strictement positif.");
        }
    }

    private void verifierSalaireDansGrille(PropositionEmbauche p, BigDecimal salaire) {
        Poste poste = p.getCandidature().getDemandeRecrutement().getPoste();
        grilleSalarialService.getGrillePourPosteEtDepartement(
                poste.getIdPoste(), poste.getDepartement().getIdDepartement())
                .ifPresent(g -> {
                    if (salaire.compareTo(g.getSalaireMin()) < 0
                            || salaire.compareTo(g.getSalaireMax()) > 0) {
                        throw new IllegalArgumentException(
                                "Le salaire doit être compris dans la grille : "
                                        + g.getSalaireMin() + " – " + g.getSalaireMax() + " €.");
                    }
                });
    }

    private boolean appliquerExpiration(PropositionEmbauche p) {
        boolean echue = Statut.APPROUVEE.equals(p.getStatut().getLibelle())
                && p.getDateExpiration().isBefore(LocalDateTime.now());
        if (echue) {
            p.setStatut(statutPropositionService.getByLibelle(Statut.EXPIREE));
            p.setDateDecision(LocalDateTime.now());
        }
        return echue;
    }
}