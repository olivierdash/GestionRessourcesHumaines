package com.example.sirh.service;

import com.example.sirh.dto.ChartPointDto;
import com.example.sirh.dto.FunnelStepDto;
import com.example.sirh.dto.HiringDelayDto;
import com.example.sirh.dto.OnboardingStatutCountDto;
import com.example.sirh.dto.RHDashboardDto;
import com.example.sirh.model.Candidature;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.OffreEmploieRepository;
import com.example.sirh.repository.OnboardingRepository;
import com.example.sirh.repository.OnboardingTacheRepository;
import com.example.sirh.repository.PropositionEmbaucheRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RHDashboardService {

    private final DemandeRecrutementRepository demandeRepo;
    private final ContratRepository contratRepo;
    private final OnboardingRepository onboardingRepo;
    private final OffreEmploieRepository offreRepo;
    private final CandidatureRepository candidatureRepo;
    private final PropositionEmbaucheRepository propositionRepo;
    private final EmployeRepository employeRepo;
    private final OnboardingTacheRepository onboardingTacheRepo;

    @Transactional(readOnly = true)
    public RHDashboardDto getRHDashboardData(String period) {
        PeriodRange range = resolvePeriod(period);

        long demandesAValider = demandeRepo.countByEtatAndDateRange("Cree", null, null);

        LocalDate today = LocalDate.now();
        long contratsARenouveler = contratRepo.countByStatutLibelleAndDateFinBetween(
                "actif", today, today.plusDays(30));

        long onboardingsEnCours = onboardingRepo.countByDateFinIsNull();
        double progressionMoyenne = computeOnboardingsProgressionMoyenne();

        // Chargement unique : funnel + time-to-fill partagent la même liste
        List<HiringDelayDto> hiringData = employeRepo.findHiringDatesAndDemandCreation(
                range.start(), range.end());

        List<FunnelStepDto> funnel = buildFunnel(range.start(), range.end(), hiringData);
        Double timeToFill = computeTimeToFill(hiringData);
        List<ChartPointDto> chartData = buildChartData(range.start(), range.end());

        return RHDashboardDto.builder()
                .demandesAValider(demandesAValider)
                .contratsARenouvelerSous30j(contratsARenouveler)
                .onboardingsEnCours(onboardingsEnCours)
                .onboardingsProgressionMoyenne(progressionMoyenne)
                .funnel(funnel)
                .timeToFillMoyen(timeToFill)
                .chartData(chartData)
                .period(period)
                .periodLabel(range.label())
                .build();
    }

    private PeriodRange resolvePeriod(String period) {
        LocalDate today = LocalDate.now();

        return switch (period == null ? "30d" : period) {
            case "7d" -> new PeriodRange(today.minusDays(7), today, "7 derniers jours");
            case "trimestre" -> {
                int month = today.getMonthValue();
                int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                LocalDate quarterStart = LocalDate.of(today.getYear(), quarterStartMonth, 1);
                yield new PeriodRange(quarterStart, today, "Ce trimestre");
            }
            case "annee" -> new PeriodRange(
                    LocalDate.of(today.getYear(), 1, 1),
                    today,
                    "Cette année");
            default -> new PeriodRange(today.minusDays(30), today, "30 derniers jours");
        };
    }

    private List<FunnelStepDto> buildFunnel(LocalDate start, LocalDate end, List<HiringDelayDto> hiringData) {
        long demandesValidees = demandeRepo.countByEtatAndDateRange(
                "Valide", start.atStartOfDay(), end.atTime(LocalTime.MAX));

        long offresPubliees = offreRepo.countByDatePublicationBetween(
                start.atStartOfDay(), end.atTime(LocalTime.MAX));

        long candidaturesRecues = candidatureRepo.countByPeriodWithFilters(
                start.atStartOfDay(), end.atTime(LocalTime.MAX), null, null);

        long candidaturesEvaluees = candidatureRepo.countByPeriodWithFilters(
                start.atStartOfDay(), end.atTime(LocalTime.MAX), null, "RECU");

        long propositionsEmises = propositionRepo.countByDatePropositionBetween(
                start.atStartOfDay(), end.atTime(LocalTime.MAX));

        long embauches = hiringData.size();

        List<FunnelStepDto> funnel = new ArrayList<>();
        funnel.add(FunnelStepDto.builder()
                .label("Demandes validées").count(demandesValidees).tauxConversion(null).build());
        funnel.add(buildStep("Offres publiées", offresPubliees, demandesValidees));
        funnel.add(buildStep("Candidatures reçues", candidaturesRecues, offresPubliees));
        funnel.add(buildStep("Candidatures évaluées", candidaturesEvaluees, candidaturesRecues));
        funnel.add(buildStep("Propositions émises", propositionsEmises, candidaturesEvaluees));
        funnel.add(buildStep("Embauches effectives", embauches, propositionsEmises));

        return funnel;
    }

    private FunnelStepDto buildStep(String label, long count, long previous) {
        Double taux = (previous > 0)
                ? Math.round((count * 1000.0) / previous) / 10.0
                : null;
        return FunnelStepDto.builder()
                .label(label).count(count).tauxConversion(taux).build();
    }

    private Double computeTimeToFill(List<HiringDelayDto> hiringData) {
        if (hiringData.isEmpty()) {
            return null;
        }
        return hiringData.stream()
                .mapToLong(r -> ChronoUnit.DAYS.between(
                        r.getDateCreation().toLocalDate(), r.getHiringDate()))
                .average()
                .orElse(Double.NaN);
    }

    private double computeOnboardingsProgressionMoyenne() {
        List<OnboardingStatutCountDto> rows = onboardingTacheRepo.countTachesByActiveOnboardingAndStatut();
        if (rows.isEmpty()) {
            return 0.0;
        }

        Map<Integer, Long> totals = new HashMap<>();
        Map<Integer, Long> terminees = new HashMap<>();

        for (OnboardingStatutCountDto row : rows) {
            Integer id = row.getIdOnboarding();
            long count = row.getCount() != null ? row.getCount() : 0L;

            totals.merge(id, count, Long::sum);
            if ("terminée".equals(row.getStatutLibelle())) {
                terminees.merge(id, count, Long::sum);
            }
        }

        return totals.entrySet().stream()
                .mapToDouble(e -> {
                    long total = e.getValue();
                    if (total == 0) return 0.0;
                    long done = terminees.getOrDefault(e.getKey(), 0L);
                    return done * 100.0 / total;
                })
                .average()
                .orElse(0.0);
    }

    private List<ChartPointDto> buildChartData(LocalDate start, LocalDate end) {
        List<Candidature> candidatures = candidatureRepo.findByPeriodWithFilters(
                start.atStartOfDay(), end.atTime(LocalTime.MAX), null);

        Map<String, Long> groupByDate = candidatures.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDateDepotCandidature().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        List<ChartPointDto> result = new ArrayList<>();
        groupByDate.forEach((date, count) -> result.add(new ChartPointDto(date, count)));
        return result;
    }

    private record PeriodRange(LocalDate start, LocalDate end, String label) {}
}