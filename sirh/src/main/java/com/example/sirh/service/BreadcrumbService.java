package com.example.sirh.service;

import com.example.sirh.dto.BreadcrumbItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BreadcrumbService {

    public static final String SESSION_KEY = "breadcrumb";

    public void set(HttpSession session, List<BreadcrumbItem> items) {
        if (session != null) {
            session.setAttribute(SESSION_KEY, items);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BreadcrumbItem> get(HttpSession session) {
        if (session == null)
            return List.of();
        Object obj = session.getAttribute(SESSION_KEY);
        if (obj instanceof List<?>) {
            return (List<BreadcrumbItem>) obj;
        }
        return List.of();
    }

    public void clear(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
        }
    }

    /**
     * Génère un fil d'Ariane par défaut à partir de l'URL.
     * Ex: /demandes/12/edit -> Accueil > Demandes > Détail #12 > Modification
     */
    public List<BreadcrumbItem> generateFromUri(String uri) {
        List<BreadcrumbItem> items = new ArrayList<>();
        items.add(new BreadcrumbItem("Accueil", "/"));

        // On ignore la racine ou les pages d'auth pour ne pas polluer
        if (uri == null || uri.isEmpty() || uri.equals("/") || uri.startsWith("/login") || uri.startsWith("/auth")) {
            return items; // Retourne juste "Accueil"
        }

        String[] parts = uri.split("/");
        // parts[0] est toujours vide à cause du premier "/"

        String module = parts.length > 1 ? parts[1] : "";
        String action = parts.length > 2 ? parts[2] : "";
        String subAction = parts.length > 3 ? parts[3] : "";

        // 1. Le module (Demandes, Employés, etc.)
        String moduleLabel = switch (module) {
            case "demandes" -> "Demandes";
            case "employes" -> "Employés";
            case "departements" -> "Départements";
            case "postes" -> "Postes";
            case "grilles" -> "Grilles";
            case "offres" -> "Offres";
            case "candidatures" -> "Candidatures";
            case "propositions" -> "Propositions";
            case "contrats" -> "Contrats";
            case "admin" -> "Administration";
            case "onboarding" -> "Onboarding";
            default -> capitalize(module);
        };

        String moduleUrl = "/" + module;
        items.add(new BreadcrumbItem(moduleLabel, moduleUrl));

        // 2. L'action principale
        if (!action.isEmpty()) {
            if (action.equals("create")) {
                items.add(new BreadcrumbItem("Création", null));
            } else if (action.equals("validations")) {
                items.add(new BreadcrumbItem("Validations", null));
            } else if (action.matches("\\d+")) {
                String detailUrl = subAction.isEmpty() ? null : moduleUrl + "/" + action;

                // 2. On ajoute l'élément au breadcrumb
                items.add(new BreadcrumbItem("Détail #" + action, detailUrl));
                // 3. La sous-action (edit, etc.)
                if (!subAction.isEmpty()) {
                    if (subAction.equals("edit") || subAction.equals("update")) {
                        items.add(new BreadcrumbItem("Modification", null));
                    } else if (subAction.equals("proposer")) {
                        items.add(new BreadcrumbItem("Proposition", null));
                    } else if (subAction.equals("evaluer")) {
                        items.add(new BreadcrumbItem("Évaluation", null));
                    } else {
                        items.add(new BreadcrumbItem(capitalize(subAction), null));
                    }
                }
            } else {
                items.add(new BreadcrumbItem(capitalize(action), null));
            }
        }

        return items;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).replace("-", " ");
    }
}