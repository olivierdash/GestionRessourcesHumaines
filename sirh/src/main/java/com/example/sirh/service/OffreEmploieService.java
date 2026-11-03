package com.example.sirh.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.sirh.dto.OffrePublicDto;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.StatutOffreEmploi;
import com.example.sirh.repository.OffreEmploieRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @author vox
 *         OffreEmploieService
 */
@Service
@AllArgsConstructor
public class OffreEmploieService {

    private final OffreEmploieRepository offreEmploieRepo;

    private final StatutOffreEmploieService statutOffreService;
    private final DemandeRecrutementService demandeRecrutementService;

    /**
     * task 8 : lister toutes les offres d'emploi
     */
    public List<OffreEmploi> listAllEmploie() {
        return offreEmploieRepo.findAllByOrderByDatePublicationDesc();
    }

    /**
     * task 9 : créer une offre d'emploi
     */
    @Transactional
    public OffreEmploi createNewOffer(OffreEmploi offre, Integer idStatutOffreEmploi) {
        if (offre.getDemandeRecrutement() == null || offre.getDemandeRecrutement().getIdDemandeRecrutement() == null) {
            throw new IllegalArgumentException("La demande de recrutement est obligatoire");
        }

        offre.setIdOffre(null); 
        offre.setDemandeRecrutement(
                demandeRecrutementService.getById(offre.getDemandeRecrutement().getIdDemandeRecrutement()));
        offre.setStatut(statutOffreService.getById(idStatutOffreEmploi));

        return offreEmploieRepo.save(offre);
    }

    /**
     * task 9 bis: publier une offre
     */
    @Transactional
    public void publicationOffre(Integer id_offre) {
        OffreEmploi offer = getOffreEmploiById(id_offre);
        StatutOffreEmploi statut_publication = statutOffreService.getByLibelle("PUBLIEE");
        offer.setStatut(statut_publication);
        offer.setDatePublication(LocalDateTime.now());
        offreEmploieRepo.save(offer);
    }

    /**
     * correction d'une offre
     */
    public OffreEmploi getOffreEmploiById(Integer id_offre) {
        return offreEmploieRepo.findById(id_offre)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    @Transactional
    public void updateoffer(Integer idOffre, String nouveauTitre, String nouvelleDescription) {
        OffreEmploi offre = getOffreEmploiById(idOffre);
        if ("CLOTUREE".equalsIgnoreCase(offre.getStatut().getLibelle())) {
            throw new RuntimeException("Impossible de modifier une offre déjà clôturée.");
        }
        offre.setTitre(nouveauTitre);
        offre.setDescription(nouvelleDescription);
        offreEmploieRepo.save(offre);
    }

    /**
     * expiration après date_cloture de l'offre
     */
    @Transactional
    public void expireoffer(Integer id_offre) {
        OffreEmploi offer = this.getOffreEmploiById(id_offre);
        if (offer.getDateCloture() != null && LocalDateTime.now().isAfter(offer.getDateCloture())) {
            StatutOffreEmploi statutCloturee = statutOffreService.getByLibelle("CLOTUREE");
            offer.setStatut(statutCloturee);
            offreEmploieRepo.save(offer);
        }
    }

    /**
     * Méthode générique pour récupérer les offres par statut
     */
    public List<OffreEmploi> getOffres(StatutOffreEmploi statut) {
        if (statut == null) {
            return offreEmploieRepo.findAllByOrderByDatePublicationDesc();
        }
        return offreEmploieRepo.findByStatutId(statut.getId());
    }

    /**
     * Méthode spécifique au site public : récupérer uniquement les offres PUBLIEES
     */
    public List<OffrePublicDto> getOffresPubliees() {
        return listAllEmploie().stream()
                .map(this::toPublicDto)
                .collect(Collectors.toList());
    }

    /**
     * Détail d'une offre publique (uniquement si elle est PUBLIEE)
     */
    public OffrePublicDto getOffrePubliqueById(Integer id) {
        OffreEmploi offre = getOffreEmploiById(id);
        if (!"PUBLIEE".equalsIgnoreCase(offre.getStatut().getLibelle())) {
            throw new RuntimeException("Cette offre n'est pas publiée.");
        }
        return toPublicDto(offre);
    }

    /**
     * Méthode utilitaire de conversion Entité -> DTO Public.
     * Enrichie pour alimenter JobCard / JobDetail côté Vue.
     */
    private OffrePublicDto toPublicDto(OffreEmploi offre) {
        var demande = offre.getDemandeRecrutement();
        var poste = demande.getPoste();

        return OffrePublicDto.builder()
                .id(offre.getIdOffre())
                .titre(offre.getTitre())
                .description(offre.getDescription())
                .statut(offre.getStatut().getLibelle())
                .intitulePoste(poste.getIntitule())
                .departement(poste.getDepartement().getTitre())
                .lieu(poste.getLieu())
                .dureeHebdo(poste.getDureeHebdo())
                .effectif(demande.getEffectif())
                .dateSouhaitee(demande.getDateSouhaitee())
                .motif(demande.getMotif() != null ? demande.getMotif().getLibelle() : null)
                .datePublication(offre.getDatePublication())
                .dateCloture(offre.getDateCloture())
                .build();
    }
}