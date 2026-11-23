package com.example.sirh.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.sirh.dto.LigneRemunerationDto;
import com.example.sirh.dto.RemunerationDto;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class BulletinPaieService {

    private static final DateTimeFormatter FORMAT_MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    public byte[] generer(RemunerationDto remuneration) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, sortie);
            document.open();

            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sousTitre = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font entete = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph titreBulletin = new Paragraph("Bulletin de paie", titre);
            titreBulletin.setAlignment(Element.ALIGN_CENTER);
            document.add(titreBulletin);

            Paragraph periode = new Paragraph(
                    capitaliser(remuneration.getPeriode().format(FORMAT_MOIS)), sousTitre);
            periode.setAlignment(Element.ALIGN_CENTER);
            document.add(periode);
            document.add(new Paragraph(" ", normal));

            PdfPTable identite = new PdfPTable(new float[]{1, 2});
            identite.setWidthPercentage(100);
            ajouterLigneIdentite(identite, "Salarié", valeur(remuneration.getNomComplet()), entete, normal);
            ajouterLigneIdentite(identite, "Matricule", valeur(remuneration.getMatricule()), entete, normal);
            ajouterLigneIdentite(identite, "Période", remuneration.getDateDebut() + " au " + remuneration.getDateFin(), entete, normal);
            ajouterLigneIdentite(identite, "Mode", remuneration.isForfaitJours() ? "Forfait jours" : "Horaire", entete, normal);
            ajouterLigneIdentite(identite, "Heures mensuelles", montant(remuneration.getHeuresMensuelles()), entete, normal);
            ajouterLigneIdentite(identite, "Taux horaire", montant(remuneration.getTauxHoraire()), entete, normal);
            document.add(identite);
            document.add(new Paragraph(" ", normal));

            PdfPTable lignes = new PdfPTable(new float[]{4, 2, 2, 2});
            lignes.setWidthPercentage(100);
            ajouterCellule(lignes, "Libellé", entete, Element.ALIGN_LEFT, true);
            ajouterCellule(lignes, "Quantité", entete, Element.ALIGN_RIGHT, true);
            ajouterCellule(lignes, "Taux", entete, Element.ALIGN_RIGHT, true);
            ajouterCellule(lignes, "Montant", entete, Element.ALIGN_RIGHT, true);
            for (LigneRemunerationDto ligne : remuneration.getLignes()) {
                ajouterCellule(lignes, valeur(ligne.getLibelle()), normal, Element.ALIGN_LEFT, false);
                ajouterCellule(lignes, montant(ligne.getQuantite()), normal, Element.ALIGN_RIGHT, false);
                ajouterCellule(lignes, montant(ligne.getTaux()), normal, Element.ALIGN_RIGHT, false);
                ajouterCellule(lignes, montant(ligne.getMontant()), normal, Element.ALIGN_RIGHT, false);
            }
            document.add(lignes);
            document.add(new Paragraph(" ", normal));

            PdfPTable totaux = new PdfPTable(new float[]{3, 2});
            totaux.setWidthPercentage(60);
            totaux.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ajouterLigneIdentite(totaux, "Salaire de base", montant(remuneration.getSalaireBase()), entete, normal);
            ajouterLigneIdentite(totaux, "Retenues", montant(remuneration.getTotalRetenues()), entete, normal);
            ajouterLigneIdentite(totaux, "Heures supplémentaires", montant(remuneration.getTotalHeuresSupplementaires()), entete, normal);
            ajouterLigneIdentite(totaux, "Brut", montant(remuneration.getBrut()), entete, normal);
            document.add(totaux);

            document.close();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le bulletin de paie.", e);
        }
        return sortie.toByteArray();
    }

    private void ajouterLigneIdentite(PdfPTable table, String libelle, String valeur,
                                      Font entete, Font normal) {
        ajouterCellule(table, libelle, entete, Element.ALIGN_LEFT, false);
        ajouterCellule(table, valeur, normal, Element.ALIGN_LEFT, false);
    }

    private void ajouterCellule(PdfPTable table, String texte, Font font, int alignement,
                                boolean fondGris) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte, font));
        cellule.setHorizontalAlignment(alignement);
        cellule.setPadding(4);
        if (fondGris) {
            cellule.setGrayFill(0.92f);
        }
        table.addCell(cellule);
    }

    private String valeur(String valeur) {
        return valeur == null || valeur.isBlank() ? "—" : valeur;
    }

    private String montant(BigDecimal valeur) {
        if (valeur == null) {
            return "—";
        }
        return String.format(Locale.FRANCE, "%,.2f", valeur);
    }

    private String capitaliser(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }
        return texte.substring(0, 1).toUpperCase(Locale.FRENCH) + texte.substring(1);
    }
}
