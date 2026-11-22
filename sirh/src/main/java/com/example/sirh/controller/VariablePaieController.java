package com.example.sirh.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.dto.VariablePaieFilter;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.VariablePaie;
import com.example.sirh.model.enums.CodeVariablePaie;
import com.example.sirh.model.enums.StatutVariablePaie;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.VariablePaieService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/variables-paie")
@RequiredArgsConstructor
public class VariablePaieController {

    private final VariablePaieService variablePaieService;
    private final EmployeService employeService;

    @GetMapping
    public String index(@ModelAttribute("filter") VariablePaieFilter filter, Model model) {
        if (filter.getPeriode() == null || filter.getPeriode().isBlank()) {
            filter.setPeriode(YearMonth.now().toString());
        }
        List<VariablePaie> variables = variablePaieService.rechercher(
                filter.getPeriode(), filter.getEmployeId(), filter.getStatut());

        Map<CodeVariablePaie, BigDecimal> totaux = new LinkedHashMap<>();
        for (VariablePaie variable : variables) {
            if (variable.getStatut() == StatutVariablePaie.ANNULE) {
                continue;
            }
            totaux.merge(variable.getCode(), variable.getQuantite(), BigDecimal::add);
        }

        model.addAttribute("variables", variables);
        model.addAttribute("totaux", totaux);
        model.addAttribute("employes", employeService.getAllEmployesList());
        model.addAttribute("statuts", StatutVariablePaie.values());
        return "conges/variables-paie/index";
    }

    @PostMapping("/{id}/consommer")
    public String consommer(@PathVariable Integer id,
                            @ModelAttribute("filter") VariablePaieFilter filter,
                            RedirectAttributes redirect) {
        try {
            variablePaieService.marquerConsomme(id);
            redirect.addFlashAttribute("succes", "Variable marquée comme consommée.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("erreur", e.getErrors().values().stream()
                    .findFirst().orElse("Erreur de validation."));
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/conges/variables-paie?periode=" + filter.getPeriode();
    }

    @GetMapping("/export")
    public void exporter(@ModelAttribute("filter") VariablePaieFilter filter,
                         HttpServletResponse response) throws IOException {
        String periode = filter.getPeriode() == null || filter.getPeriode().isBlank()
                ? YearMonth.now().toString() : filter.getPeriode();
        List<VariablePaie> variables = variablePaieService.rechercher(
                periode, filter.getEmployeId(), filter.getStatut());

        StringBuilder csv = new StringBuilder();
        csv.append("Periode;Matricule;Salarie;Code;Libelle;Date debut;Date fin;Quantite;Unite;Statut\n");
        for (VariablePaie variable : variables) {
            csv.append(variable.getPeriode()).append(';')
                    .append(variable.getEmploye().getMatricule()).append(';')
                    .append(echapper(nomEmploye(variable))).append(';')
                    .append(variable.getCode()).append(';')
                    .append(echapper(variable.getLibelle())).append(';')
                    .append(variable.getDateDebut()).append(';')
                    .append(variable.getDateFin()).append(';')
                    .append(variable.getQuantite()).append(';')
                    .append(variable.getUnite()).append(';')
                    .append(variable.getStatut()).append('\n');
        }

        byte[] contenu = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=variables-paie-" + periode + ".csv");
        response.getOutputStream().write(contenu);
    }

    private String nomEmploye(VariablePaie variable) {
        try {
            return variable.getEmploye().getCandidature().getCandidat().getPrenom()
                    + " " + variable.getEmploye().getCandidature().getCandidat().getNom();
        } catch (RuntimeException e) {
            return variable.getEmploye().getMatricule();
        }
    }

    private String echapper(String valeur) {
        if (valeur == null) {
            return "";
        }
        return valeur.replace(";", ",").replace("\n", " ");
    }
}
