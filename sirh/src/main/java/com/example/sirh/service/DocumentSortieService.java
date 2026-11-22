package com.example.sirh.service;

import com.example.sirh.model.DocumentSortie;
import com.example.sirh.model.RuptureContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DocumentSortieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @author moi
 * module 12
 */

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentSortieService {

    private final DocumentSortieRepository repo;

    /** RÈGLE : 3 documents obligatoires. Idempotent. */
    public void genererObligatoires(RuptureContrat rupture, Utilisateur generePar) {
        for (String type : List.of("SOLDE_TOUT_COMPTE", "CERTIFICAT_TRAVAIL", "ATTESTATION_EMPLOI")) {
            if (repo.findByRuptureIdRuptureAndTypeDocument(
                    rupture.getIdRupture(), type).isEmpty()) {
                repo.save(DocumentSortie.builder()
                        .rupture(rupture)
                        .typeDocument(type)
                        .dateGeneration(LocalDate.now())
                        .generePar(generePar)
                        .build());
            }
        }
    }

    /** RÈGLE : date_remise >= date_generation. */
    public DocumentSortie remettre(Integer idDoc, String fichierUrl) {
        DocumentSortie d = repo.findById(idDoc)
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable"));
        if (d.getDateRemise() != null)
            throw new IllegalStateException("Document déjà remis");
        d.setFichierUrl(fichierUrl);
        d.setDateRemise(LocalDate.now());
        return repo.save(d);
    }

    /** Bloque la clôture tant que les 3 documents obligatoires ne sont pas remis. */
    public boolean tousRemis(Integer idRupture) {
        List<DocumentSortie> docs = repo.findByRuptureIdRupture(idRupture);
        long obligatoires = docs.stream()
                .filter(d -> List.of("SOLDE_TOUT_COMPTE", "CERTIFICAT_TRAVAIL", "ATTESTATION_EMPLOI")
                        .contains(d.getTypeDocument()))
                .count();
        long remis = docs.stream()
                .filter(d -> d.getDateRemise() != null)
                .filter(d -> List.of("SOLDE_TOUT_COMPTE", "CERTIFICAT_TRAVAIL", "ATTESTATION_EMPLOI")
                        .contains(d.getTypeDocument()))
                .count();
        return obligatoires >= 3 && remis >= 3;
    }

    public List<DocumentSortie> lister(Integer idRupture) {
        return repo.findByRuptureIdRupture(idRupture);
    }
}