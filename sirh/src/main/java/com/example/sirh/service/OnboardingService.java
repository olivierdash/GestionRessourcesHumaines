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
import com.example.sirh.repository.OnboardingRepository;
import com.example.sirh.constants.ConstantSirh.Statut;

@Service
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final OnboardingTacheService onboardingTacheService;
    private final StatutTacheOnboardingService statutTacheOnboardingService;
    private final EmployeService employeService;
    private final UtilisateurService utilisateurService;
    private final DocumentService documentService;
    private final StatutEmployeService statutEmployeService;
    // + le paramètre correspondant dans le constructeur et son this.xxx = xxx

    public OnboardingService(OnboardingRepository onboardingRepository,
            OnboardingTacheService onboardingTacheService,
            StatutTacheOnboardingService statutTacheOnboardingService,
            UtilisateurService utilisateurService,
            EmployeService employeService,
            DocumentService documentService,
            StatutEmployeService statutEmployeService) {
        this.onboardingRepository = onboardingRepository;
        this.onboardingTacheService = onboardingTacheService;
        this.statutTacheOnboardingService = statutTacheOnboardingService;
        this.utilisateurService = utilisateurService;
        this.employeService = employeService;
        this.documentService = documentService;
        this.statutEmployeService = statutEmployeService;

    }

    private void creerTache(Onboarding onboarding, String titre, String roleLibelle, int jours) {
        Utilisateur responsable = utilisateurService.findFirstByRoleLibelle(roleLibelle);

        StatutTacheOnboarding aFaire = statutTacheOnboardingService.getByLibelle("à faire");

        OnboardingTache tache = new OnboardingTache();
        tache.setOnboarding(onboarding);
        tache.setTitre(titre);
        tache.setResponsable(responsable);
        tache.setDateEcheance(onboarding.getDateDebut().plusDays(jours));
        tache.setObligatoire(true);
        tache.setStatut(aFaire);

        onboardingTacheService.save(tache);
    }

    private String documentExige(String titreTache) {
        return switch (titreTache) {
            case "Contrat signé" -> "Contrat signé";
            case "Inscription à la paie" -> "RIB";
            default -> null;
        };
    }

    @Transactional(readOnly = true)
    public List<OnboardingTache> taches(Integer idOnboarding) {
        return onboardingTacheService.findByOnboarding(idOnboarding);
    }

    // Page 19 : retrouver l'onboarding d'un salarié
    @Transactional(readOnly = true)
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
        OnboardingTache tache = onboardingTacheService.getById(idTache);

        StatutTacheOnboarding terminee = statutTacheOnboardingService.getByLibelle("terminée");

        String typeExige = documentExige(tache.getTitre());
        if (typeExige != null
                && !documentService.existsByTacheIdAndTypeDocumentLibelle(tache.getId(), typeExige)) {
            throw new IllegalStateException(
                    "Déposez d'abord le document requis : " + typeExige);
        }
        tache.setStatut(terminee);
        onboardingTacheService.saveAndFlush(tache);
        activerSiComplet(tache.getId());
        return tache;
    }

    @Transactional
    public OnboardingTache terminerTache(Integer idTache, Integer idUtilisateur, boolean estRh) {
        OnboardingTache tache = onboardingTacheService.getById(idTache);

        boolean estResponsable = tache.getResponsable() != null
                && tache.getResponsable().getIdUtilisateur().equals(idUtilisateur);

        if (!estRh && !estResponsable) {
            throw new IllegalStateException(
                    "Seul le responsable de la tâche ou la RH peut la terminer.");
        }
        return terminerTache(idTache);
    }

    @Transactional(readOnly = true)
    public int progression(Integer idOnboarding) {
        long total = onboardingTacheService.countByOnboarding(idOnboarding);
        if (total == 0) {
            return 0;
        }
        long terminees = onboardingTacheService.countTerminees(idOnboarding);
        return (int) (terminees * 100 / total);
    }

    @Transactional(readOnly = true)
    public boolean estComplet(Integer idOnboarding) {
        long restantes = onboardingTacheService.countObligatoiresNonTerminees(idOnboarding);
        return restantes == 0;
    }

    @Transactional
    public void activerSiComplet(Integer idTache) {
        OnboardingTache tache = onboardingTacheService.getById(idTache);

        Onboarding onboarding = tache.getOnboarding();

        if (estComplet(onboarding.getId())) {
            onboarding.setDateFin(LocalDate.now());
            onboardingRepository.save(onboarding);

            Employe employe = onboarding.getEmploye();
            employe.setStatusEmploye(statutEmployeService.getByLibelle(Statut.ACTIF_EMPLOYE));
            employeService.save(employe);
        }
    }
}