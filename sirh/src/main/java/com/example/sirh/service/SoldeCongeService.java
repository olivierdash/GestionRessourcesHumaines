package com.example.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DureeCongeDto;
import com.example.sirh.dto.SoldeCongeView;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.DroitConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.MouvementSoldeConge;
import com.example.sirh.model.SoldeConge;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.ModeAcquisitionDroit;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.model.enums.TypeMouvementSolde;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.DemandeCongeRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.MouvementSoldeCongeRepository;
import com.example.sirh.repository.SoldeCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoldeCongeService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal PLAFOND_PAR_DEFAUT = new BigDecimal("9999.00");

    private final SoldeCongeRepository soldeCongeRepository;
    private final MouvementSoldeCongeRepository mouvementRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final TypeCongeService typeCongeService;
    private final DroitCongeService droitCongeService;
    private final CalculDureeCongeService calculDureeCongeService;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Lecture
    // ------------------------------------------------------------------

    public BigDecimal disponible(SoldeConge solde) {
        if (solde == null) {
            return ZERO;
        }
        return nz(solde.getJoursAcquis())
                .add(nz(solde.getJoursReportes()))
                .add(nz(solde.getJoursAjustes()))
                .subtract(nz(solde.getJoursUtilises()))
                .subtract(nz(solde.getJoursEnAttente()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public Optional<SoldeConge> trouver(Integer employeId, Integer typeId, Integer annee) {
        return soldeCongeRepository.findByEmployeIdEmployerAndTypeCongeIdAndAnnee(
                employeId, typeId, annee);
    }

    @Transactional(readOnly = true)
    public SoldeConge getById(Integer id) {
        return soldeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solde de congé introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<SoldeConge> rechercher(Integer annee, Integer employeId, Integer typeId) {
        return soldeCongeRepository.rechercher(annee, employeId, typeId);
    }

    @Transactional(readOnly = true)
    public List<MouvementSoldeConge> mouvements(Integer soldeId) {
        return mouvementRepository.findBySoldeCongeIdOrderByDateMouvementDesc(soldeId);
    }

    /**
     * Soldes d'un salarié pour une année, y compris les types non encore initialisés.
     */
    @Transactional
    public List<SoldeCongeView> soldesEmploye(Integer employeId, Integer annee) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable : " + employeId));
        int anneeCible = annee == null ? LocalDate.now().getYear() : annee;

        List<SoldeCongeView> vues = new ArrayList<>();
        for (TypeConge type : typeCongeService.listerCongesAvecSoldeActifs()) {
            SoldeConge solde = getOuCreerSolde(employe, type, anneeCible);
            SoldeCongeView vue = new SoldeCongeView();
            vue.setType(type);
            vue.setSolde(solde);
            vue.setDisponible(disponible(solde));
            vues.add(vue);
        }
        return vues;
    }

    // ------------------------------------------------------------------
    // Réservation / consommation
    // ------------------------------------------------------------------

    @Transactional
    public void reserver(DemandeConge demande) {
        if (!Boolean.TRUE.equals(demande.getSoldeRequis())
                || !Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde())) {
            return;
        }

        Map<Integer, BigDecimal> parAnnee = repartirParAnnee(demande);
        Map<Integer, SoldeConge> soldes = new TreeMap<>();
        Map<String, String> erreurs = new LinkedHashMap<>();

        for (Map.Entry<Integer, BigDecimal> entree : parAnnee.entrySet()) {
            SoldeConge solde = getOuCreerSolde(demande.getEmploye(),
                    demande.getTypeConge(), entree.getKey());
            soldes.put(entree.getKey(), solde);

            if (Boolean.TRUE.equals(solde.getAnneeCloturee())) {
                erreurs.put("solde", "L'année " + entree.getKey()
                        + " est clôturée : aucune nouvelle demande ne peut l'impacter.");
                continue;
            }
            if (disponible(solde).compareTo(entree.getValue()) < 0) {
                erreurs.put("solde", "Solde insuffisant pour " + entree.getKey()
                        + " : disponible " + disponible(solde) + " jour(s), demandé "
                        + entree.getValue() + " jour(s).");
            }
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        for (Map.Entry<Integer, BigDecimal> entree : parAnnee.entrySet()) {
            SoldeConge solde = soldes.get(entree.getKey());
            solde.setJoursEnAttente(nz(solde.getJoursEnAttente()).add(entree.getValue()));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.RESERVATION, entree.getValue(),
                    demande, demande.getCreateur(), "Réservation à la soumission");
        }
    }

    @Transactional
    public void consommer(DemandeConge demande, Utilisateur validateur) {
        if (!Boolean.TRUE.equals(demande.getSoldeRequis())
                || !Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde())) {
            return;
        }

        for (Map.Entry<Integer, BigDecimal> entree : repartirParAnnee(demande).entrySet()) {
            SoldeConge solde = getSoldeVerrouille(demande.getEmploye().getIdEmployer(),
                    demande.getTypeConge().getId(), entree.getKey());
            BigDecimal enAttente = nz(solde.getJoursEnAttente());
            if (enAttente.compareTo(entree.getValue()) < 0) {
                throw new IllegalStateException("Incohérence de réservation pour l'année "
                        + entree.getKey() + " (en attente " + enAttente + ", demandé "
                        + entree.getValue() + ").");
            }
            solde.setJoursEnAttente(enAttente.subtract(entree.getValue()));
            solde.setJoursUtilises(nz(solde.getJoursUtilises()).add(entree.getValue()));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.CONSOMMATION, entree.getValue(),
                    demande, validateur, "Consommation à l'approbation");
        }
    }

    @Transactional
    public void liberer(DemandeConge demande, Utilisateur utilisateur) {
        if (!Boolean.TRUE.equals(demande.getSoldeRequis())
                || !Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde())) {
            return;
        }

        for (Map.Entry<Integer, BigDecimal> entree : repartirParAnnee(demande).entrySet()) {
            SoldeConge solde = trouverVerrouille(demande.getEmploye().getIdEmployer(),
                    demande.getTypeConge().getId(), entree.getKey());
            if (solde == null) {
                continue;
            }
            BigDecimal montant = entree.getValue().min(nz(solde.getJoursEnAttente()));
            if (montant.signum() <= 0) {
                continue;
            }
            solde.setJoursEnAttente(nz(solde.getJoursEnAttente()).subtract(montant));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.LIBERATION, montant,
                    demande, utilisateur, "Libération après refus ou annulation");
        }
    }

    @Transactional
    public void annulerConsommation(DemandeConge demande, Utilisateur utilisateur) {
        if (!Boolean.TRUE.equals(demande.getSoldeRequis())
                || !Boolean.TRUE.equals(demande.getTypeConge().getConsommeSolde())) {
            return;
        }

        for (Map.Entry<Integer, BigDecimal> entree : repartirParAnnee(demande).entrySet()) {
            SoldeConge solde = trouverVerrouille(demande.getEmploye().getIdEmployer(),
                    demande.getTypeConge().getId(), entree.getKey());
            if (solde == null) {
                continue;
            }
            BigDecimal montant = entree.getValue().min(nz(solde.getJoursUtilises()));
            if (montant.signum() <= 0) {
                continue;
            }
            solde.setJoursUtilises(nz(solde.getJoursUtilises()).subtract(montant));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.ANNULATION_CONSOMMATION, montant,
                    demande, utilisateur, "Annulation d'une demande approuvée");
        }
    }

    // ------------------------------------------------------------------
    // Ajustement manuel
    // ------------------------------------------------------------------

    @Transactional
    public SoldeConge ajuster(Integer soldeId, BigDecimal jours, String motif,
                              boolean forcer, Utilisateur utilisateur) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        if (jours == null || jours.signum() == 0) {
            erreurs.put("jours", "Le nombre de jours doit être différent de zéro.");
        }
        if (motif == null || motif.isBlank()) {
            erreurs.put("motif", "Le motif de l'ajustement est obligatoire.");
        }
        if (!erreurs.isEmpty()) {
            throw new ValidationMetierException(erreurs);
        }

        SoldeConge existant = getById(soldeId);
        SoldeConge solde = getSoldeVerrouille(existant.getEmploye().getIdEmployer(),
                existant.getTypeConge().getId(), existant.getAnnee());

        if (Boolean.TRUE.equals(solde.getAnneeCloturee())) {
            erreurs.put("solde", "L'année " + solde.getAnnee() + " est clôturée.");
            throw new ValidationMetierException(erreurs);
        }

        BigDecimal nouveauDisponible = disponible(solde).add(jours);
        if (nouveauDisponible.signum() < 0 && !forcer) {
            erreurs.put("jours", "L'ajustement rendrait le solde négatif (disponible actuel : "
                    + disponible(solde) + "). Cochez « forcer » pour l'autoriser.");
            throw new ValidationMetierException(erreurs);
        }

        solde.setJoursAjustes(nz(solde.getJoursAjustes()).add(jours));
        soldeCongeRepository.save(solde);
        enregistrerMouvement(solde, TypeMouvementSolde.AJUSTEMENT, jours, null,
                utilisateur, motif.trim() + (forcer ? " (forcé)" : ""));
        return solde;
    }

    // ------------------------------------------------------------------
    // Initialisation annuelle et clôture
    // ------------------------------------------------------------------

    @Transactional
    public int initialiserAnnee(Integer annee, Utilisateur utilisateur) {
        if (annee == null || annee < 2000 || annee > 2100) {
            throw new IllegalArgumentException("Année invalide.");
        }

        if (annee > 2000) {
            cloturerAnnee(annee - 1, utilisateur);
        }

        List<TypeConge> types = typeCongeService.listerCongesAvecSoldeActifs();
        int compteur = 0;

        for (Employe employe : employeRepository.findAll()) {
            if (employe.getHiringDate() != null
                    && employe.getHiringDate().isAfter(LocalDate.of(annee, 12, 31))) {
                continue;
            }
            for (TypeConge type : types) {
                SoldeConge solde = getOuCreerSolde(employe, type, annee);
                appliquerReport(solde, utilisateur);
                compteur++;
            }
        }

        audit(utilisateur, "INITIALISATION_SOLDES_CONGES", "solde_conge", 0,
                "Initialisation des soldes " + annee + " (" + compteur + " lignes)");
        return compteur;
    }

    @Transactional
    public void cloturerAnnee(Integer annee, Utilisateur utilisateur) {
        if (annee == null || annee < 2000 || annee > 2100) {
            throw new IllegalArgumentException("Année invalide.");
        }

        List<StatutDemandeConge> enAttente = List.of(
                StatutDemandeConge.SOUMISE, StatutDemandeConge.VALIDEE_MANAGER);
        long demandesEnAttente = demandeCongeRepository.countChevauchementsPeriode(
                LocalDate.of(annee, 1, 1), LocalDate.of(annee, 12, 31), enAttente);
        if (demandesEnAttente > 0) {
            Map<String, String> erreurs = new LinkedHashMap<>();
            erreurs.put("cloture", "Impossible de clôturer " + annee + " : "
                    + demandesEnAttente + " demande(s) sont encore en attente sur cette année. "
                    + "Traitez-les avant de relancer la clôture.");
            throw new ValidationMetierException(erreurs);
        }

        List<SoldeConge> soldes = soldeCongeRepository.findByAnnee(annee);
        for (SoldeConge solde : soldes) {
            if (!Boolean.TRUE.equals(solde.getAnneeCloturee())) {
                solde.setAnneeCloturee(true);
                soldeCongeRepository.save(solde);
            }
        }

        // Le report de N-1 est appliqué dès que N existe et que N-1 est clôturée.
        for (SoldeConge solde : soldeCongeRepository.findByAnnee(annee + 1)) {
            appliquerReport(solde, utilisateur);
        }

        audit(utilisateur, "CLOTURE_ANNEE_CONGES", "solde_conge", 0,
                "Clôture de l'année " + annee + " (" + soldes.size() + " soldes)");
    }

    @Transactional
    public int cloturerEtInitialiser(Integer annee, Utilisateur utilisateur) {
        cloturerAnnee(annee, utilisateur);
        return initialiserAnnee(annee + 1, utilisateur);
    }

    private void appliquerReport(SoldeConge solde, Utilisateur utilisateur) {
        if (Boolean.TRUE.equals(solde.getReportInitialise())) {
            return;
        }
        TypeConge type = solde.getTypeConge();
        if (!Boolean.TRUE.equals(type.getReportAutorise())) {
            solde.setReportInitialise(true);
            soldeCongeRepository.save(solde);
            return;
        }

        Optional<SoldeConge> precedent = soldeCongeRepository
                .findByEmployeIdEmployerAndTypeCongeIdAndAnnee(
                        solde.getEmploye().getIdEmployer(), type.getId(), solde.getAnnee() - 1);
        if (precedent.isEmpty() || !Boolean.TRUE.equals(precedent.get().getAnneeCloturee())) {
            return;
        }

        BigDecimal disponiblePrecedent = disponible(precedent.get()).max(BigDecimal.ZERO);
        BigDecimal plafond = type.getPlafondReportJours() == null
                ? PLAFOND_PAR_DEFAUT : type.getPlafondReportJours();
        BigDecimal report = disponiblePrecedent.min(plafond).setScale(2, RoundingMode.HALF_UP);

        if (report.signum() > 0) {
            solde.setJoursReportes(nz(solde.getJoursReportes()).add(report));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.REPORT, report, null,
                    utilisateur, "Report de l'année " + (solde.getAnnee() - 1));
        }

        solde.setReportInitialise(true);
        soldeCongeRepository.save(solde);
    }

    // ------------------------------------------------------------------
    // Moteur d'acquisition
    // ------------------------------------------------------------------

    @Transactional
    public SoldeConge getOuCreerSolde(Employe employe, TypeConge type, Integer annee) {
        Optional<SoldeConge> existant = soldeCongeRepository.findForUpdate(
                employe.getIdEmployer(), type.getId(), annee);
        if (existant.isPresent()) {
            SoldeConge solde = existant.get();
            appliquerAcquisition(solde);
            return solde;
        }

        SoldeConge solde = construireSolde(employe, type, annee);
        solde = soldeCongeRepository.save(solde);
        appliquerAcquisition(solde);
        return solde;
    }

    private SoldeConge construireSolde(Employe employe, TypeConge type, Integer annee) {
        Contrat contrat = resoudreContrat(employe, annee);
        int ancienneteMois = ancienneteEnMois(employe.getHiringDate(), LocalDate.of(annee, 1, 1));
        Optional<DroitConge> regle = droitCongeService.trouverRegle(
                type, contrat == null ? null : contrat.getTypeContrat(), ancienneteMois);

        BigDecimal taux = regle.map(DroitConge::getJoursParAn).orElse(BigDecimal.ZERO);
        ModeAcquisitionDroit mode = regle.map(DroitConge::getModeAcquisition)
                .orElse(ModeAcquisitionDroit.ANNUEL);
        boolean prorata = regle.map(r -> r.getProrataAnneeEmbauche() == null
                || r.getProrataAnneeEmbauche()).orElse(true);

        int moisDebut = 1;
        if (employe.getHiringDate() != null && employe.getHiringDate().getYear() == annee) {
            moisDebut = employe.getHiringDate().getMonthValue();
        }

        BigDecimal objectif = objectifAnnuel(taux, annee, employe.getHiringDate(), mode, prorata);

        return SoldeConge.builder()
                .employe(employe)
                .typeConge(type)
                .annee(annee)
                .tauxAnnuel(taux.setScale(2, RoundingMode.HALF_UP))
                .objectifAnnuel(objectif.setScale(2, RoundingMode.HALF_UP))
                .modeAcquisition(mode)
                .moisDebutAcquisition(moisDebut)
                .joursAcquis(ZERO)
                .joursReportes(ZERO)
                .joursAjustes(ZERO)
                .joursUtilises(ZERO)
                .joursEnAttente(ZERO)
                .dernierMoisAcquis(0)
                .anneeCloturee(false)
                .reportInitialise(false)
                .version(0L)
                .build();
    }

    private BigDecimal objectifAnnuel(BigDecimal taux, Integer annee, LocalDate hiringDate,
                                      ModeAcquisitionDroit mode, boolean prorata) {
        if (taux == null || taux.signum() <= 0 || hiringDate == null) {
            return ZERO;
        }
        int anneeEmbauche = hiringDate.getYear();
        if (annee < anneeEmbauche) {
            return ZERO;
        }
        boolean anneeEmbaucheCourante = annee == anneeEmbauche;
        if (!anneeEmbaucheCourante) {
            return taux;
        }

        int moisRestants = 13 - hiringDate.getMonthValue();
        if (mode == ModeAcquisitionDroit.MENSUEL || prorata) {
            BigDecimal brut = taux.multiply(BigDecimal.valueOf(moisRestants))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            return arrondirDemi(brut);
        }
        return taux;
    }

    private void appliquerAcquisition(SoldeConge solde) {
        if (Boolean.TRUE.equals(solde.getAnneeCloturee())) {
            return;
        }
        BigDecimal cible = cibleAcquisition(solde);
        BigDecimal acquis = nz(solde.getJoursAcquis());
        BigDecimal delta = cible.subtract(acquis);

        if (delta.signum() > 0) {
            solde.setJoursAcquis(acquis.add(delta));
            solde.setDernierMoisAcquis(dernierMoisAcquisition(solde));
            soldeCongeRepository.save(solde);
            enregistrerMouvement(solde, TypeMouvementSolde.ACQUISITION, delta, null,
                    null, "Acquisition " + solde.getAnnee());
        }
    }

    private BigDecimal cibleAcquisition(SoldeConge solde) {
        int anneeCourante = LocalDate.now().getYear();
        BigDecimal objectif = nz(solde.getObjectifAnnuel());

        if (solde.getAnnee() > anneeCourante) {
            return ZERO;
        }
        if (solde.getAnnee() < anneeCourante
                || solde.getModeAcquisition() == ModeAcquisitionDroit.ANNUEL) {
            return objectif;
        }

        int moisCourant = LocalDate.now().getMonthValue();
        int moisDebut = solde.getMoisDebutAcquisition() == null
                ? 1 : solde.getMoisDebutAcquisition();
        int moisDisponibles = 13 - moisDebut;
        int moisEcoules = Math.max(0, Math.min(moisCourant, 12) - moisDebut + 1);
        if (moisEcoules <= 0) {
            return ZERO;
        }
        BigDecimal cible = objectif.multiply(BigDecimal.valueOf(moisEcoules))
                .divide(BigDecimal.valueOf(moisDisponibles), 2, RoundingMode.HALF_UP);
        return cible.min(objectif);
    }

    private int dernierMoisAcquisition(SoldeConge solde) {
        if (solde.getAnnee() < LocalDate.now().getYear()) {
            return 12;
        }
        if (solde.getAnnee() > LocalDate.now().getYear()) {
            return 0;
        }
        return LocalDate.now().getMonthValue();
    }

    private Contrat resoudreContrat(Employe employe, Integer annee) {
        LocalDate premierJanvier = LocalDate.of(annee, 1, 1);
        LocalDate dernierDeLAnnee = LocalDate.of(annee, 12, 31);

        List<Contrat> actifs = contratRepository.findActifsPourDate(
                employe.getIdEmployer(), premierJanvier);
        if (!actifs.isEmpty()) {
            return actifs.get(0);
        }

        List<Contrat> tous = contratRepository.findByEmployeIdEmployer(employe.getIdEmployer());
        Optional<Contrat> premierDeLAnnee = tous.stream()
                .filter(c -> c.getDateDebut() != null
                        && !c.getDateDebut().isBefore(premierJanvier)
                        && !c.getDateDebut().isAfter(dernierDeLAnnee))
                .min(Comparator.comparing(Contrat::getDateDebut));
        if (premierDeLAnnee.isPresent()) {
            return premierDeLAnnee.get();
        }

        return tous.stream()
                .filter(c -> c.getDateDebut() != null
                        && !c.getDateDebut().isAfter(dernierDeLAnnee))
                .max(Comparator.comparing(Contrat::getDateDebut))
                .orElse(null);
    }

    private int ancienneteEnMois(LocalDate hiringDate, LocalDate reference) {
        if (hiringDate == null || hiringDate.isAfter(reference)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(hiringDate, reference);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    public Map<Integer, BigDecimal> repartirParAnnee(DemandeConge demande) {
        DureeCongeDto duree = calculDureeCongeService.detail(
                demande.getTypeConge(),
                demande.getDateDebut(),
                demande.getDateFin(),
                demande.getDemiJourDebut(),
                demande.getDemiJourFin());
        return new TreeMap<>(duree.getParAnnee());
    }

    public Map<String, BigDecimal> repartirParMois(DemandeConge demande) {
        DureeCongeDto duree = calculDureeCongeService.detail(
                demande.getTypeConge(),
                demande.getDateDebut(),
                demande.getDateFin(),
                demande.getDemiJourDebut(),
                demande.getDemiJourFin());
        return duree.getParMois();
    }

    private SoldeConge getSoldeVerrouille(Integer employeId, Integer typeId, Integer annee) {
        return soldeCongeRepository.findForUpdate(employeId, typeId, annee)
                .orElseThrow(() -> new IllegalStateException(
                        "Solde introuvable pour l'année " + annee + "."));
    }

    private SoldeConge trouverVerrouille(Integer employeId, Integer typeId, Integer annee) {
        return soldeCongeRepository.findForUpdate(employeId, typeId, annee).orElse(null);
    }

    private void enregistrerMouvement(SoldeConge solde, TypeMouvementSolde type,
                                      BigDecimal jours, DemandeConge demande,
                                      Utilisateur utilisateur, String motif) {
        mouvementRepository.save(MouvementSoldeConge.builder()
                .soldeConge(solde)
                .typeMouvement(type)
                .jours(jours.setScale(2, RoundingMode.HALF_UP))
                .demandeConge(demande)
                .utilisateur(utilisateur)
                .motif(motif)
                .build());
    }

    private BigDecimal nz(BigDecimal valeur) {
        return valeur == null ? ZERO : valeur.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal arrondirDemi(BigDecimal valeur) {
        return valeur.multiply(BigDecimal.valueOf(2))
                .setScale(0, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
    }

    private void audit(Utilisateur utilisateur, String action, String entite,
                       Integer idEntite, String details) {
        if (utilisateur == null || utilisateur.getEmail() == null) {
            return;
        }
        try {
            auditService.enregistrerAction(utilisateur.getEmail(), action, entite, idEntite, details);
        } catch (RuntimeException ignored) {
            // L'audit ne doit pas bloquer une opération métier déjà valide.
        }
    }

    public BigDecimal plafondReport(TypeConge type) {
        return type.getPlafondReportJours() == null
                ? PLAFOND_PAR_DEFAUT : type.getPlafondReportJours();
    }

    public YearMonth periodeDe(LocalDate date) {
        return YearMonth.from(date);
    }
}
