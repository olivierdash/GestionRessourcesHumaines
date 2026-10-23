package com.example.sirh.controller;

import com.example.sirh.model.Departement;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/departements")
public class DepartementController {

    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;

    public DepartementController(DepartementRepository departementRepository,
            UtilisateurRepository utilisateurRepository, EmployeRepository employeRepository) {
        this.departementRepository = departementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("departements", departementRepository.findAll());
        return "bloc1/departement/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        List<Utilisateur> managers = utilisateurRepository.findAll().stream()
                .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole().getLibelle()))
                .toList();
        model.addAttribute("managers", managers);
        model.addAttribute("departement", new Departement());
        return "bloc1/departement/form";
    }

    @PostMapping
    public String store(
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager,
            Model model) {

        // VALIDATION SERVEUR : Vérification de l'unicité
        if (departementRepository.existsByTitreIgnoreCase(titre.trim())) {
            model.addAttribute("error", "Un département avec ce titre existe déjà.");
            model.addAttribute("managers", utilisateurRepository.findAll().stream()
                    .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole().getLibelle()))
                    .toList());
            Departement dep = new Departement();
            dep.setTitre(titre);
            model.addAttribute("departement", dep);
            return "bloc1/departement/form"; // Retour au formulaire avec l'erreur
        }

        Departement departement = new Departement();
        departement.setTitre(titre.trim());

        if (idManager != null) {
            utilisateurRepository.findById(idManager).ifPresent(departement::setManager);
        }

        departementRepository.save(departement);
        return "redirect:/departements";
    }

    /** GET /departements/{id} - Fiche détail. */
    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable"));

        // Récupération du vrai nombre d'employés
        long nombreEmployes = employeRepository
                .countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(id);

        model.addAttribute("departement", departement);
        model.addAttribute("nombreEmployes", nombreEmployes);

        return "bloc1/departement/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable"));
        model.addAttribute("managers", utilisateurRepository.findAll().stream()
                .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole().getLibelle()))
                .toList());
        model.addAttribute("departement", departement);
        return "bloc1/departement/form";
    }

    @PutMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager,
            Model model) {

        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable"));

        // VALIDATION SERVEUR : Vérification de l'unicité (en excluant l'ID actuel)
        if (departementRepository.existsByTitreIgnoreCaseAndIdDepartementNot(titre.trim(), id)) {
            model.addAttribute("error", "Un autre département porte déjà ce titre.");
            model.addAttribute("managers", utilisateurRepository.findAll().stream()
                    .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole().getLibelle()))
                    .toList());
            // On garde les modifications temporaires pour ne pas perdre la saisie
            departement.setTitre(titre);
            model.addAttribute("departement", departement);
            return "bloc1/departement/form";
        }

        departement.setTitre(titre.trim());

        if (idManager != null) {
            utilisateurRepository.findById(idManager).ifPresent(departement::setManager);
        } else {
            departement.setManager(null);
        }

        departementRepository.save(departement);
        return "redirect:/departements";
    }

    // -----------------------------------------------------------------
    // API POUR LA VALIDATION JAVASCRIPT (AJAX)
    // -----------------------------------------------------------------
    @GetMapping("/check-titre")
    @ResponseBody
    public boolean checkTitreDisponible(
            @RequestParam String titre,
            @RequestParam(required = false) Integer idDepartement) {

        if (idDepartement == null) {
            // Création
            return !departementRepository.existsByTitreIgnoreCase(titre.trim());
        } else {
            // Modification
            return !departementRepository.existsByTitreIgnoreCaseAndIdDepartementNot(titre.trim(), idDepartement);
        }
    }
}