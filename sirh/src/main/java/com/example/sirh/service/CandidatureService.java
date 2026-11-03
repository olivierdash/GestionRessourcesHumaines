package com.example.sirh.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.sirh.model.Candidat;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.StatutCandidature;
import com.example.sirh.repository.CandidatureRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @author vox CandidatureService
 */
@Service
@AllArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepo;

    private final CandidatService candidatService;
    private final OffreEmploieService offreService;
    private final StatutCandidatureService statutCandidatureService;

    public List<Candidature> listeCandidatureByOffre(Integer id_offre) {
        return candidatureRepo.findByOffreIdOffre(id_offre);
    }

    public List<Candidature> listeCandidatureByCandidat(Integer id_candidat) {
        return candidatureRepo.findByCandidatIdCandidatOrderByDateDepotCandidatureDesc(id_candidat);
    }

    public Candidature getCandidatureById(Integer id_candidature) {
        return candidatureRepo.findById(id_candidature)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable avec l'id " + id_candidature));
    }

    public List<Candidature> getCandidaturesSansEmploye() {
        return candidatureRepo.findCandidaturesSansEmploye();
    }

    @Transactional
    public void changerStatutCandidature(Integer id_candidature, Integer id_nouveau_statut) {
        Candidature candidature = this.getCandidatureById(id_candidature);
        StatutCandidature nouveauStatut = statutCandidatureService.getById(id_nouveau_statut);
        candidature.setStatutCandidature(nouveauStatut);
        candidatureRepo.save(candidature);
    }

    /**
     * Site public : postuler à une offre.
     * Le candidat est authentifié (session) — plus de création à la volée.
     */
    @Transactional
    public Candidature postulerOffrePublique(Integer idOffre, Integer idCandidat, MultipartFile cvFile) {

        // 1. Charger le candidat authentifié
        Candidat candidat = candidatService.getCandidatById(idCandidat);

        // 2. Vérifier l'offre
        OffreEmploi offre = offreService.getOffreEmploiById(idOffre);
        if (!"PUBLIEE".equalsIgnoreCase(offre.getStatut().getLibelle())) {
            throw new RuntimeException("Cette offre n'est plus ouverte aux candidatures.");
        }

        // 3. Vérifier le doublon
        if (candidatureRepo.existsByCandidatIdCandidatAndOffreIdOffre(idCandidat, idOffre)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre.");
        }

        // 4. CV obligatoire
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("Le CV est obligatoire.");
        }

        // 5. Sauvegarder le CV sur le serveur
        String nomOriginal = Paths.get(cvFile.getOriginalFilename()).getFileName().toString();
        String nomStocke = "cv_" + UUID.randomUUID() + "_" + nomOriginal;
        String cvUrl = "/uploads/" + nomStocke;

        try {
            Path dossier = Paths.get("uploads");
            Files.createDirectories(dossier);
            Files.copy(cvFile.getInputStream(), dossier.resolve(nomStocke));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du CV", e);
        }
        candidat.setCvUrl(cvUrl);
        candidatService.save(candidat);

        // 6. Créer la candidature
        StatutCandidature statutRecu = statutCandidatureService.getByLibelle("RECU");

        Candidature candidature = new Candidature();
        candidature.setCandidat(candidat);
        candidature.setOffre(offre);
        candidature.setDemandeRecrutement(offre.getDemandeRecrutement());
        candidature.setDateDepotCandidature(LocalDateTime.now());
        candidature.setStatutCandidature(statutRecu);

        return candidatureRepo.save(candidature);
    }
}