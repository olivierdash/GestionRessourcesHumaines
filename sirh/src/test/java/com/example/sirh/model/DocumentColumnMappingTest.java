package com.example.sirh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

class DocumentColumnMappingTest {

    @Test
    void mapsExactlyTheColumnsPresentInDocumentTable() {
        assertEquals(Set.of(
                "id_document",
                "id_employer",
                "id_type_document",
                "id_tache",
                "nom_fichier",
                "fichier_url",
                "date_depot",
                "date_expiration",
                "statut_document",
                "id_depose_par"), mappedColumns());
    }

    private Set<String> mappedColumns() {
        return Arrays.stream(Document.class.getDeclaredFields())
                .map(field -> columnName(field))
                .collect(Collectors.toSet());
    }

    private String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isBlank()) {
            return column.name();
        }
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.name().isBlank()) {
            return joinColumn.name();
        }
        return field.getName();
    }
}
