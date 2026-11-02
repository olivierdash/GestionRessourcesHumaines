package com.example.sirh.controller;

import com.example.sirh.model.Poste;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.PosteMissionRepository;
import com.example.sirh.service.PosteReferentielService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Controller
@RequestMapping("/postes")
@RequiredArgsConstructor
public class PosteController {

    private final PosteReferentielService posteService;
    private final DepartementRepository departementRepository;
    private final PosteMissionRepository missionRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("postes", posteService.listerPostes());
        return "poste/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("poste", new Poste());
        model.addAttribute("departements", departementRepository.findAll());
        return "poste/form";
    }

    @PostMapping
    public String store(
            @RequestParam String intitule,
            @RequestParam Integer idDepartement,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE)
                    LocalDate dateEffet,
            @RequestParam(required = false) String lieu,
            @RequestParam(required = false) Integer dureeHebdo,
            RedirectAttributes redirect) {

        try {
            Poste poste = posteService.creerPoste(
                    intitule, idDepartement, dateEffet, lieu, dureeHebdo);

            return "redirect:/postes/" + poste.getIdPoste();

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/postes/create";
        }
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Poste poste = posteService.trouverPoste(id);

        model.addAttribute("poste", poste);
        model.addAttribute(
                "missions",
                missionRepository.findByPosteIdPoste(id));

        return "poste/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("poste", posteService.trouverPoste(id));
        model.addAttribute("departements", departementRepository.findAll());
        return "poste/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String intitule,
            @RequestParam Integer idDepartement,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE)
                    LocalDate dateEffet,
            @RequestParam(required = false) String lieu,
            @RequestParam(required = false) Integer dureeHebdo,
            RedirectAttributes redirect) {

        try {
            posteService.modifierPoste(
                    id, intitule, idDepartement,
                    dateEffet, lieu, dureeHebdo);

            return "redirect:/postes/" + id;

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/postes/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/missions")
    public String addMission(
            @PathVariable Integer id,
            @RequestParam String titre,
            RedirectAttributes redirect) {

        try {
            posteService.ajouterMission(id, titre);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/postes/" + id;
    }

    @PostMapping("/{id}/missions/{missionId}/delete")
    public String deleteMission(
            @PathVariable Integer id,
            @PathVariable Integer missionId,
            RedirectAttributes redirect) {

        try {
            posteService.supprimerMission(id, missionId);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/postes/" + id;
    }
}
