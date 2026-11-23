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
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sirh.model.Document;
import com.example.sirh.service.DocumentService;
import com.example.sirh.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    /**
     * Affiche le document dans le navigateur (PDF, image...) sans le télécharger.
     */
    @GetMapping("/{id}/apercu")
    public ResponseEntity<Resource> apercu(@PathVariable Integer id) {
        Document doc = documentService.getById(id);
        return fileStorageService.repondre(doc.getFichierUrl(), doc.getNomFichier(), true);
    }

    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telecharger(@PathVariable Integer id) {
        Document doc = documentService.getById(id);
        Resource fichier = documentService.charger(doc);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(doc.getNomFichier(), StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }
}