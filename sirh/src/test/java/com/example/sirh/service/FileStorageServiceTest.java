package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class FileStorageServiceTest {

    @TempDir
    Path dossierUpload;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(dossierUpload.toString());
    }

    @Test
    void afficheUnPdfEnLigneDepuisUneUrlUploads() throws IOException {
        Files.writeString(dossierUpload.resolve("cv_test.pdf"), "%PDF-1.4");

        ResponseEntity<Resource> reponse = service.repondre(
                "/uploads/cv_test.pdf", "CV - Jean Dupont.pdf", true);

        assertEquals(MediaType.APPLICATION_PDF, reponse.getHeaders().getContentType());
        assertTrue(reponse.getHeaders().getContentDisposition().isInline());
        assertEquals("CV - Jean Dupont.pdf",
                reponse.getHeaders().getContentDisposition().getFilename());
    }

    @Test
    void forceLeTelechargementPourUnFormatNonAfficheable() throws IOException {
        Files.writeString(dossierUpload.resolve("contrat.docx"), "contenu");

        ResponseEntity<Resource> reponse = service.repondre(
                "contrat.docx", "contrat.docx", true);

        assertTrue(reponse.getHeaders().getContentDisposition().isAttachment());
    }

    @Test
    void refuseUneSortieDuDossierUpload() {
        assertThrows(ResponseStatusException.class,
                () -> service.resoudre("../secret.pdf"));
    }

    @Test
    void refuseUnFichierAbsent() {
        assertThrows(ResponseStatusException.class,
                () -> service.resoudre("inconnu.pdf"));
    }
}
