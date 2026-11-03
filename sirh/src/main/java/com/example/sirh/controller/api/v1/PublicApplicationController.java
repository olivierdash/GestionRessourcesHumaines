package com.example.sirh.controller.api.v1;

import com.example.sirh.dto.CandidaturePublicDto;
import com.example.sirh.model.Candidature;
import com.example.sirh.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/public/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PublicApplicationController {

    private final CandidatureService candidatureService;

    @GetMapping
    public List<CandidaturePublicDto> listMine(@RequestParam Integer idCandidat) {
        return candidatureService
                .listeCandidatureByCandidat(idCandidat)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public CandidaturePublicDto detail(@PathVariable Integer id,
                                       @RequestParam Integer idCandidat) {
        Candidature c = candidatureService.getCandidatureById(id);
        if (!c.getCandidat().getIdCandidat().equals(idCandidat)) {
            // 404 et non 403 : on ne révèle pas l'existence d'une candidature d'autrui.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return toDto(c);
    }

    private CandidaturePublicDto toDto(Candidature c) {
        return CandidaturePublicDto.builder()
                .id(c.getIdCandidature())
                .dateDepot(c.getDateDepotCandidature())
                .statut(c.getStatutCandidature().getLibelle())
                .offreId(c.getOffre().getIdOffre())
                .offreTitre(c.getOffre().getTitre())
                .poste(c.getDemandeRecrutement().getPoste().getIntitule())
                .departement(c.getDemandeRecrutement().getPoste().getDepartement().getTitre())
                .build();
    }
}