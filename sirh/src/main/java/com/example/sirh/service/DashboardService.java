package com.example.sirh.service;

import com.example.sirh.dto.ChartPointDto;
import com.example.sirh.dto.DashboardDto;
import com.example.sirh.dto.DemandeResumeDto;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.repository.OffreEmploieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DemandeRecrutementRepository demandeRepo;
    private final OffreEmploieRepository offreRepo;
    private final CandidatureRepository candidatureRepo;

    @Transactional(readOnly = true)
    public DashboardDto getDashboardData(Utilisateur utilisateur, int limit) {
        String role = utilisateur.getRole().getLibelle().toUpperCase();
        Integer idManager = "MANAGER".equals(role) ? utilisateur.getIdUtilisateur() : null;

        // 1. Détermination des états à surveiller pour le KPI 1
        List<String> etatsAValider = switch (role) {
            case "RH" -> List.of("Cree");
            case "FINANCE" -> List.of("ValideRH");
            case "DG" -> List.of("Cree", "ValideRH", "ValideFinance");
            case "MANAGER" -> List.of("Cree", "ValideRH", "ValideFinance");
            default -> List.of();
        };

        // 2. KPI 1 : Demandes en cours
        long demandesEnCours = (idManager == null)
                ? demandeRepo.countByEtatDemandeIntituleIn(etatsAValider)
                : demandeRepo.countByEtatDemandeIntituleInAndManagerIdUtilisateur(etatsAValider, idManager);

        // 3. KPI 3 : Postes ouverts
        long postesOuverts = (idManager == null)
                ? offreRepo.countByStatutLibelle("PUBLIEE")
                : offreRepo.countByStatutLibelleAndDemandeRecrutementManagerIdUtilisateur("PUBLIEE", idManager);

        // 4. KPI 4 : Candidatures (7 derniers jours)
        LocalDateTime ilYa7Jours = LocalDateTime.now().minusDays(7);
        long candidaturesSemaine = (idManager == null)
                ? candidatureRepo.countByDateDepotCandidatureAfter(ilYa7Jours)
                : candidatureRepo.countByDateDepotCandidatureAfterAndOffreDemandeRecrutementManagerIdUtilisateur(ilYa7Jours, idManager);

        // 5. Données du graphique (30 derniers jours)
        LocalDateTime ilYa30Jours = LocalDateTime.now().minusDays(30);
        List<Candidature> candidatures30Jours = (idManager == null)
                ? candidatureRepo.findByDateDepotCandidatureAfter(ilYa30Jours)
                : candidatureRepo.findByDateDepotCandidatureAfterAndOffreDemandeRecrutementManagerIdUtilisateur(ilYa30Jours, idManager);

        List<ChartPointDto> chartData = new ArrayList<>();
        Map<String, Long> groupByDate = candidatures30Jours.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDateDepotCandidature().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));
        groupByDate.forEach((date, count) -> chartData.add(new ChartPointDto(date, count)));

        // 6. Table : Dernières demandes (avec limite dynamique)
        Pageable limite = PageRequest.of(0, limit);
        List<DemandeRecrutement> dernieres = (idManager == null)
                ? demandeRepo.findAllByOrderByDateCreationDesc(limite)
                : demandeRepo.findByManagerIdUtilisateurOrderByDateCreationDesc(idManager, limite);

        List<DemandeResumeDto> dernieresDto = dernieres.stream()
                .map(d -> DemandeResumeDto.builder()
                        .id(d.getIdDemandeRecrutement())
                        .reference("#REC-" + d.getIdDemandeRecrutement())
                        .poste(d.getPoste().getIntitule())
                        .departement(d.getPoste().getDepartement().getTitre())
                        .dateCreation(d.getDateCreation())
                        .statut(d.getEtatDemande().getIntitule())
                        .build())
                .collect(Collectors.toList());

        return DashboardDto.builder()
                .demandesEnCours(demandesEnCours)
                .postesOuverts(postesOuverts)
                .candidaturesSemaine(candidaturesSemaine)
                .chartData(chartData)
                .dernieresDemandes(dernieresDto)
                .build();
    }
}