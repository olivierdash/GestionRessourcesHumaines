package com.example.sirh.controller.api.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.example.sirh.dto.DesactivationAccesDto;
import com.example.sirh.dto.DocumentSortieDto;
import com.example.sirh.dto.MaterielDto;
import com.example.sirh.dto.RuptureContratDto;
import com.example.sirh.dto.SoldeToutCompteDto;
import com.example.sirh.model.DesactivationAcces;
import com.example.sirh.model.DocumentSortie;
import com.example.sirh.model.MaterielEmploye;
import com.example.sirh.model.RuptureContrat;
import com.example.sirh.model.SoldeToutCompte;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.service.AuditService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DesactivationAccesService;
import com.example.sirh.service.DocumentSortieService;
import com.example.sirh.service.MaterielService;
import com.example.sirh.service.OffboardingService;
import com.example.sirh.service.RuptureContratService;
import com.example.sirh.service.SoldeToutCompteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author moi
 * module 12
 */
@RestController
@RequestMapping("/api/v1/offboarding")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG')")
@Transactional
public class OffboardingController {

    private static final Set<String> MOTIFS_RUPTURE = Set.of(
            "DEMISSION", "LICENCIEMENT", "FIN_CDD", "RETRAITE",
            "RUPTURE_CONVENTIONNELLE", "FIN_PERIODE_ESSAI");
    private static final Set<String> ETATS_RESTITUTION = Set.of("BON", "ENDOMMAGE", "PERDU");

    private final RuptureContratService ruptureService;
    private final OffboardingService offboardingService;
    private final MaterielService materielService;
    private final DesactivationAccesService accesService;
    private final DocumentSortieService documentService;
    private final SoldeToutCompteService stcService;
    private final CongeAccessService accessService;
    private final AuditService auditService;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;

    @GetMapping("/ruptures")
    public List<RuptureContratDto> listerRuptures(
            @RequestParam(required = false) String statut) {
        Utilisateur acteur = accessService.utilisateurCourant();
        List<RuptureContrat> ruptures = statut == null || statut.isBlank()
                ? ruptureService.listerActifs()
                : ruptureService.listerParStatut(statut);
        auditService.enregistrerConsultationSensible(
                acteur, "rupture_contrat", 0, "Consultation de la liste des ruptures");
        return ruptures.stream().map(this::versDto).toList();
    }

    @GetMapping("/ruptures/{id}")
    public RuptureContratDto consulterRupture(@PathVariable Integer id) {
        RuptureContrat rupture = ruptureService.findById(id);
        auditService.enregistrerConsultationSensible(
                accessService.utilisateurCourant(), "rupture_contrat", id,
                "Consultation d'une rupture de contrat");
        return versDto(rupture);
    }

