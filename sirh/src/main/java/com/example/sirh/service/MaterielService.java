package com.example.sirh.service;

import com.example.sirh.model.MaterielEmploye;
import com.example.sirh.repository.MaterielEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @author moi
 * module 12
 */

@Service
@RequiredArgsConstructor
@Transactional
public class MaterielService {

    private final MaterielEmployeRepository repo;

    /** RÈGLE : date_restitution >= date_remise (le CHECK SQL le garantit aussi). */
    public MaterielEmploye restituer(Integer idMateriel, String etat, java.math.BigDecimal retenue) {
        MaterielEmploye m = repo.findById(idMateriel)
                .orElseThrow(() -> new IllegalArgumentException("Matériel introuvable"));
        if (m.getDateRestitution() != null)
            throw new IllegalStateException("Matériel déjà restitué");

        m.setDateRestitution(LocalDate.now());
        m.setEtatRestitution(etat);
        if (retenue != null) m.setMontantRetenue(retenue);
        return repo.save(m);
    }

    public List<MaterielEmploye> listerNonRestitues(Integer idEmployer) {
        return repo.findByEmployeIdEmployerAndDateRestitutionIsNull(idEmployer);
    }

    /** Utilisé par le service Offboarding pour bloquer la clôture. */
    public boolean toutEstRestitue(Integer idEmployer) {
        return repo.countByEmployeIdEmployerAndDateRestitutionIsNull(idEmployer) == 0;
    }
}