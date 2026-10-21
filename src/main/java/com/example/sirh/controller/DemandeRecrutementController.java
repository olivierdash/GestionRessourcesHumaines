package com.example.sirh.controller;

import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.*;
import com.example.sirh.wrapper.DemandeRecrutementFilter;
import com.example.sirh.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/demandes")
public class DemandeRecrutementController {

    private final DemandeRecrutementRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;
    private final EtatDemandeRepository etatDemandeRepository;
    private final HistoriqueDemandeRecrutementRepository historiqueRepository;

    public DemandeRecrutementController(
            DemandeRecrutementRepository demandeRepository,
            UtilisateurRepository utilisateurRepository,
            PosteRepository posteRepository,
            DepartementRepository departementRepository,
            EtatDemandeRepository etatDemandeRepository,
            HistoriqueDemandeRecrutementRepository historiqueRepository) {
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.posteRepository = posteRepository;
        this.departementRepository = departementRepository;
        this.etatDemandeRepository = etatDemandeRepository;
        this.historiqueRepository = historiqueRepository;
    }

    // -----------------------------------------------------------------
    // INDEX : Liste avec pagination et filtre
    // -----------------------------------------------------------------
    @GetMapping
    public String index(
            @ModelAttribute("filter") DemandeRecrutementFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Sécurité pour les dates (si l'utilisateur efface les champs)
        if (filter.getDateMin() == null) filter.setDateMin(java.time.LocalDate.now());
        if (filter.getDateMax() == null) filter.setDateMax(java.time.LocalDate.now());

        // Pagination triée par date de création décroissante
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                Sort.by("dateCreation").descending());

        Page<DemandeRecrutementLib> demandesPage = demandeRepository.findAllLib(
                filter.getDateMin().atStartOfDay(),
                filter.getDateMax().atTime(LocalTime.MAX),
                filter.getIdPoste(),
                filter.getEffectifMin(),
                filter.getEffectifMax(),
                filter.getIdManager(),
                filter.getIdDepartement(),
                pageable
        );

        model.addAttribute("demandes", demandesPage);
        model.addAttribute("postes", posteRepository.findAll());
        model.addAttribute("departements", departementRepository.findAll());
        model.addAttribute("managers", utilisateurRepository.findAll()); // À filtrer par role MANAGER si besoin

        return "demande/index";
    }

    // -----------------------------------------------------------------
    // CREATE (GET) : Formulaire de création
    // -----------------------------------------------------------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("postes", posteRepository.findAll());
        // On ne propose que les utilisateurs ayant le rôle MANAGER (filtrage basique en Java)
        List<Utilisateur> managers = utilisateurRepository.findAll().stream()
                .wrapper(u -> "MANAGER".equals(u.getRole()))
                .toList();
        model.addAttribute("managers", managers);
        
        return "demande/form";
    }

    // -----------------------------------------------------------------
    // CREATE (POST) : Enregistrement
    // -----------------------------------------------------------------
    @PostMapping
    public String store(
            @RequestParam Integer idManager,
            @RequestParam Integer idPoste,
            @RequestParam Integer effectif,
            @RequestParam(required = false) String motif) {

        Utilisateur manager = utilisateurRepository.findById(idManager)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager introuvable"));
        
        Poste poste = posteRepository.findById(idPoste)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Poste introuvable"));

        DemandeRecrutement demande = DemandeRecrutement.builder()
                .manager(manager)
                .poste(poste)
                .effectif(effectif)
                .motif(motif)
                .build();

        DemandeRecrutement savedDemande = demandeRepository.save(demande);

        // Création de l'état initial (BROUILLON)
        etatDemandeRepository.findAll().stream()
                .wrapper(e -> "BROUILLON".equals(e.getIntitule()))
                .findFirst()
                .ifPresent(etat -> {
                    HistoriqueDemandeRecrutement hist = HistoriqueDemandeRecrutement.builder()
                            .demandeRecrutement(savedDemande)
                            .etatDemande(etat)
                            .validateur(manager)
                            .build();
                    historiqueRepository.save(hist);
                });

        return "redirect:/demandes/" + savedDemande.getIdDemandeRecrutement();
    }

    // -----------------------------------------------------------------
    // SHOW : Fiche de la demande avec historique d'état
    // -----------------------------------------------------------------
    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        // Récupération de la demande
        DemandeRecrutement demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));

        // Récupération et tri de l'historique (du plus récent au plus ancien)
        List<HistoriqueDemandeRecrutement> historique = historiqueRepository.findAll().stream()
                .wrapper(h -> h.getDemandeRecrutement().getIdDemandeRecrutement().equals(id))
                .sorted(Comparator.comparing(HistoriqueDemandeRecrutement::getDateChangement).reversed())
                .toList();

        model.addAttribute("demande", demande);
        model.addAttribute("historique", historique);
        
        return "demande/show";
    }
}