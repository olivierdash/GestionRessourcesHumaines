package com.example.sirh.controller.api.v1;

import com.example.sirh.dto.OffrePublicDto;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.OffreEmploieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3010")
public class JobController {

    private final OffreEmploieService offreService;
    private final CandidatureService candidatureService;

    /**
     * Liste des offres PUBLIEES.
     * Step 1 : renvoie la liste complète. Pagination côté serveur en Step 2.
     */
    @GetMapping
    public List<OffrePublicDto> list() {
        return offreService.getOffresPubliees();
    }

    /**
     * Détail d'une offre PUBLIEE.
     * Si introuvable ou non publiée, GlobalExceptionHandler renvoie du JSON 400/404.
     */
    @GetMapping("/{id}")
    public OffrePublicDto detail(@PathVariable Integer id) {
        return offreService.getOffrePubliqueById(id);
    }

    /**
     * Postuler à une offre.
     * L'identité du candidat est fournie par le client (idCandidat) —
     * l'API est stateless, aucune session serveur n'est gérée.
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, String>> apply(
            @PathVariable Integer id,
            @RequestParam("idCandidat") Integer idCandidat,
            @RequestParam("cv") MultipartFile cv) {
        try {
            candidatureService.postulerOffrePublique(id, idCandidat, cv);
            return ResponseEntity.ok(Map.of("message", "Candidature envoyée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}