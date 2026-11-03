package com.example.sirh.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DemandeRecrutementDTO;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.dto.ValidationPageData;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.EtatDemande;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.HistoriqueDemandeRecrutement;
import com.example.sirh.model.MotifDemande;
import com.example.sirh.model.Poste;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.wrapper.DemandeRecrutementFilter;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DemandeRecrutementService {

    private final UtilisateurService utilisateurService;
    private final PosteService posteService;
    private final MotifDemandeService motifDemandeService;
    private final DemandeRecrutementRepository demandeRepository;
    private final EtatDemandeService etatDemandeService;
    private final HistoriqueDemandeRecrutementService historiqueService;
    private final GrilleSalarialService grilleSalarialService;

    @Transactional
    public DemandeRecrutement createDemande(DemandeRecrutementDTO dto) {
        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        Poste poste = posteService.getById(dto.getIdPoste());
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());
        EtatDemande statutBrouillon = etatDemandeService.getByIntitule("Brouillon");

        DemandeRecrutement demande = DemandeRecrutement.builder()
                .manager(manager)
                .poste(poste)
                .dateSouhaitee(dto.getDateSouhaitee())
                .effectif(dto.getEffectif())
                .motif(motif)
                .etatDemande(statutBrouillon)
                .commentaires(dto.getRemarque())
                .build();

        return demandeRepository.save(demande);
    }

    @Transactional(readOnly = true)
    public DemandeRecrutement getById(Integer id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demande introuvable"));
    }

    @Transactional(readOnly = true)
    public List<DemandeRecrutement> getAllDemandes() {
        return demandeRepository.findAll();
    }

    @Transactional
    public DemandeRecrutement updateDemande(DemandeRecrutementDTO dto, Utilisateur validateur,
            String action) {

        DemandeRecrutement demande = getById(dto.getIdDemandeRecrutement());
        EtatDemande etatActuel = demande.getEtatDemande();
        String etatIntitule = etatActuel.getIntitule().toLowerCase().trim();
        String role = validateur.getRole().getLibelle().toLowerCase().trim();

        if (!"brouillon".equals(etatIntitule) || !"manager".equals(role)) {
            throw new RuntimeException(
                    "Modification Impossible : seul le manager peut modifier un brouillon.");
        }

        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        Poste poste = posteService.getById(dto.getIdPoste());
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());

        demande.setManager(manager);
        demande.setPoste(poste);
        demande.setDateSouhaitee(dto.getDateSouhaitee());
        demande.setEffectif(dto.getEffectif());
        demande.setMotif(motif);
        demande.setCommentaires(dto.getRemarque());

        if ("submit".equals(action)) {
            EtatDemande nouvelEtat = etatDemandeService.getByIntitule("Cree");
            demande.setEtatDemande(nouvelEtat);
            demandeRepository.save(demande);
            saveHistorique(demande, validateur);
        } else {
            demandeRepository.save(demande);
        }

        return demande;
    }

    @Transactional
    public DemandeRecrutement updateStateDemande(Integer idDemande, Utilisateur validateur,
            String remarque, String action) {

        DemandeRecrutement demande = getById(idDemande);
        demande.setCommentaires(remarque);

        EtatDemande etatActuel = demande.getEtatDemande();
        String etatIntitule = etatActuel.getIntitule().toLowerCase().trim();
        String role = validateur.getRole().getLibelle().toLowerCase().trim();

        EtatDemande nouvelEtat = null;

        if ("refuser".equals(action)) {
            if ("rh".equals(role) || "finance".equals(role) || "dg".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("Refuse");
            } else {
                throw new RuntimeException("Seul un validateur peut refuser une demande.");
            }
        } else {
            if ("cree".equalsIgnoreCase(etatIntitule) && "rh".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("ValideRH");
            } else if ("validerh".equalsIgnoreCase(etatIntitule) && "finance".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("ValideFinance");
            } else if ("validefinance".equalsIgnoreCase(etatIntitule) && "dg".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("Valide");
            }
        }

        if (nouvelEtat == null) {
            throw new RuntimeException("Action impossible pour cet état ou ce rôle.");
        }

        demande.setEtatDemande(nouvelEtat);
        demandeRepository.save(demande);
        saveHistorique(demande, validateur);

        return demande;
    }

    @Transactional(readOnly = true)
    public Page<DemandeRecrutementLib> findAllLib(DemandeRecrutementFilter filter, Pageable pageable) {
        return demandeRepository.findAllLib(filter, pageable);
    }

    @Transactional(readOnly = true)
    public DemandeRecrutementDTO getDtoForEdit(Integer id) {
        DemandeRecrutement demande = getById(id);
        if (!"Brouillon".equalsIgnoreCase(demande.getEtatDemande().getIntitule())) {
            throw new RuntimeException("Cette demande n'est plus modifiable.");
        }
        return toDto(demande);
    }

    private DemandeRecrutementDTO toDto(DemandeRecrutement demande) {
        if (demande == null) {
            return null;
        }

        DemandeRecrutementDTO dto = new DemandeRecrutementDTO();
        dto.setIdDemandeRecrutement(demande.getIdDemandeRecrutement());

        if (demande.getManager() != null) {
            dto.setIdManager(demande.getManager().getIdUtilisateur());
        }
        if (demande.getPoste() != null) {
            dto.setIdPoste(demande.getPoste().getIdPoste());
        }
        dto.setDateSouhaitee(demande.getDateSouhaitee());
        dto.setEffectif(demande.getEffectif());
        if (demande.getMotif() != null) {
            dto.setMotif(demande.getMotif());
        }
        if (demande.getEtatDemande() != null) {
            dto.setIdEtat(demande.getEtatDemande().getIdEtatDemande());
            dto.setEtat(demande.getEtatDemande().getIntitule());
        }
        dto.setRemarque(demande.getCommentaires());

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean canValidate(String etat, String role) {
        return ("cree".equals(etat) && "rh".equals(role)) ||
               ("validerh".equals(etat) && "finance".equals(role)) ||
               ("validefinance".equals(etat) && "dg".equals(role)) ||
               "refuse".equals(etat);
    }

    @Transactional(readOnly = true)
    public ValidationPageData getValidationPageData(String role) {
        String etatRequis = switch (role) {
            case "RH" -> "Cree";
            case "FINANCE" -> "ValideRH";
            case "DG" -> "ValideFinance";
            default -> null;
        };

        List<DemandeRecrutement> demandes = (etatRequis != null)
                ? demandeRepository.findByEtatDemandeIntituleIgnoreCaseOrderByDateCreationDesc(etatRequis)
                : List.of();

        Map<Integer, GrilleSalarial> grilles = new HashMap<>();

        if ("FINANCE".equals(role)) {
            for (DemandeRecrutement d : demandes) {
                grilleSalarialService.getGrillePourPosteEtDepartement(
                        d.getPoste().getIdPoste(),
                        d.getPoste().getDepartement().getIdDepartement())
                        .ifPresent(grille -> grilles.put(d.getIdDemandeRecrutement(), grille));
            }
        }
        return new ValidationPageData(demandes, grilles);
    }

    @Transactional
    public void saveHistorique(DemandeRecrutement demande, Utilisateur validateur) {
        HistoriqueDemandeRecrutement histo = new HistoriqueDemandeRecrutement();
        histo.setCommentaires(demande.getCommentaires());
        histo.setDemandeRecrutement(demande);
        histo.setEtatDemande(demande.getEtatDemande());
        histo.setValidateur(validateur);
        historiqueService.save(histo);
    }
}