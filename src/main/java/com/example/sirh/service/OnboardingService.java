package com.example.sirh.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.Employe;
import com.example.sirh.model.Onboarding;
import com.example.sirh.model.OnboardingTache;
import com.example.sirh.model.StatutTacheOnboarding;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DocumentRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.OnboardingRepository;
import com.example.sirh.repository.OnboardingTacheRepository;
import com.example.sirh.repository.StatutTacheOnboardingRepository;
import com.example.sirh.repository.UtilisateurRepository;

@Service
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final OnboardingTacheRepository tacheRepository;
    private final StatutTacheOnboardingRepository statutRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DocumentRepository documentRepository;

    public OnboardingService(OnboardingRepository onboardingRepository,
            OnboardingTacheRepository tacheRepository,
            StatutTacheOnboardingRepository statutRepository,
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            DocumentRepository documentRepository) {
        this.onboardingRepository = onboardingRepository;
        this.tacheRepository = tacheRepository;
        this.statutRepository = statutRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.documentRepository = documentRepository;
    }

    private void creerTache(Onboarding onboarding, String titre, String roleLibelle, int jours) {
        Utilisateur responsable = utilisateurRepository.findFirstByRoleLibelle(roleLibelle)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun utilisateur avec le rôle " + roleLibelle));

        StatutTacheOnboarding aFaire = statutRepository.findByLibelle("à faire")
                .orElseThrow(() -> new IllegalStateException("Statut 'à faire' introuvable"));

        OnboardingTache tache = new OnboardingTache();
        tache.setOnboarding(onboarding);
        tache.setTitre(titre);
        tache.setResponsable(responsable);
        tache.setDateEcheance(onboarding.getDateDebut().plusDays(jours));
        tache.setObligatoire(true);
        tache.setStatut(aFaire);

        tacheRepository.save(tache);
    }

    private String documentExige(String titreTache) {
        return switch (titreTache) {
            case "Contrat signé" -> "Contrat signé";
            case "Inscription à la paie" -> "RIB";
            default -> null;
        };
    }

    public List<OnboardingTache> taches(Integer idOnboarding) {
        return tacheRepository.findByOnboardingIdOrderByDateEcheanceAsc(idOnboarding);
    }

    // Page 19 : retrouver l'onboarding d'un salarié
    public Optional<Onboarding> parEmploye(Integer idEmploye) {
        return onboardingRepository.findByEmployeIdEmployer(idEmploye);
    }

    // Créer le parcours d'un salarié
    @Transactional
    public Onboarding creer(Employe employe) {
        if (onboardingRepository.findByEmployeIdEmployer(employe.getIdEmployer()).isPresent()) {
            throw new IllegalStateException("Ce salarié a déjà un onboarding");
        }

        Onboarding onboarding = new Onboarding();
        onboarding.setEmploye(employe);
        onboarding.setDateDebut(LocalDate.now());
        onboarding = onboardingRepository.save(onboarding);

        creerTache(onboarding, "Contrat signé", "RH", 3);
        creerTache(onboarding, "Badge", "RESPONSABLE_IT", 5);
        creerTache(onboarding, "Compte informatique", "RESPONSABLE_IT", 5);
        creerTache(onboarding, "Matériel", "RESPONSABLE_IT", 7);
        creerTache(onboarding, "Formation", "RH", 14);
        creerTache(onboarding, "Inscription à la paie", "RH", 14);

        return onboarding;
    }

    @Transactional
    public OnboardingTache terminerTache(Integer idTache) {
        OnboardingTache tache = tacheRepository.findById(idTache)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));

        StatutTacheOnboarding terminee = statutRepository.findByLibelle("terminée")
                .orElseThrow(() -> new IllegalStateException("Statut 'terminée' introuvable"));

        String typeExige = documentExige(tache.getTitre());
        if (typeExige != null
                && !documentRepository.existsByTacheIdAndTypeDocumentLibelle(tache.getId(), typeExige)) {
            throw new IllegalStateException(
                    "Déposez d'abord le document requis : " + typeExige);
        }
        tache.setStatut(terminee);
        tacheRepository.saveAndFlush(tache);
        activerSiComplet(tache.getId());
        return tache;
    }

    public int progression(Integer idOnboarding) {
        long total = tacheRepository.countByOnboardingId(idOnboarding);
        if (total == 0) {
            return 0;
        }
        long terminees = tacheRepository.countByOnboardingIdAndStatutLibelle(idOnboarding, "terminée");
        return (int) (terminees * 100 / total);
    }

    public boolean estComplet(Integer idOnboarding) {
        long restantes = tacheRepository
                .countByOnboardingIdAndObligatoireTrueAndStatutLibelleNot(idOnboarding, "terminée");
        return restantes == 0;
    }

    @Transactional
    public void activerSiComplet(Integer idTache) {
        OnboardingTache tache = tacheRepository.findById(idTache)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));

        Onboarding onboarding = tache.getOnboarding();

        if (estComplet(onboarding.getId())) {
            onboarding.setDateFin(LocalDate.now());
            onboardingRepository.save(onboarding);

            Employe employe = onboarding.getEmploye();
            employe.setStatusEmployer("actif");
            employeRepository.save(employe);
        }
    }
}