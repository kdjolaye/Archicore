package com.klaye.monolith.document.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un document archivé.
 * Contient les métadonnées et une URL de téléchargement générée.
 */
public record DocumentResponse(
        UUID id,
        String nomFichierOriginal,
        String nomFichierStocke,
        String cheminStockage,
        String typeFichier,
        Long taille,
        LocalDateTime createdAt,
        String downloadUrl) {
}
