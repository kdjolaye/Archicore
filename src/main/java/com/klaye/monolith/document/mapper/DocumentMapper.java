package com.klaye.monolith.document.mapper;

import com.klaye.monolith.document.dto.DocumentResponse;
import com.klaye.monolith.document.entity.Document;

/**
 * Mapper pour la conversion entre l'entité Document et ses DTOs.
 */
public class DocumentMapper {

    /**
     * Convertit une entité Document en réponse DTO, incluant l'URL de téléchargement.
     */
    public static DocumentResponse toResponse(Document document, String downloadUrl) {
        if (document == null) return null;
        return new DocumentResponse(
                document.getId(),
                document.getNomFichierOriginal(),
                document.getNomFichierStocke(),
                document.getCheminStockage(),
                document.getTypeFichier(),
                document.getTaille(),
                document.getCreatedAt(),
                downloadUrl
        );
    }
}
