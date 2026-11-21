package com.example.sirh.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.JustificatifConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.model.enums.StatutJustificatif;
import com.example.sirh.repository.AbsenceRepository;
import com.example.sirh.repository.DemandeCongeRepository;
import com.example.sirh.repository.JustificatifCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JustificatifCongeService {

    private static final Set<String> EXTENSIONS_AUTORISEES =
            Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> MIME_AUTORISES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/jpg");

    private final JustificatifCongeRepository justificatifRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final AbsenceRepository absenceRepository;
    private final CongeAccessService accessService;
    private final AbsenceService absenceService;
    private final AuditService auditService;

    @Value("${app.upload.dir:uploads}")
    private String dossierUpload;

    @Value("${app.upload.max-size:10485760}")
    private long tailleMax;

    @Transactional
    public JustificatifConge deposerPourDemande(Integer demandeId, MultipartFile fichier,
                                                Utilisateur utilisateur) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demande de congé introuvable : " + demandeId));
        if (!accessService.peutVoirDemande(demande, utilisateur)) {
            erreur("justificatif", "Vous n'êtes pas autorisé à déposer un justificatif ici.");
        }
        if (demande.getStatut() == StatutDemandeConge.ANNULEE
                || demande.getStatut() == StatutDemandeConge.REFUSEE) {
            erreur("justificatif", "Cette demande est clôturée.");
        }

        return enregistrer(fichier, utilisateur, demande, null);
    }

    @Transactional
    public JustificatifConge deposerPourAbsence(Integer absenceId, MultipartFile fichier,
                                                Utilisateur utilisateur) {
        Absence absence = absenceRepository.findById(absenceId)
                .orElseThrow(() -> new IllegalArgumentException("Absence introuvable : " + absenceId));
        if (!accessService.peutGererAbsence(absence, utilisateur)) {
            erreur("justificatif", "Vous n'êtes pas autorisé à déposer un justificatif ici.");
        }
        if (absence.getStatut() == StatutAbsence.ANNULEE) {
            erreur("justificatif", "Cette absence est annulée.");
        }

        return enregistrer(fichier, utilisateur, null, absence);
    }

    private JustificatifConge enregistrer(MultipartFile fichier, Utilisateur utilisateur,
                                          DemandeConge demande, Absence absence) {
        if (fichier == null || fichier.isEmpty()) {
            erreur("fichier", "Le fichier est obligatoire.");
        }
        if (fichier.getSize() > tailleMax) {
            erreur("fichier", "Le fichier dépasse la taille maximale autorisée ("
                    + (tailleMax / 1024 / 1024) + " Mo).");
        }

        String nomOriginal = fichier.getOriginalFilename() == null
                ? "justificatif" : Paths.get(fichier.getOriginalFilename())
                        .getFileName().toString();
        String extension = extension(nomOriginal);
        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            erreur("fichier", "Formats autorisés : PDF, JPEG, PNG.");
        }
        String mime = fichier.getContentType();
        if (mime != null && !mime.isBlank() && !MIME_AUTORISES.contains(mime.toLowerCase(Locale.ROOT))) {
            erreur("fichier", "Type de fichier non autorisé.");
        }

        Path dossier = Paths.get(dossierUpload).toAbsolutePath().normalize().resolve("conges");
        String nomStocke = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(dossier);
            Path cible = dossier.resolve(nomStocke).normalize();
            if (!cible.startsWith(dossier)) {
                throw new IllegalStateException("Chemin de stockage invalide.");
            }
            fichier.transferTo(cible);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le justificatif.", e);
        }

        JustificatifConge justificatif = JustificatifConge.builder()
                .demandeConge(demande)
                .absence(absence)
                .nomFichier(nomOriginal)
                .cheminStockage("conges/" + nomStocke)
                .typeMime(mime)
                .tailleOctets(fichier.getSize())
                .deposePar(utilisateur)
                .statut(StatutJustificatif.DEPOSE)
                .build();

        return justificatifRepository.save(justificatif);
    }

    @Transactional
    public JustificatifConge valider(Integer id, Utilisateur utilisateur) {
        JustificatifConge justificatif = getById(id);
        if (!accessService.peutValiderJustificatif(justificatif, utilisateur)) {
            erreur("justificatif", "Vous n'êtes pas autorisé à valider ce justificatif.");
        }
        if (justificatif.getStatut() == StatutJustificatif.VALIDE) {
            return justificatif;
        }
        justificatif.setStatut(StatutJustificatif.VALIDE);
        justificatif.setValidateur(utilisateur);
        justificatif.setDateValidation(LocalDateTime.now());
        justificatifRepository.save(justificatif);

        if (justificatif.getAbsence() != null) {
            absenceService.justifier(justificatif.getAbsence().getId(), utilisateur);
        }
        audit(utilisateur, "VALIDATION_JUSTIFICATIF", justificatif.getId(),
                "Justificatif validé");
        return justificatif;
    }

    @Transactional
    public JustificatifConge refuser(Integer id, String commentaire, Utilisateur utilisateur) {
        JustificatifConge justificatif = getById(id);
        if (!accessService.peutValiderJustificatif(justificatif, utilisateur)) {
            erreur("justificatif", "Vous n'êtes pas autorisé à refuser ce justificatif.");
        }
        justificatif.setStatut(StatutJustificatif.REFUSE);
        justificatif.setValidateur(utilisateur);
        justificatif.setDateValidation(LocalDateTime.now());
        justificatif.setCommentaire(commentaire);
        justificatifRepository.save(justificatif);
        audit(utilisateur, "REFUS_JUSTIFICATIF", justificatif.getId(),
                commentaire == null ? "Justificatif refusé" : commentaire);
        return justificatif;
    }

    @Transactional(readOnly = true)
    public JustificatifConge getById(Integer id) {
        return justificatifRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Justificatif introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<JustificatifConge> listerPourDemande(Integer demandeId) {
        return justificatifRepository.findByDemandeCongeIdOrderByDateDepotDesc(demandeId);
    }

    @Transactional(readOnly = true)
    public List<JustificatifConge> listerPourAbsence(Integer absenceId) {
        return justificatifRepository.findByAbsenceIdOrderByDateDepotDesc(absenceId);
    }

    @Transactional(readOnly = true)
    public Resource charger(Integer id, Utilisateur utilisateur) {
        JustificatifConge justificatif = getById(id);
        if (!accessService.peutVoirJustificatif(justificatif, utilisateur)) {
            erreur("justificatif", "Vous n'êtes pas autorisé à télécharger ce justificatif.");
        }
        Path racine = Paths.get(dossierUpload).toAbsolutePath().normalize();
        Path chemin = racine.resolve(justificatif.getCheminStockage()).normalize();
        if (!chemin.startsWith(racine) || !Files.exists(chemin)) {
            throw new IllegalStateException("Fichier introuvable sur le disque.");
        }
        return new FileSystemResource(chemin);
    }

    private String extension(String nom) {
        int index = nom.lastIndexOf('.');
        if (index < 0 || index == nom.length() - 1) {
            return "";
        }
        return nom.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private void erreur(String champ, String message) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        erreurs.put(champ, message);
        throw new ValidationMetierException(erreurs);
    }

    private void audit(Utilisateur utilisateur, String action, Integer id, String details) {
        if (utilisateur == null || utilisateur.getEmail() == null) {
            return;
        }
        try {
            auditService.enregistrerAction(utilisateur.getEmail(), action,
                    "justificatif_conge", id, details);
        } catch (RuntimeException ignored) {
            // L'audit ne doit pas bloquer l'opération.
        }
    }
}
