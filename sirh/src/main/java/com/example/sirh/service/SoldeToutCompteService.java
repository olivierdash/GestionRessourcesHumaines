package com.example.sirh.service;

import com.example.sirh.model.SoldeToutCompte;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.SoldeToutCompteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author moi
 * module 12
 */

@Service
@RequiredArgsConstructor
@Transactional
public class SoldeToutCompteService {

    private final SoldeToutCompteRepository repo;

    /** RÈGLES :
     *  - net_a_payer = total_gains - total_retenues (calcul serveur, jamais client)
     *  - statut démarre en BROUILLON
     */
    public SoldeToutCompte creer(SoldeToutCompte stc) {
        if (stc.getDateCalcul() == null) stc.setDateCalcul(LocalDate.now());
        stc.setStatut("BROUILLON");
        recalculer(stc);
        return repo.save(stc);
    }

    public SoldeToutCompte modifier(SoldeToutCompte stc) {
        if (stc.getIdStc() == null)
            throw new IllegalArgumentException("Un identifiant de STC est requis");
        SoldeToutCompte existant = repo.findById(stc.getIdStc())
                .orElseThrow(() -> new IllegalArgumentException("STC introuvable"));
        if (!"BROUILLON".equals(existant.getStatut()))
            throw new IllegalStateException("STC non modifiable (statut " + existant.getStatut() + ")");
        if (stc.getSalaireMensuelReference() == null)
            throw new IllegalArgumentException("Le salaire mensuel de référence est obligatoire");
        if (stc.getJoursCongesRestants() != null && stc.getJoursCongesRestants().signum() < 0)
            throw new IllegalArgumentException("Les jours de congés restants ne peuvent pas être négatifs");
        existant.setSalaireMensuelReference(stc.getSalaireMensuelReference());
        existant.setJoursCongesRestants(stc.getJoursCongesRestants() == null
                ? BigDecimal.ZERO : stc.getJoursCongesRestants());
        existant.setTotalGains(stc.getTotalGains());
        existant.setTotalRetenues(stc.getTotalRetenues());
        recalculer(existant);
        return repo.save(existant);
    }

    private void recalculer(SoldeToutCompte stc) {
        stc.setTotalGains(stc.getTotalGains() == null ? BigDecimal.ZERO : stc.getTotalGains());
        stc.setTotalRetenues(stc.getTotalRetenues() == null ? BigDecimal.ZERO : stc.getTotalRetenues());
        if (stc.getTotalGains().signum() < 0 || stc.getTotalRetenues().signum() < 0)
            throw new IllegalArgumentException("Les gains et retenues ne peuvent pas être négatifs");
        stc.setNetAPayer(stc.getTotalGains().subtract(stc.getTotalRetenues()));
    }

    /** RÈGLE : on ne valide qu'un STC en BROUILLON. */
    public SoldeToutCompte valider(Integer idStc, Utilisateur valideur) {
        SoldeToutCompte stc = repo.findById(idStc)
                .orElseThrow(() -> new IllegalArgumentException("STC introuvable"));
        if (!"BROUILLON".equals(stc.getStatut()))
            throw new IllegalStateException("Seul un STC BROUILLON peut être validé");

        stc.setStatut("VALIDE");
        stc.setValidePar(valideur);
        stc.setDateValidation(LocalDate.now());
        return repo.save(stc);
    }

    public SoldeToutCompte marquerPaye(Integer idStc) {
        SoldeToutCompte stc = repo.findById(idStc)
                .orElseThrow(() -> new IllegalArgumentException("STC introuvable"));
        if (!"VALIDE".equals(stc.getStatut()))
            throw new IllegalStateException("Seul un STC VALIDE peut être marqué PAYE");

        stc.setStatut("PAYE");
        return repo.save(stc);
    }

    public List<SoldeToutCompte> listerParStatut(String statut) {
        return repo.findByStatut(statut);
    }

    public Optional<SoldeToutCompte> findByrupture(Integer idRupture){
        return repo.findByRuptureContratIdRupture(idRupture);
    }
}