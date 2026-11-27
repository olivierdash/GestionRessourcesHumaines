package com.example.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.LigneRemunerationDto;
import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.dto.RemunerationDetailJourDto;
import com.example.sirh.dto.RemunerationDto;
import com.example.sirh.model.Absence;
import com.example.sirh.model.AffectationPlanning;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.HoraireTravail;
import com.example.sirh.model.Planning;
import com.example.sirh.model.Pointage;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.CodeVariablePaie;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.repository.AbsenceRepository;
import com.example.sirh.repository.AffectationPlanningRepository;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.PointageRepository;
import com.example.sirh.util.TauxHoraireCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemunerationService {

    private static final BigDecimal CINQ = new BigDecimal("5");
    private static final BigDecimal SOIXANTE = new BigDecimal("60");
    private static final BigDecimal DEMI = new BigDecimal("0.5");
    private static final BigDecimal TAUX_HEURES_SUP = new BigDecimal("1.25");
    private static final BigDecimal JOURS_MOIS_DEFAUT = new BigDecimal("21.67");

    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final AffectationPlanningRepository affectationPlanningRepository;
    private final AbsenceRepository absenceRepository;
    private final PointageRepository pointageRepository;
    private final PointageService pointageService;
    private final RegimeTravailService regimeTravailService;
    private final JourFerieService jourFerieService;

    @Transactional(readOnly = true)
    public RemunerationDto calculer(Integer employeId, YearMonth periode) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable."));
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        Contrat contrat = contratActif(employeId, debut, fin);
        List<AffectationPlanning> affectations =
                affectationPlanningRepository.findPourPeriode(employeId, debut, fin);

        RegimeTravail regimeReference = regimePour(affectations, fin);
        boolean forfaitJours = regimeReference != null
                && Boolean.TRUE.equals(regimeReference.getForfaitJours());

        BigDecimal heuresHebdo = heuresHebdomadaires(regimeReference, contrat);
        BigDecimal heuresMensuelles = TauxHoraireCalculator.heuresMensuelles(heuresHebdo);
        BigDecimal salaireMensuel = contrat == null ? BigDecimal.ZERO : nz(contrat.getSalaireMensuel());
        BigDecimal tauxHoraire = TauxHoraireCalculator.tauxHoraire(salaireMensuel, heuresMensuelles);
        BigDecimal joursOuvres = joursOuvres(debut, fin);

        List<Absence> absences = absenceRepository
                .findByEmployeIdEmployerInAndDateDebutLessThanEqualAndDateFinGreaterThanEqualAndStatutNot(
                        List.of(employeId), fin, debut, StatutAbsence.ANNULEE);

        Map<LocalDate, Pointage> pointages = new HashMap<>();
        for (Pointage pointage : pointageRepository
                .findByEmployeIdEmployerAndDatePointageBetweenOrderByDatePointageAsc(
                        employeId, debut, fin)) {
            pointages.put(pointage.getDatePointage(), pointage);
        }

        List<RemunerationDetailJourDto> jours = new ArrayList<>();
        Map<String, BigDecimal> retenuesParType = new LinkedHashMap<>();

        BigDecimal totalRetenues = BigDecimal.ZERO;
        BigDecimal totalHeuresSupplementaires = BigDecimal.ZERO;
        BigDecimal totalHeuresSupMontant = BigDecimal.ZERO;

        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            RegimeTravail regime = regimePour(affectations, date);
            HoraireTravail horaire = regimeTravailService
                    .trouverHorairePourJour(regime, date.getDayOfWeek());
            BigDecimal heuresPrevues = heuresPrevues(horaire, heuresHebdo);

            Absence absence = absencePour(absences, date);
            Pointage pointage = pointages.get(date);
            PointageCalculDto calcul = pointage == null ? null : pointageService.calculer(pointage);

            boolean ferie = jourFerieService.estFerie(date);

            String statut;
            String libelle = "";
            BigDecimal heuresTravaillees = BigDecimal.ZERO;
            BigDecimal heuresSup = BigDecimal.ZERO;
            BigDecimal retenue = BigDecimal.ZERO;

            if (ferie) {
                statut = "FERIE";
                libelle = jourFerieService.libelleFerie(date);
                if (calcul != null) {
                    heuresTravaillees = minutesEnHeures(calcul.getMinutesTravaillees());
                    heuresSup = minutesEnHeures(calcul.getMinutesSupplementaires());
                }
            } else if (absence != null) {
                libelle = absence.getTypeConge() == null
                        ? "Absence" : absence.getTypeConge().getLibelle();
                boolean conge = absence.getTypeConge() != null
                        && absence.getTypeConge().getCategorie() == CategorieTypeConge.CONGE;
                statut = conge ? "CONGE" : "ABSENCE";
                if (absenceSansMaintien(absence)) {
                    BigDecimal facteur = facteurJour(absence, date);
                    if (forfaitJours) {
                        retenue = div(salaireMensuel, joursOuvres).multiply(facteur);
                    } else {
                        retenue = heuresPrevues.multiply(facteur).multiply(tauxHoraire);
                    }
                }
            } else if (horaire == null) {
                statut = "REPOS";
            } else if (pointage != null && calcul != null) {
                statut = "TRAVAIL";
                heuresTravaillees = minutesEnHeures(calcul.getMinutesTravaillees());
                heuresSup = minutesEnHeures(calcul.getMinutesSupplementaires());
            } else {
                statut = "SANS_POINTAGE";
            }

            BigDecimal montantHeuresSup = heuresSup.multiply(tauxHoraire)
                    .multiply(TAUX_HEURES_SUP);
            totalHeuresSupMontant = totalHeuresSupMontant.add(montantHeuresSup);
            totalHeuresSupplementaires = totalHeuresSupplementaires.add(heuresSup);
            totalRetenues = totalRetenues.add(retenue);
            if (retenue.signum() > 0) {
                retenuesParType.merge(libelle, retenue, BigDecimal::add);
            }

            jours.add(RemunerationDetailJourDto.builder()
                    .date(date)
                    .jour(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH))
                    .statut(statut)
                    .libelle(libelle)
                    .heuresPrevues(scale2(heuresPrevues))
                    .heuresTravaillees(scale2(heuresTravaillees))
                    .heuresSupplementaires(scale2(heuresSup))
                    .retenue(scale2(retenue))
                    .montantHeuresSupplementaires(scale2(montantHeuresSup))
                    .build());
        }

        BigDecimal salaireBase = scale2(salaireMensuel);
        BigDecimal brut = scale2(salaireMensuel.subtract(totalRetenues).add(totalHeuresSupMontant));

        List<LigneRemunerationDto> lignes = new ArrayList<>();
        lignes.add(LigneRemunerationDto.builder()
                .code(CodeVariablePaie.BASE_CALCUL)
                .libelle("Salaire de base")
                .base(scale2(salaireMensuel))
                .taux(tauxHoraire)
                .quantite(forfaitJours ? scale2(joursOuvres) : heuresMensuelles)
                .unite(forfaitJours ? "JOUR" : "HEURE")
                .montant(salaireBase)
                .build());

        retenuesParType.forEach((type, montant) -> lignes.add(LigneRemunerationDto.builder()
                .code(CodeVariablePaie.ABSENCE_NON_PAYEE)
                .libelle("Retenue " + type)
                .unite(forfaitJours ? "JOUR" : "HEURE")
                .montant(scale2(montant))
                .build()));

        if (totalHeuresSupplementaires.signum() > 0) {
            lignes.add(LigneRemunerationDto.builder()
                    .code(CodeVariablePaie.HEURES_SUP_25)
                    .libelle("Heures supplémentaires 25 %")
                    .base(tauxHoraire)
                    .taux(TAUX_HEURES_SUP)
                    .quantite(scale2(totalHeuresSupplementaires))
                    .unite("HEURE")
                    .montant(scale2(totalHeuresSupMontant))
                    .build());
        }

        lignes.add(LigneRemunerationDto.builder()
                .code(CodeVariablePaie.BRUT_TOTAL)
                .libelle("Brut total")
                .montant(brut)
                .build());

        return RemunerationDto.builder()
                .employeId(employe.getIdEmployer())
                .matricule(employe.getMatricule())
                .nomComplet(nomComplet(employe))
                .periode(periode)
                .dateDebut(debut)
                .dateFin(fin)
                .forfaitJours(forfaitJours)
                .salaireMensuel(salaireBase)
                .heuresHebdomadaires(heuresHebdo)
                .heuresMensuelles(heuresMensuelles)
                .tauxHoraire(tauxHoraire)
                .joursOuvresMois(scale2(joursOuvres))
                .salaireBase(salaireBase)
                .totalRetenues(scale2(totalRetenues))
                .totalHeuresSupplementaires(scale2(totalHeuresSupplementaires))
                .brut(brut)
                .lignes(lignes)
                .jours(jours)
                .build();
    }

    private Contrat contratActif(Integer employeId, LocalDate debut, LocalDate fin) {
        List<Contrat> contrats = contratRepository.findActifsPourDate(employeId, fin);
        if (contrats.isEmpty()) {
            contrats = contratRepository.findActifsPourDate(employeId, debut);
        }
        return contrats.isEmpty() ? null : contrats.get(0);
    }

    private RegimeTravail regimePour(List<AffectationPlanning> affectations, LocalDate date) {
        for (AffectationPlanning affectation : affectations) {
            if (!couvre(affectation.getDateDebut(), affectation.getDateFin(), date)) {
                continue;
            }
            Planning planning = affectation.getPlanning();
            if (planning != null && couvre(planning.getDateDebut(), planning.getDateFin(), date)) {
                return planning.getRegimeTravail();
            }
        }
        return null;
    }

    private boolean couvre(LocalDate debut, LocalDate fin, LocalDate date) {
        if (debut != null && date.isBefore(debut)) {
            return false;
        }
        return fin == null || !date.isAfter(fin);
    }

    private BigDecimal heuresHebdomadaires(RegimeTravail regime, Contrat contrat) {
        if (regime != null && regime.getHeuresHebdomadaires() != null
                && regime.getHeuresHebdomadaires().signum() > 0) {
            return regime.getHeuresHebdomadaires();
        }
        if (contrat != null && contrat.getPoste() != null
                && contrat.getPoste().getDureeHebdo() != null
                && contrat.getPoste().getDureeHebdo() > 0) {
            return BigDecimal.valueOf(contrat.getPoste().getDureeHebdo());
        }
        return null;
    }

    private BigDecimal heuresPrevues(HoraireTravail horaire, BigDecimal heuresHebdo) {
        if (horaire == null || horaire.getHeureDebut() == null || horaire.getHeureFin() == null) {
            if (heuresHebdo == null) {
                return BigDecimal.ZERO;
            }
            return heuresHebdo.divide(CINQ, 2, RoundingMode.HALF_UP);
        }
        long minutes = Duration.between(horaire.getHeureDebut(), horaire.getHeureFin()).toMinutes();
        long pause = horaire.getPauseMinutes() == null ? 0 : horaire.getPauseMinutes();
        long nettes = Math.max(0, minutes - pause);
        return BigDecimal.valueOf(nettes).divide(SOIXANTE, 2, RoundingMode.HALF_UP);
    }

    private Absence absencePour(List<Absence> absences, LocalDate date) {
        for (Absence absence : absences) {
            if (!date.isBefore(absence.getDateDebut()) && !date.isAfter(absence.getDateFin())) {
                return absence;
            }
        }
        return null;
    }

    private boolean absenceSansMaintien(Absence absence) {
        if (absence.getStatut() == StatutAbsence.NON_JUSTIFIEE) {
            return true;
        }
        return absence.getTypeConge() == null
                || !Boolean.TRUE.equals(absence.getTypeConge().getPaye());
    }

    private BigDecimal facteurJour(Absence absence, LocalDate date) {
        if (date.equals(absence.getDateDebut()) && absence.getDemiJourDebut() != null) {
            return DEMI;
        }
        if (date.equals(absence.getDateFin()) && absence.getDemiJourFin() != null) {
            return DEMI;
        }
        return BigDecimal.ONE;
    }

    private BigDecimal joursOuvres(LocalDate debut, LocalDate fin) {
        int total = 0;
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() <= 5 && !jourFerieService.estFerie(date)) {
                total++;
            }
        }
        if (total == 0) {
            return JOURS_MOIS_DEFAUT;
        }
        return BigDecimal.valueOf(total);
    }

    private BigDecimal minutesEnHeures(int minutes) {
        return BigDecimal.valueOf(Math.max(0, minutes))
                .divide(SOIXANTE, 2, RoundingMode.HALF_UP);
    }

    private String nomComplet(Employe employe) {
        if (employe.getCandidature() == null || employe.getCandidature().getCandidat() == null) {
            return employe.getMatricule();
        }
        String prenom = employe.getCandidature().getCandidat().getPrenom();
        String nom = employe.getCandidature().getCandidat().getNom();
        return ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
    }

    private BigDecimal nz(BigDecimal valeur) {
        return valeur == null ? BigDecimal.ZERO : valeur;
    }

    private BigDecimal div(BigDecimal numerateur, BigDecimal denominateur) {
        if (denominateur == null || denominateur.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return numerateur.divide(denominateur, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale2(BigDecimal valeur) {
        return nz(valeur).setScale(2, RoundingMode.HALF_UP);
    }
}
