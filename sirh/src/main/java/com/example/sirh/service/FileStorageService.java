package com.example.sirh.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralise l'accès aux fichiers déposés dans le dossier d'upload
 * (CV, documents du dossier salarié, justificatifs de congé...).
 *
 * <p>Le service empêche toute sortie du dossier racine (path traversal)
 * et prépare les réponses HTTP en distinguant l'affichage dans le
 * navigateur (inline) du téléchargement (attachment).</p>
 */
@Service
public class FileStorageService {

    private static final Map<String, MediaType> TYPES_CONNUS = Map.ofEntries(
            Map.entry("pdf", MediaType.APPLICATION_PDF),
            Map.entry("png", MediaType.IMAGE_PNG),
            Map.entry("jpg", MediaType.IMAGE_JPEG),
            Map.entry("jpeg", MediaType.IMAGE_JPEG),
            Map.entry("gif", MediaType.IMAGE_GIF),
            Map.entry("webp", MediaType.parseMediaType("image/webp")),
            Map.entry("bmp", MediaType.parseMediaType("image/bmp")),
            Map.entry("txt", MediaType.TEXT_PLAIN),
            Map.entry("csv", MediaType.parseMediaType("text/csv")),
            Map.entry("doc", MediaType.parseMediaType("application/msword")),
            Map.entry("docx", MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", MediaType.parseMediaType("application/vnd.ms-excel")),
            Map.entry("xlsx", MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("ppt", MediaType.parseMediaType("application/vnd.ms-powerpoint")),
            Map.entry("pptx", MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation")));

    private final Path racine;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String dossierUpload) {
        this.racine = Paths.get(dossierUpload).toAbsolutePath().normalize();
    }

    public Path getRacine() {
        return racine;
    }

    /**
     * Transforme un chemin stocké en base ({@code cv_uuid_fichier.pdf},
     * {@code conges/uuid.pdf} ou {@code /uploads/...}) en chemin réel sécurisé.
     */
    public Path resoudre(String chemin) {
        String relatif = normaliser(chemin);
        Path fichier = racine.resolve(relatif).normalize();

        if (!fichier.startsWith(racine) || !Files.isRegularFile(fichier)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
        }
        return fichier;
    }

    public Resource charger(String chemin) {
        return new FileSystemResource(resoudre(chemin));
    }

    /**
     * Prépare une réponse HTTP pour un fichier stocké.
     *
     * @param chemin      chemin relatif en base ou URL {@code /uploads/...}
     * @param nomAffiche  nom présenté au navigateur
     * @param apercu      true pour afficher dans le navigateur si possible,
     *                    false pour forcer le téléchargement
     */
    public ResponseEntity<Resource> repondre(String chemin, String nomAffiche, boolean apercu) {
        return repondre(charger(chemin), nomAffiche, apercu, null);
    }

    /**
     * Variante utilisée quand le contrôleur a déjà chargé la ressource après
     * avoir vérifié les droits d'accès (ex. justificatif de congé).
     */
    public ResponseEntity<Resource> repondre(Resource ressource, String nomAffiche,
                                             boolean apercu, String typeMimeIndice) {
        MediaType type = detecterType(ressource, typeMimeIndice);
        boolean affichageInline = apercu && estApercuable(type);

        ContentDisposition disposition = (affichageInline
                ? ContentDisposition.inline()
                : ContentDisposition.attachment())
                .filename(nomAffiche == null || nomAffiche.isBlank() ? "document" : nomAffiche,
                        StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(type)
                .body(ressource);
    }

    private String normaliser(String chemin) {
        if (chemin == null || chemin.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
        }

        String valeur = chemin.trim().replace('\\', '/');

        // Retire une éventuelle query string ou ancre.
        int query = valeur.indexOf('?');
        if (query >= 0) {
            valeur = valeur.substring(0, query);
        }
        int ancre = valeur.indexOf('#');
        if (ancre >= 0) {
            valeur = valeur.substring(0, ancre);
        }

        // Accepte aussi une URL complète (anciennes valeurs en base).
        if (valeur.startsWith("http://") || valeur.startsWith("https://")) {
            try {
                String path = URI.create(valeur).getPath();
                valeur = path == null ? "" : path;
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
            }
        }

        if (valeur.startsWith("/uploads/")) {
            valeur = valeur.substring("/uploads/".length());
        } else if (valeur.startsWith("uploads/")) {
            valeur = valeur.substring("uploads/".length());
        }

        while (valeur.startsWith("/")) {
            valeur = valeur.substring(1);
        }

        if (valeur.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
        }
        return valeur;
    }

    private MediaType detecterType(Resource ressource, String typeMimeIndice) {
        String extension = extension(ressource.getFilename());
        MediaType typeConnu = TYPES_CONNUS.get(extension);
        if (typeConnu != null) {
            return typeConnu;
        }

        if (typeMimeIndice != null && !typeMimeIndice.isBlank()) {
            try {
                return MediaType.parseMediaType(typeMimeIndice);
            } catch (InvalidMediaTypeException ignored) {
                // On tente la détection suivante.
            }
        }

        try {
            String typeProbe = Files.probeContentType(ressource.getFile().toPath());
            if (typeProbe != null && !typeProbe.isBlank()) {
                return MediaType.parseMediaType(typeProbe);
            }
        } catch (IOException | InvalidMediaTypeException ignored) {
            // Type inconnu : téléchargement forcé.
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private boolean estApercuable(MediaType type) {
        if (type == null) {
            return false;
        }
        if (MediaType.APPLICATION_PDF.isCompatibleWith(type)) {
            return true;
        }
        if ("image".equalsIgnoreCase(type.getType())) {
            // Le SVG est volontairement exclu : il peut contenir du script.
            return !"svg".equalsIgnoreCase(type.getSubtype());
        }
        return "text".equalsIgnoreCase(type.getType())
                && "plain".equalsIgnoreCase(type.getSubtype());
    }

    private String extension(String nomFichier) {
        if (nomFichier == null) {
            return "";
        }
        int point = nomFichier.lastIndexOf('.');
        if (point < 0 || point == nomFichier.length() - 1) {
            return "";
        }
        return nomFichier.substring(point + 1).toLowerCase(Locale.ROOT);
    }
}
