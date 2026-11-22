package com.example.sirh.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.JustificatifConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.FileStorageService;
import com.example.sirh.service.JustificatifCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/justificatifs")
@RequiredArgsConstructor
public class JustificatifCongeController {

    private final JustificatifCongeService justificatifCongeService;
    private final CongeAccessService accessService;
    private final FileStorageService fileStorageService;

    /**
     * Affiche le justificatif dans le navigateur (PDF ou image).
     * L'accès est vérifié par {@link JustificatifCongeService#charger}.
     */
    @GetMapping("/{id}/apercu")
    public ResponseEntity<Resource> apercu(@PathVariable Integer id) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        JustificatifConge justificatif = justificatifCongeService.getById(id);
        Resource resource = justificatifCongeService.charger(id, utilisateur);
        return fileStorageService.repondre(resource, justificatif.getNomFichier(),
                true, justificatif.getTypeMime());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> telecharger(@PathVariable Integer id) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        JustificatifConge justificatif = justificatifCongeService.getById(id);
        Resource resource = justificatifCongeService.charger(id, utilisateur);

        String mime = justificatif.getTypeMime() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE : justificatif.getTypeMime();
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(justificatif.getNomFichier(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(mime))
                .body(resource);
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id, RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            JustificatifConge justificatif = justificatifCongeService.valider(id, utilisateur);
            redirect.addFlashAttribute("succes", "Justificatif validé.");
            return "redirect:" + retour(justificatif, utilisateur);
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:" + retour(justificatifCongeService.getById(id), utilisateur);
    }

    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Integer id,
                          @RequestParam(required = false) String commentaire,
                          RedirectAttributes redirect) {
        Utilisateur utilisateur = accessService.utilisateurCourant();
        try {
            JustificatifConge justificatif =
                    justificatifCongeService.refuser(id, commentaire, utilisateur);
            redirect.addFlashAttribute("succes", "Justificatif refusé.");
            return "redirect:" + retour(justificatif, utilisateur);
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:" + retour(justificatifCongeService.getById(id), utilisateur);
    }

    private String retour(JustificatifConge justificatif, Utilisateur utilisateur) {
        boolean staff = accessService.estRhOuDg(utilisateur)
                || accessService.estManager(utilisateur);
        if (justificatif.getDemandeConge() != null) {
            Integer id = justificatif.getDemandeConge().getId();
            return staff ? "/conges/demandes/" + id : "/mes-conges/" + id;
        }
        Integer id = justificatif.getAbsence() == null ? null : justificatif.getAbsence().getId();
        return staff ? "/conges/absences/" + id : "/mes-conges/absences";
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
