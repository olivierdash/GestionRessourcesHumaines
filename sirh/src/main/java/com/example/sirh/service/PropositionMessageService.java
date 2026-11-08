package com.example.sirh.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.PropositionMessage;
import com.example.sirh.repository.PropositionMessageRepository;

import lombok.RequiredArgsConstructor;

import static com.example.sirh.constants.ConstantSirh.Statut;

@Service
@RequiredArgsConstructor
public class PropositionMessageService {

    private final PropositionService propositionService;
    private final PropositionMessageRepository messageRepository;
    private final CandidatService candidatService;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<PropositionMessage> lister(Integer idProposition) {
        return messageRepository
                .findByPropositionIdPropositionOrderByDateCreationAsc(idProposition);
    }

    @Transactional(readOnly = true)
    public boolean attendReponseRh(Integer idProposition) {
        List<PropositionMessage> thread = lister(idProposition);
        return !thread.isEmpty() && thread.get(thread.size() - 1).getCandidat() != null;
    }

    @Transactional
    public PropositionMessage envoyerParCandidat(Integer idProposition, Integer idCandidat,
            String contenu, BigDecimal salairePropose) {
        PropositionEmbauche p = propositionService.getPourCandidat(idProposition, idCandidat);
        exigerPhaseNegociation(p);
        exigerContenuOuSalaire(contenu, salairePropose);
        exigerSalairePositif(salairePropose);

        PropositionMessage m = PropositionMessage.builder()
                .proposition(p)
                .candidat(candidatService.getCandidatById(idCandidat))
                .contenu(contenu != null ? contenu.trim() : "")
                .salairePropose(salairePropose)
                .build();
        return messageRepository.save(m);
    }

    @Transactional
    public PropositionMessage envoyerParRh(Integer idProposition, Integer idUtilisateur,
            String contenu, BigDecimal salairePropose) {
        PropositionEmbauche p = propositionService.getProposition(idProposition);
        exigerPhaseNegociation(p);
        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Un feedback RH doit contenir un message.");
        }
        exigerSalairePositif(salairePropose);

        PropositionMessage m = PropositionMessage.builder()
                .proposition(p)
                .utilisateur(utilisateurService.getById(idUtilisateur))
                .contenu(contenu.trim())
                .salairePropose(salairePropose)
                .build();
        return messageRepository.save(m);
    }

    private void exigerPhaseNegociation(PropositionEmbauche p) {
        if (!Statut.APPROUVEE.equals(p.getStatut().getLibelle())) {
            throw new IllegalStateException(
                    "La proposition n'est pas en phase de négociation (statut actuel : "
                            + p.getStatut().getLibelle() + ").");
        }
    }

    private void exigerContenuOuSalaire(String contenu, BigDecimal salaire) {
        if ((contenu == null || contenu.isBlank()) && salaire == null) {
            throw new IllegalArgumentException(
                    "Le message doit contenir un texte ou une contre-proposition salariale.");
        }
    }

    private void exigerSalairePositif(BigDecimal salaire) {
        if (salaire != null && salaire.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le salaire contre-proposé doit être strictement positif.");
        }
    }
}