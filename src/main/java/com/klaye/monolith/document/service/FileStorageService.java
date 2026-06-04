package com.klaye.monolith.document.service;

import com.klaye.monolith.document.dto.DocumentResponse;
import com.klaye.monolith.document.entity.Document;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Interface du service de stockage de fichiers pour le Monolithe.
 */
public interface FileStorageService {

    Document store(MultipartFile file, String codeDossier, String codeMission, Long uploadedBy);

    Resource loadAsResource(String cheminStockage);

    void delete(UUID id);

    Document getDocumentEntity(UUID id);

    DocumentResponse findById(UUID id, String baseUrl);

    List<DocumentResponse> findByDossierAndMission(String codeDossier, String codeMission, String baseUrl);
}