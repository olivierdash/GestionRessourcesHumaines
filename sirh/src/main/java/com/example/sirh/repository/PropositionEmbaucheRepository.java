package com.example.sirh.repository;

import com.example.sirh.dto.PropositionEmbaucheFilter;
import com.example.sirh.model.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Integer> {

        List<PropositionEmbauche> findAllByOrderByIdPropositionDesc();

        List<PropositionEmbauche> findByCandidatureCandidatIdCandidatOrderByIdPropositionDesc(
                        Integer idCandidat);

        Optional<PropositionEmbauche> findByIdPropositionAndCandidatureCandidatIdCandidat(
                        Integer idProposition, Integer idCandidat);

        long countByDatePropositionBetween(LocalDateTime start, LocalDateTime end);

        boolean existsByCandidatureIdCandidatureAndStatutLibelleIn(
                        Integer idCandidature, List<String> statuts);

        @Query("""
                        SELECT p FROM PropositionEmbauche p
                        WHERE p.candidature.idCandidature IN :idsCandidatures
                          AND p.statut.libelle IN :statuts
                        """)
        List<PropositionEmbauche> findActivesByCandidatureIds(
                        @Param("idsCandidatures") List<Integer> idsCandidatures,
                        @Param("statuts") List<String> statuts);

        @Query("""
                        SELECT p FROM PropositionEmbauche p
                        WHERE p.statut.id = COALESCE(:#{#f.statut}, p.statut.id)
                          AND p.candidature.candidat.idCandidat = COALESCE(:#{#f.idCandidat}, p.candidature.candidat.idCandidat)
                          AND p.candidature.demandeRecrutement.poste.idPoste = COALESCE(:#{#f.idPoste}, p.candidature.demandeRecrutement.poste.idPoste)
                          AND p.candidature.demandeRecrutement.poste.departement.idDepartement = COALESCE(:#{#f.idDepartement}, p.candidature.demandeRecrutement.poste.departement.idDepartement)
                          AND p.dateExpiration <= COALESCE(:#{#f.dateExpirationMax}, p.dateExpiration)
                        ORDER BY p.idProposition DESC
                        """)
        List<PropositionEmbauche> findWithFilters(@Param("f") PropositionEmbaucheFilter filter);
}