    @PostMapping("/ruptures")
    @ResponseStatus(HttpStatus.CREATED)
    public RuptureContratDto initierRupture(@Valid @RequestBody RuptureRequest request) {
        var employe = employeRepository.findById(request.idEmployer())
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable"));
        var contrat = contratRepository.findById(request.idContract())
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));
        if (!employe.getIdEmployer().equals(contrat.getEmploye().getIdEmployer())) {
            throw new IllegalArgumentException("Le contrat ne correspond pas au salarié");
        }
        Utilisateur acteur = preparerActeur();
        RuptureContrat rupture = RuptureContrat.builder()
                .employe(employe)
                .contrat(contrat)
                .motifRupture(validerValeur(request.motifRupture(), MOTIFS_RUPTURE, "motif"))
                .dateNotification(request.dateNotification())
                .dureePreavisJours(request.dureePreavisJours())
                .dateSortie(request.dateSortie())
                .commentaire(request.commentaire())
                .initiePar(acteur)
                .build();
        RuptureContrat creee = ruptureService.initier(rupture);
        auditer(acteur, "CREATION", "rupture_contrat", creee.getIdRupture(),
                "Initiation d'une rupture de contrat");
        return versDto(creee);
    }

    @PostMapping("/ruptures/{id}/demarrer")
    public RuptureContratDto demarrerOffboarding(@PathVariable Integer id) {
        Utilisateur acteur = preparerActeur();
        RuptureContrat rupture = offboardingService.demarrerOffboarding(id, acteur);
        auditer(acteur, "DEMARRAGE_OFFBOARDING", "rupture_contrat", id,
                "Démarrage de l'offboarding");
        return versDto(rupture);
    }

    @PostMapping("/ruptures/{id}/cloturer")
    public RuptureContratDto cloturerOffboarding(@PathVariable Integer id) {
        Utilisateur acteur = preparerActeur();
        RuptureContrat rupture = offboardingService.cloturer(id);
        auditer(acteur, "CLOTURE", "rupture_contrat", id, "Clôture et archivage du dossier salarié");
        return versDto(rupture);
    }

    @PostMapping("/ruptures/{id}/annuler")
    public RuptureContratDto annulerRupture(@PathVariable Integer id) {
        Utilisateur acteur = preparerActeur();
        RuptureContrat rupture = ruptureService.changerStatut(id, "ANNULEE");
        auditer(acteur, "ANNULATION", "rupture_contrat", id, "Annulation d'une rupture");
        return versDto(rupture);
    }

    @GetMapping("/employes/{idEmployer}/materiels")
    public List<MaterielDto> listerMateriels(@PathVariable Integer idEmployer) {
        employeRepository.findById(idEmployer)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable"));
        auditService.enregistrerConsultationSensible(accessService.utilisateurCourant(),
                "materiel_employe", idEmployer, "Consultation du matériel du salarié");
        return materielService.listerParEmploye(idEmployer).stream().map(this::versDto).toList();
    }

    @PostMapping("/materiels")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterielDto attribuerMateriel(@Valid @RequestBody MaterielRequest request) {
        var employe = employeRepository.findById(request.idEmployer())
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable"));
        Utilisateur acteur = preparerActeur();
        MaterielEmploye materiel = MaterielEmploye.builder()
                .employe(employe)
                .designation(request.designation())
                .numeroSerie(request.numeroSerie())
                .dateRemise(request.dateRemise())
                .montantRetenue(request.montantRetenue())
                .build();
        MaterielEmploye attribue = materielService.attribuer(materiel);
        auditer(acteur, "ATTRIBUTION", "materiel_employe", attribue.getId(),
                "Attribution de matériel à un salarié");
        return versDto(attribue);
    }

    @PostMapping("/materiels/{id}/restituer")
    public MaterielDto restituerMateriel(
            @PathVariable Integer id, @Valid @RequestBody RestitutionRequest request) {
        Utilisateur acteur = preparerActeur();
        MaterielEmploye materiel = materielService.restituer(
                id, validerValeur(request.etatRestitution(), ETATS_RESTITUTION, "état"),
                request.montantRetenue());
        auditer(acteur, "RESTITUTION", "materiel_employe", id, "Restitution du matériel");
        return versDto(materiel);
    }

    @GetMapping("/ruptures/{id}/acces")
    public List<DesactivationAccesDto> listerAcces(@PathVariable Integer id) {
        ruptureService.findById(id);
        auditService.enregistrerConsultationSensible(accessService.utilisateurCourant(),
                "desactivation_acces", id, "Consultation des accès à désactiver");
        return accesService.lister(id).stream().map(this::versDto).toList();
    }

    @PostMapping("/acces/{id}/fait")
    public DesactivationAccesDto marquerAccesDesactive(
            @PathVariable Integer id, @Valid @RequestBody CommentaireRequest request) {
        Utilisateur acteur = preparerActeur();
        DesactivationAcces acces = accesService.marquerFait(id, acteur, request.commentaire());
        auditer(acteur, "DESACTIVATION", "desactivation_acces", id,
                "Désactivation d'un accès lors de l'offboarding");
        return versDto(acces);
    }

    @GetMapping("/ruptures/{id}/documents")
    public List<DocumentSortieDto> listerDocuments(@PathVariable Integer id) {
        ruptureService.findById(id);
        auditService.enregistrerConsultationSensible(accessService.utilisateurCourant(),
                "document_sortie", id, "Consultation des documents de sortie");
        return documentService.lister(id).stream().map(this::versDto).toList();
    }

    @PostMapping("/documents/{id}/remettre")
    public DocumentSortieDto remettreDocument(
            @PathVariable Integer id, @Valid @RequestBody RemiseDocumentRequest request) {
        Utilisateur acteur = preparerActeur();
        DocumentSortie document = documentService.remettre(id, request.fichierUrl());
        auditer(acteur, "REMISE", "document_sortie", id, "Remise d'un document de sortie");
        return versDto(document);
    }

    @PostMapping("/ruptures/{id}/solde")
    @ResponseStatus(HttpStatus.CREATED)
    public SoldeToutCompteDto creerSolde(
            @PathVariable Integer id, @Valid @RequestBody SoldeRequest request) {
        RuptureContrat rupture = ruptureService.findById(id);
        if (stcService.findByrupture(id).isPresent()) {
            throw new IllegalStateException("Un solde de tout compte existe déjà pour cette rupture");
        }
        Utilisateur acteur = preparerActeur();
        SoldeToutCompte solde = SoldeToutCompte.builder()
                .ruptureContrat(rupture)
                .salaireMensuelReference(request.salaireMensuelReference())
                .joursCongesRestants(request.joursCongesRestants())
                .totalGains(request.totalGains())
                .totalRetenues(request.totalRetenues())
                .build();
        SoldeToutCompte cree = stcService.creer(solde);
        auditer(acteur, "CREATION", "solde_tout_compte", cree.getIdStc(),
                "Création d'un solde de tout compte");
        return versDto(cree);
    }

    @GetMapping("/ruptures/{id}/solde")
    public SoldeToutCompteDto consulterSolde(@PathVariable Integer id) {
        SoldeToutCompte solde = stcService.findByrupture(id)
                .orElseThrow(() -> new IllegalArgumentException("Solde de tout compte introuvable"));
        auditService.enregistrerConsultationSensible(accessService.utilisateurCourant(),
                "solde_tout_compte", solde.getIdStc(), "Consultation d'un solde de tout compte");
        return versDto(solde);
    }

    @PostMapping("/soldes/{id}/valider")
    public SoldeToutCompteDto validerSolde(@PathVariable Integer id) {
        Utilisateur acteur = preparerActeur();
        SoldeToutCompte solde = stcService.valider(id, acteur);
        auditer(acteur, "VALIDATION", "solde_tout_compte", id, "Validation du solde de tout compte");
        return versDto(solde);
    }

    @PostMapping("/soldes/{id}/payer")
    public SoldeToutCompteDto marquerSoldePaye(@PathVariable Integer id) {
        Utilisateur acteur = preparerActeur();
        SoldeToutCompte solde = stcService.marquerPaye(id);
        auditer(acteur, "PAIEMENT", "solde_tout_compte", id,
                "Marquage du solde de tout compte comme payé");
        return versDto(solde);
    }

    private RuptureContratDto versDto(RuptureContrat rupture) {
        var candidat = rupture.getEmploye().getCandidature().getCandidat();
        return RuptureContratDto.builder()
                .idRupture(rupture.getIdRupture())
                .idEmployer(rupture.getEmploye().getIdEmployer())
                .employeNom(candidat.getPrenom() + " " + candidat.getNom())
                .matricule(rupture.getEmploye().getMatricule())
                .motifRupture(rupture.getMotifRupture())
                .statutRupture(rupture.getStatutRupture())
                .dateNotification(rupture.getDateNotification())
                .dureePreavisJours(rupture.getDureePreavisJours())
                .dateSortie(rupture.getDateSortie())
                .commentaire(rupture.getCommentaire())
                .archive(rupture.getArchive())
                .build();
    }

    private MaterielDto versDto(MaterielEmploye materiel) {
        return MaterielDto.builder()
                .id(materiel.getId())
                .idEmployer(materiel.getEmploye().getIdEmployer())
                .designation(materiel.getDesignation())
                .numeroSerie(materiel.getNumeroSerie())
                .dateRemise(materiel.getDateRemise())
                .dateRestitution(materiel.getDateRestitution())
                .etatRestitution(materiel.getEtatRestitution())
                .montantRetenue(materiel.getMontantRetenue())
                .restitue(materiel.getDateRestitution() != null)
                .build();
    }

    private DesactivationAccesDto versDto(DesactivationAcces acces) {
        return DesactivationAccesDto.builder()
                .id(acces.getId())
                .idRupture(acces.getRupture().getIdRupture())
                .typeAcces(acces.getTypeAcces())
                .identifiant(acces.getIdentifiant())
                .dateDesactivation(acces.getDateDesactivation())
                .statut(acces.getStatut())
                .effectueParNom(acces.getEffectuePar() == null ? null : acces.getEffectuePar().getNom())
                .commentaire(acces.getCommentaire())
                .build();
    }

    private DocumentSortieDto versDto(DocumentSortie document) {
        return DocumentSortieDto.builder()
                .id(document.getId())
                .idRupture(document.getRupture().getIdRupture())
                .typeDocument(document.getTypeDocument())
                .reference(document.getReference())
                .fichierUrl(document.getFichierUrl())
                .dateGeneration(document.getDateGeneration())
                .dateRemise(document.getDateRemise())
                .genereParNom(document.getGenerePar() == null ? null : document.getGenerePar().getNom())
                .remis(document.getDateRemise() != null)
                .build();
    }

    private SoldeToutCompteDto versDto(SoldeToutCompte solde) {
        return SoldeToutCompteDto.builder()
                .idStc(solde.getIdStc())
                .idRupture(solde.getRuptureContrat().getIdRupture())
                .dateCalcul(solde.getDateCalcul())
                .salaireMensuelReference(solde.getSalaireMensuelReference())
                .joursCongesRestants(solde.getJoursCongesRestants())
                .totalGains(solde.getTotalGains())
                .totalRetenues(solde.getTotalRetenues())
                .netAPayer(solde.getNetAPayer())
                .statut(solde.getStatut())
                .dateValidation(solde.getDateValidation())
                .build();
    }

    private String validerValeur(String valeur, Set<String> valeurs, String nom) {
        if (!valeurs.contains(valeur)) {
            throw new IllegalArgumentException("Valeur de " + nom + " invalide");
        }
        return valeur;
    }

    private Utilisateur preparerActeur() {
        Utilisateur acteur = accessService.utilisateurCourant();
        auditService.definirUtilisateurCourant(acteur);
        return acteur;
    }

    private void auditer(Utilisateur acteur, String action, String entite,
                         Integer idEntite, String details) {
        auditService.enregistrerActionSensible(acteur, action, entite, idEntite, details);
    }

    public record RuptureRequest(
            @NotNull Integer idEmployer,
            @NotNull Integer idContract,
            @NotBlank String motifRupture,
            LocalDate dateNotification,
            @NotNull @Min(0) Integer dureePreavisJours,
            @NotNull LocalDate dateSortie,
            @Size(max = 500) String commentaire) {}

    public record MaterielRequest(
            @NotNull Integer idEmployer,
            @NotBlank @Size(max = 150) String designation,
            @Size(max = 100) String numeroSerie,
            LocalDate dateRemise,
            @DecimalMin("0.00") BigDecimal montantRetenue) {}

    public record RestitutionRequest(
            @NotBlank @Pattern(regexp = "BON|ENDOMMAGE|PERDU") String etatRestitution,
            @DecimalMin("0.00") BigDecimal montantRetenue) {}

    public record CommentaireRequest(@Size(max = 500) String commentaire) {}

    public record RemiseDocumentRequest(@NotBlank @Size(max = 500) String fichierUrl) {}

    public record SoldeRequest(
            @NotNull @Positive BigDecimal salaireMensuelReference,
            @NotNull @PositiveOrZero BigDecimal joursCongesRestants,
            @NotNull @PositiveOrZero BigDecimal totalGains,
            @NotNull @PositiveOrZero BigDecimal totalRetenues) {}
}
