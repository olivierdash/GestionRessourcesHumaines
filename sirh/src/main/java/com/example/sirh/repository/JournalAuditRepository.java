package com.example.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sirh.model.JournalAudit;
import java.util.List;


@Repository 
public interface JournalAuditRepository extends JpaRepository<JournalAudit,Integer> {
    List<JournalAudit> findByEntiteCibleAndIdEntiteCibleOrderByDateActionDesc(String entiteCible, Integer idEntiteCible);
}
