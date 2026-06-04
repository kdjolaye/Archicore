package com.klaye.monolith.document.service;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.service.AuditService;
import com.klaye.monolith.audit.util.AuditUtils;
import com.klaye.monolith.document.config.FileStorageProperties;
import com.klaye.monolith.document.dto.DocumentResponse;
import com.klaye.monolith.document.entity.Document;
import com.klaye.monolith.document.mapper.DocumentMapper;
import com.klaye.monolith.document.repository.DocumentRepository;
import com.klaye.monolith.mission.service.MissionService;
import com.klaye.monolith.mission.dto.MissionResponse;
import com.klaye.monolith.common.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final DocumentRepository documentRepository;
    private final FileStorageProperties storageProperties;
    private final MissionService missionService;
    private final AuditService auditService;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        String location = storageProperties.getRootLocation();

        if (location == null || location.isBlank()) {
            throw new IllegalStateException(
                    "[FileStorage] rootLocation est NULL. " +
                            "Vérifier que 'file.root-location' est bien défini dans application.yml"
            );
        }

        Path root = Paths.get(location).toAbsolutePath().normalize();
        log.info("[FileStorage] Répertoire configuré : {}", root);

        try {
            Files.createDirectories(root);
            log.info("[FileStorage] Répertoire prêt ✓");
        } catch (IOException e) {
            throw new IllegalStateException(
                    "[FileStorage] Impossible de créer le répertoire : " + root +
                            " | Cause : " + e.getMessage(), e
            );
        }

        if (!Files.isWritable(root)) {
            throw new IllegalStateException(
                    "[FileStorage] Répertoire non accessible en écriture : " + root
            );
        }
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Document store(MultipartFile file, String codeDossier, String codeMission, Long uploadedBy) {

        MissionResponse mission = missionService.findByCode(codeMission);
        if (!codeDossier.equals(mission.codeDossier())) {
            throw new RuntimeException("Mission " + codeMission + " n'appartient pas au dossier " + codeDossier);
        }

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension        = getExtension(originalFilename);
        UUID   id               = UUID.randomUUID();
        String cleanDossier     = sanitize(codeDossier);
        String cleanMission     = sanitize(codeMission);
        String storedFilename   = id + (extension.isEmpty() ? "" : "." + extension);
        String relativePath     = cleanDossier + "/" + cleanMission + "/" + storedFilename;

        saveToDisk(file, relativePath, storedFilename);

        Document document;
        try {
            document = Document.builder()
                    .id(id)
                    .nomFichierOriginal(originalFilename != null ? originalFilename : "unknown")
                    .nomFichierStocke(storedFilename)
                    .cheminStockage(relativePath)
                    .typeFichier(file.getContentType())
                    .taille(file.getSize())
                    .codeDossier(cleanDossier)
                    .codeMission(cleanMission)
                    .uploadedBy(uploadedBy)
                    .build();

            document = documentRepository.save(document);

        } catch (Exception e) {
            log.error("[FileStorage] Erreur BDD après écriture disque. Nettoyage : {}", relativePath);
            try {
                Path root = Paths.get(storageProperties.getRootLocation()).toAbsolutePath().normalize();
                Files.deleteIfExists(root.resolve(relativePath));
            } catch (IOException ex) {
                log.error("[FileStorage] Échec nettoyage fichier orphelin : {}", relativePath, ex);
            }
            throw e;
        }

        auditService.log(
                AuditAction.UPLOAD_DOCUMENT,
                AuditUtils.getCurrentUsername(),
                uploadedBy,
                "Document",
                document.getId().toString(),
                "Upload : " + document.getNomFichierOriginal() +
                        " | dossier=" + cleanDossier + " | mission=" + cleanMission,
                AuditUtils.getClientIp()
        );

        return document;
    }

    // -------------------------------------------------------------------------
    // Téléchargement
    // -------------------------------------------------------------------------

    @Override
    public Resource loadAsResource(String cheminStockage) {
        try {
            Path root     = Paths.get(storageProperties.getRootLocation()).toAbsolutePath().normalize();
            Path filePath = root.resolve(cheminStockage).normalize();

            if (!filePath.startsWith(root)) {
                throw new RuntimeException("Tentative d'accès non autorisée en dehors du répertoire racine.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String filename = cheminStockage.substring(cheminStockage.lastIndexOf("/") + 1);
                String uuidPart = filename.contains(".")
                        ? filename.substring(0, filename.lastIndexOf("."))
                        : filename;

                auditService.log(
                        AuditAction.TELECHARGER_DOCUMENT,
                        AuditUtils.getCurrentUsername(),
                        null,
                        "Document",
                        uuidPart,
                        "Téléchargement : " + filename,
                        AuditUtils.getClientIp()
                );

                return resource;
            } else {
                throw new ResourceNotFoundException("Fichier introuvable : " + cheminStockage);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier : " + cheminStockage, e);
        }
    }

    // -------------------------------------------------------------------------
    // Suppression
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void delete(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));

        auditService.log(
                AuditAction.SUPPRIMER_DOCUMENT,
                AuditUtils.getCurrentUsername(),
                null,
                "Document",
                id.toString(),
                "Suppression : " + document.getNomFichierOriginal() +
                        " | dossier=" + document.getCodeDossier() +
                        " | mission=" + document.getCodeMission(),
                AuditUtils.getClientIp()
        );

        try {
            Path root = Paths.get(storageProperties.getRootLocation()).toAbsolutePath().normalize();
            Files.deleteIfExists(root.resolve(document.getCheminStockage()));
        } catch (IOException e) {
            log.error("Erreur lors de la suppression physique du fichier : {}", id, e);
        }

        documentRepository.delete(document);
    }

    // -------------------------------------------------------------------------
    // Accès direct à l'entité (usage interne — download)
    // -------------------------------------------------------------------------

    @Override
    public Document getDocumentEntity(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));
    }

    // -------------------------------------------------------------------------
    // Consultation (findById)
    // -------------------------------------------------------------------------

    @Override
    public DocumentResponse findById(UUID id, String baseUrl) {
        Document document = getDocumentEntity(id);

        auditService.log(
                AuditAction.CONSULTER_DOCUMENT,
                AuditUtils.getCurrentUsername(),
                null,
                "Document",
                id.toString(),
                "Consultation : " + document.getNomFichierOriginal(),
                AuditUtils.getClientIp()
        );

        return DocumentMapper.toResponse(document, buildUrl(baseUrl, id));
    }

    // -------------------------------------------------------------------------
    // Liste des documents d'une mission
    // -------------------------------------------------------------------------

    @Override
    public List<DocumentResponse> findByDossierAndMission(String codeDossier, String codeMission, String baseUrl) {
        return documentRepository.findByCodeDossierAndCodeMission(codeDossier, codeMission).stream()
                .map(doc -> DocumentMapper.toResponse(doc, buildUrl(baseUrl, doc.getId())))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !storageProperties.getAllowedTypes().contains(contentType)) {
            throw new RuntimeException("Type de fichier non supporté : " + contentType);
        }
        long maxSizeInBytes = (long) storageProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxSizeInBytes) {
            throw new RuntimeException(
                    "Le fichier dépasse la taille maximale de " + storageProperties.getMaxSizeMb() + "MB"
            );
        }
    }

    private String sanitize(String input) {
        if (input == null) return "unknown";
        String clean = StringUtils.cleanPath(input);
        if (clean.contains("..")) {
            throw new RuntimeException("Tentative de traversée de répertoire détectée dans : " + input);
        }
        return clean;
    }

    private void saveToDisk(MultipartFile file, String relativePath, String filename) {
        try {
            Path root       = Paths.get(storageProperties.getRootLocation()).toAbsolutePath().normalize();
            Path targetFile = root.resolve(relativePath.replace("/", java.io.File.separator)).normalize();

            log.info("[FileStorage] Root     : {}", root);
            log.info("[FileStorage] Target   : {}", targetFile);
            log.info("[FileStorage] Writable : {}", Files.isWritable(root));

            Files.createDirectories(targetFile.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur écriture fichier [" + filename + "]" +
                            " | root=" + storageProperties.getRootLocation() +
                            " | cause=" + e.getClass().getSimpleName() + ": " + e.getMessage(), e
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String buildUrl(String baseUrl, UUID id) {
        return baseUrl + "/api/v1/documents/download/" + id;
    }
}