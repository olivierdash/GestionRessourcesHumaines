package com.example.sirh.controller.api.v1;

import com.example.sirh.dto.MessageCandidatRequestDto;
import com.example.sirh.dto.MessagePubliqueDto;
import com.example.sirh.dto.PropositionDetailPubliqueDto;
import com.example.sirh.dto.PropositionPubliqueDto;
import com.example.sirh.dto.ReponseCandidatRequestDto;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.PropositionMessage;
import com.example.sirh.service.PropositionMessageService;
import com.example.sirh.service.PropositionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.sirh.constants.ConstantSirh.Statut;

@RestController
@RequestMapping("/api/public/propositions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3010")
public class PublicPropositionController {

    private final PropositionService propositionService;
    private final PropositionMessageService propositionMessageService;

    @GetMapping
    public ResponseEntity<List<PropositionPubliqueDto>> list(@RequestParam Integer idCandidat) {
        List<PropositionPubliqueDto> dtos = propositionService
                .listerPourCandidat(idCandidat)
                .stream()
                .map(this::toResumeDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropositionDetailPubliqueDto> detail(
            @PathVariable Integer id,
            @RequestParam Integer idCandidat) {
        PropositionEmbauche p = propositionService.getPourCandidat(id, idCandidat);
        List<PropositionMessage> messages = propositionMessageService.lister(id);

        String statut = p.getStatut().getLibelle();
        boolean actionRequise = Statut.APPROUVEE.equals(statut);

        return ResponseEntity.ok(PropositionDetailPubliqueDto.builder()
                .idProposition(p.getIdProposition())
                .offreTitre(p.getCandidature().getOffre().getTitre())
                .poste(p.getCandidature().getDemandeRecrutement().getPoste().getIntitule())
                .departement(p.getCandidature().getDemandeRecrutement().getPoste().getDepartement().getTitre())
                .salairePropose(p.getSalairePropose())
                .statut(statut)
                .dateProposition(p.getDateProposition())
                .dateExpiration(p.getDateExpiration())
                .dateDecision(p.getDateDecision())
                .remarque(p.getRemarque())
                .messages(messages.stream().map(this::toMessageDto).toList())
                .actionRequise(actionRequise)
                .build());
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> envoyerMessage(
            @PathVariable Integer id,
            @RequestBody MessageCandidatRequestDto request) {
        propositionMessageService.envoyerParCandidat(
                id,
                request.getIdCandidat(),
                request.getContenu(),
                request.getSalairePropose());
        return ResponseEntity.ok(Map.of("message", "Message envoyé."));
    }

    @PostMapping("/{id}/repondre")
    public ResponseEntity<?> repondre(
            @PathVariable Integer id,
            @RequestBody ReponseCandidatRequestDto request) {
        String msg = propositionService.repondreCandidat(
                id,
                request.getIdCandidat(),
                request.getReponse(),
                request.getRemarque());
        return ResponseEntity.ok(Map.of("message", msg));
    }

    private PropositionPubliqueDto toResumeDto(PropositionEmbauche p) {
        return PropositionPubliqueDto.builder()
                .idProposition(p.getIdProposition())
                .offreTitre(p.getCandidature().getOffre().getTitre())
                .poste(p.getCandidature().getDemandeRecrutement().getPoste().getIntitule())
                .departement(p.getCandidature().getDemandeRecrutement().getPoste().getDepartement().getTitre())
                .salairePropose(p.getSalairePropose())
                .statut(p.getStatut().getLibelle())
                .dateProposition(p.getDateProposition())
                .dateExpiration(p.getDateExpiration())
                .build();
    }

    private MessagePubliqueDto toMessageDto(PropositionMessage m) {
        boolean deCandidat = m.getCandidat() != null;
        String auteur = deCandidat
                ? m.getCandidat().getPrenom() + " " + m.getCandidat().getNom()
                : m.getUtilisateur().getNom();
        return MessagePubliqueDto.builder()
                .id(m.getIdMessage())
                .auteur(auteur)
                .deCandidat(deCandidat)
                .contenu(m.getContenu())
                .salairePropose(m.getSalairePropose())
                .dateCreation(m.getDateCreation())
                .build();
    }
}