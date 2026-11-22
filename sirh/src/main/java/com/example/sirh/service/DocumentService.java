package com.example.sirh.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.sirh.model.Document;
import com.example.sirh.model.Employe;
import com.example.sirh.model.OnboardingTache;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DocumentRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TypeDocumentService typeDocumentService;
    private final EmployeService employeService;
    private final UtilisateurService utilisateurService;
    private final OnboardingTacheService onboardingTacheService;
    private final Path dossier;

    public DocumentService(@Value("${app.upload.dir:uploads}") String dossierUpload,
            DocumentRepository documentRepository,
            TypeDocumentService typeDocumentService,
            EmployeService employeService,
            UtilisateurService utilisateurService,
            OnboardingTacheService onboardingTacheService) {
        this.dossier = Paths.get(dossierUpload).toAbsolutePath().normalize();
        this.documentRepository = documentRepository;
        this.typeDocumentService = typeDocumentService;
        this.employeService = employeService;
        this.utilisateurService = utilisateurService;
        this.onboardingTacheService = onboardingTacheService;
    }

    @Transactional
    public Document deposer(Integer idEmploye, Integer idTypeDocument, Integer idTache,
            Integer idDeposePar, MultipartFile fichier) {

        Employe employe = employeService.getById(idEmploye);
        TypeDocument type = typeDocumentService.getById(idTypeDocument);
        Utilisateur deposePar = utilisateurService.getById(idDeposePar);

        Document doc = new Document();
        doc.setEmploye(employe);
        doc.setTypeDocument(type);
        doc.setDeposePar(deposePar);
        doc.setDateDepot(LocalDateTime.now());

        if (idTache != null) {
            OnboardingTache tache = onboardingTacheService.getById(idTache);
            doc.setTache(tache);
        }

        doc.setNomFichier(Paths.get(fichier.getOriginalFilename()).getFileName().toString());
        doc.setFichierUrl(stocker(fichier));

        return documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public boolean existsByTacheIdAndTypeDocumentLibelle(Integer idTache, String libelle) {
        return documentRepository.existsByTacheIdAndTypeDocumentLibelle(idTache, libelle);
    }

    @Transactional(readOnly = true)
    public List<Document> documentsDe(Integer idEmploye) {
        return documentRepository.findByEmployeIdEmployerOrderByDateDepotDesc(idEmploye);
    }

    @Transactional(readOnly = true)
    public Document getById(Integer idDocument) {
        return documentRepository.findById(idDocument)
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable : " + idDocument));
    }

    public Resource charger(Document doc) {
        Path chemin = dossier.resolve(doc.getFichierUrl()).normalize();
        if (!chemin.startsWith(dossier) || !Files.exists(chemin)) {
            throw new IllegalStateException("Fichier introuvable sur le disque");
        }
        return new FileSystemResource(chemin);
    }

    @Transactional(readOnly = true)
    public List<TypeDocument> getAllTypesDocument() {
        return typeDocumentService.getAllTypes();
    }

    // Range le fichier sur le disque et renvoie le nom sous lequel il est stocké
    private String stocker(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier envoyé");
        }

        String nomOriginal = Paths.get(fichier.getOriginalFilename()).getFileName().toString();
        String nomStocke = UUID.randomUUID() + "_" + nomOriginal;

        try {
            Files.createDirectories(dossier);
            Files.copy(fichier.getInputStream(), dossier.resolve(nomStocke));
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier", e);
        }
        return nomStocke;
    }
}