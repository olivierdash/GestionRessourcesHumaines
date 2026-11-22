package com.example.sirh.service;

import com.example.sirh.model.TypeDocument;
import com.example.sirh.repository.TypeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeDocumentService {

    private final TypeDocumentRepository typeDocumentRepository;

    @Transactional(readOnly = true)
    public TypeDocument getById(Integer id) {
        return typeDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de document introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<TypeDocument> getAllTypes() {
        return typeDocumentRepository.findAll();
    }
}