package com.example.sirh.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sirh.model.Document;
import com.example.sirh.model.Employe;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DocumentRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.OnboardingTacheRepository;
import com.example.sirh.repository.TypeDocumentRepository;
import com.example.sirh.repository.UtilisateurRepository;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OnboardingTacheRepository tacheRepository;
    private final Path dossier;

    public DocumentService(@Value("${app.upload.dir:uploads}") String dossierUpload,
            DocumentRepository documentRepository,
            TypeDocumentRepository typeDocumentRepository,
            EmployeRepository employeRepository,
            UtilisateurRepository utilisateurRepository,
            OnboardingTacheRepository tacheRepository) {
        this.dossier = Paths.get(dossierUpload).toAbsolutePath().normalize();
        this.documentRepository = documentRepository;
        this.typeDocumentRepository = typeDocumentRepository;
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.tacheRepository = tacheRepository;
    }

    @Transactional
    public Document deposer(Integer idEmploye, Integer idTypeDocument, Integer idTache,
            Integer idDeposePar, MultipartFile fichier) {

        Employe employe = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable"));
        TypeDocument type = typeDocumentRepository.findById(idTypeDocument)
                .orElseThrow(() -> new IllegalArgumentException("Type de document introuvable"));
        Utilisateur deposePar = utilisateurRepository.findById(idDeposePar)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Document doc = new Document();
        doc.setEmploye(employe);
        doc.setTypeDocument(type);
        doc.setDeposePar(deposePar);
        doc.setDateDepot(LocalDateTime.now());

        if (idTache != null) {
            doc.setTache(tacheRepository.findById(idTache)
                    .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable")));
        }

        doc.setNomFichier(Paths.get(fichier.getOriginalFilename()).getFileName().toString());
        doc.setFichierUrl(stocker(fichier));

        return documentRepository.save(doc);
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