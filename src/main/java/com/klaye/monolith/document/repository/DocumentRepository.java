package com.klaye.monolith.document.repository;

import com.klaye.monolith.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour la gestion des métadonnées des Documents.
 * Migré depuis Document-Service vers le Monolithe.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Recherche les documents liés à une mission spécifique d'un dossier.
     */
    List<Document> findByCodeDossierAndCodeMission(String codeDossier, String codeMission);
}
