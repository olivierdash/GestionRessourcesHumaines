package com.example.sirh.controller.api.v1;

import com.example.sirh.model.DemandeExplication;
import com.example.sirh.repository.DemandeExplicationReposiroty;
import com.example.sirh.service.ReponseEtDecisionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/discipline")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3010")
public class PublicDisciplineController {
    private final DemandeExplicationReposiroty demandeRepository;
    private final ReponseEtDecisionService decisionService;

    @GetMapping("/mes-demandes")
    public List<DemandePubliqueDto> mesDemandes(@RequestParam Integer idCandidat) {
        return demandeRepository.findByIncidentEmployeCandidatureCandidatIdCandidat(idCandidat)
                .stream().map(this::versDto).toList();
    }

    @PostMapping("/demandes/{id}/reponse")
    public ResponseEntity<?> repondre(@PathVariable Integer id,
                                      @Valid @RequestBody ReponsePubliqueRequest request) {
        DemandeExplication demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Demande introuvable"));
        Integer idCandidatEmploye = demande.getIncident().getEmploye().getCandidature().getCandidat().getIdCandidat();
        if (!idCandidatEmploye.equals(request.idCandidat()))
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        decisionService.enregistrerReponse(demande, request.contenu());
        return ResponseEntity.ok(Map.of("message", "Réponse enregistrée."));
    }

    private DemandePubliqueDto versDto(DemandeExplication demande) {
        var decision = demande.getReponseEtDecision();
        return new DemandePubliqueDto(
                demande.getIdDemande(),
                demande.getMotif(),
                demande.getDateEnvoi(),
                demande.getDateLimiteReponse(),
                demande.getIncident().getStatutInstruction(),
                decision == null ? null : decision.getContenuReponse(),
                decision == null ? null : decision.getTypeSanction(),
                decision == null ? null : decision.getCommentaireDecision());
    }

    public record ReponsePubliqueRequest(
            Integer idCandidat,
            @NotBlank String contenu) {}

    public record DemandePubliqueDto(
            Integer id,
            String motif,
            LocalDate dateEnvoi,
            LocalDate dateLimiteReponse,
            String statut,
            String reponse,
            String typeSanction,
            String commentaireDecision) {}
}
