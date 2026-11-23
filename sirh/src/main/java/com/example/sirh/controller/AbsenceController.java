package com.example.sirh.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.dto.AbsenceFilter;
import com.example.sirh.dto.AbsenceForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.JustificatifConge;
import com.example.sirh.service.AbsenceService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.JustificatifCongeService;
import com.example.sirh.service.TypeCongeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;
    private final EmployeService employeService;
    private final TypeCongeService typeCongeService;
    private final JustificatifCongeService justificatifCongeService;
    private final CongeAccessService accessService;

    @GetMapping
    public String index(@ModelAttribute("filter") AbsenceFilter filter, Model model) {
        List<Absence> absences = filter.isActif()
                ? absenceService.lister(filter)
                : absenceService.lister(null);

        Map<Integer, List<JustificatifConge>> justificatifs = new LinkedHashMap<>();
        for (Absence absence : absences) {
            justificatifs.put(absence.getId(),
                    justificatifCongeService.listerPourAbsence(absence.getId()));
        }

        model.addAttribute("absences", absences);
        model.addAttribute("justificatifsParAbsence", justificatifs);
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("types", typeCongeService.listerAbsencesActives());
        model.addAttribute("statuts", com.example.sirh.model.enums.StatutAbsence.values());
        model.addAttribute("aujourdhui", LocalDate.now());
        return "conges/absences/index";
    }

    @GetMapping("/nouveau")
    public String nouveau(@ModelAttribute("absence") AbsenceForm form, Model model) {
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("types", typeCongeService.listerAbsencesActives());
        return "conges/absences/form";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("absence") AbsenceForm form,
                              Model model, RedirectAttributes redirect) {
        try {
            absenceService.saisir(form, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Absence enregistrée.");
            return "redirect:/conges/absences";
        } catch (ValidationMetierException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("employes", employeService.getAllEmployesList());
            model.addAttribute("types", typeCongeService.listerAbsencesActives());
            return "conges/absences/form";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("employes", employeService.getAllEmployesList());
            model.addAttribute("types", typeCongeService.listerAbsencesActives());
            return "conges/absences/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Absence absence = absenceService.getById(id);
        model.addAttribute("absence", absence);
        model.addAttribute("justificatifs", justificatifCongeService.listerPourAbsence(id));
        model.addAttribute("aujourdhui", LocalDate.now());
        return "conges/absences/detail";
    }

    @PostMapping("/{id}/justifier")
    public String justifier(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            absenceService.justifier(id, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Absence justifiée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/absences/" + id;
    }

    @PostMapping("/{id}/non-justifiee")
    public String nonJustifiee(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            absenceService.marquerNonJustifiee(id, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Absence marquée non justifiée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/absences/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id,
                          @RequestParam(required = false) String motif,
                          RedirectAttributes redirect) {
        try {
            absenceService.annuler(id, motif, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Absence annulée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/absences/" + id;
    }

    @PostMapping("/{id}/justificatifs")
    public String deposerJustificatif(@PathVariable Integer id,
                                      @RequestParam("fichier") MultipartFile fichier,
                                      RedirectAttributes redirect) {
        try {
            justificatifCongeService.deposerPourAbsence(
                    id, fichier, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Justificatif déposé.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", premier(e));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/absences/" + id;
    }

    private String premier(ValidationMetierException e) {
        return e.getErrors().values().stream().findFirst().orElse("Erreur de validation.");
    }
}
