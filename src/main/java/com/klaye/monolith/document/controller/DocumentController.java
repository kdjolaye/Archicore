package com.klaye.monolith.document.controller;

import com.klaye.monolith.auth.config.JwtUserDetails;
import com.klaye.monolith.document.dto.DocumentResponse;
import com.klaye.monolith.document.entity.Document;
import com.klaye.monolith.document.mapper.DocumentMapper;
import com.klaye.monolith.document.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur gérant les documents archivés dans le Monolithe.
 * Le contrôle d'accès est délégué entièrement à @PreAuthorize + CustomPermissionEvaluator.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final FileStorageService fileStorageService;

    /**
     * Upload — WRITE vérifié sur codeMission via le PermissionEvaluator.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasPermission(#codeMission, 'Document', 'WRITE')")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("codeDossier") String codeDossier,
            @RequestParam("codeMission") String codeMission,
            @AuthenticationPrincipal JwtUserDetails userDetails,
            HttpServletRequest request) {

        Document saved = fileStorageService.store(file, codeDossier, codeMission, userDetails.userId());
        String downloadUrl = buildBaseUrl(request) + "/api/v1/documents/download/" + saved.getId();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DocumentMapper.toResponse(saved, downloadUrl));
    }

    /**
     * Download — READ suffit, accès géré par @PreAuthorize.
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasPermission(null, 'Document', 'READ')")
    public ResponseEntity<Resource> download(
            @PathVariable UUID id,
            HttpServletRequest request) {

        Document document = fileStorageService.getDocumentEntity(id);
        Resource resource = fileStorageService.loadAsResource(document.getCheminStockage());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getTypeFichier()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getNomFichierOriginal() + "\"")
                .body(resource);
    }

    /**
     * Liste les documents d'une mission — READ suffit.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Document', 'READ')")
    public ResponseEntity<List<DocumentResponse>> list(
            @RequestParam String codeDossier,
            @RequestParam String codeMission,
            HttpServletRequest request) {

        List<DocumentResponse> documents = fileStorageService
                .findByDossierAndMission(codeDossier, codeMission, buildBaseUrl(request));

        return ResponseEntity.ok(documents);
    }

    /**
     * Suppression — DELETE vérifié via le PermissionEvaluator.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'Document', 'DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fileStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String buildBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}